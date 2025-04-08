package com.example.android_app.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NetworkManager {

    private static final String TAG = "NetworkManager";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    public interface ResponseCallback {
        void onResponse(boolean success, String response);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not found.", e);
            return null;
        }
    }

    private static void sendRequest(Context context, String url, String json, String method, ResponseCallback callback) {
        RequestBody body = json != null ? RequestBody.create(json, JSON_MEDIA_TYPE) : null;
        Request.Builder requestBuilder = new Request.Builder().url(url);

        switch (method.toUpperCase()) {
            case "POST":
                if (body != null) requestBuilder.post(body);
                break;
            case "PUT":
                if (body != null) requestBuilder.put(body);
                break;
            case "DELETE":
                requestBuilder.delete();
                break;
            default:
                requestBuilder.get();
                break;
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_SHORT).show()
                    );
                }
                callback.onResponse(false, null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = response.isSuccessful();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(context, "Запрос выполнен успешно", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Ошибка выполнения запроса", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                callback.onResponse(success, responseBody);
            }
        });
    }

    public static void sendLoginRequest(Context context, String username, String email, String password, ResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/login";
        try {
            String hashedPassword = sha256(password);
            if (hashedPassword == null) {
                callback.onResponse(false, "Password hashing failed.");
                return;
            }

            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("email", email);
            json.put("password", hashedPassword);

            sendRequest(context, url, json.toString(), "POST", callback);
        } catch (Exception e) {
            Log.e(TAG, "Error creating login JSON", e);
            callback.onResponse(false, null);
        }
    }
}