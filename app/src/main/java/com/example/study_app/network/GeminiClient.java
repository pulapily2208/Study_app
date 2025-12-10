package com.example.study_app.network;

import android.util.Log;

import com.example.study_app.BuildConfig;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class GeminiClient {
    private static final String TAG = "GeminiClient";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com";
    private static String MODEL = "gemini-2.5-flash";

    private final OkHttpClient client;
    private final String apiKey;

    public GeminiClient() {
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        this.client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .readTimeout(java.time.Duration.ofSeconds(12))
                .writeTimeout(java.time.Duration.ofSeconds(12))
                .callTimeout(java.time.Duration.ofSeconds(15))
                .build();
    }

    public void setModel(String modelName) {
        if (modelName != null && !modelName.trim().isEmpty()) {
            MODEL = modelName.trim();
        }
    }

    public String generateContent(String prompt) throws IOException {
        String url = BASE_URL + "/v1/models/" + MODEL + ":generateContent?key=" + apiKey;

        String json = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"parts\": [ { \"text\": " + escapeJsonString(prompt) + " } ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyStr = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                Log.e(TAG, "HTTP " + response.code() + " body: " + bodyStr);
                throw new IOException(buildFriendlyError(response.code(), bodyStr));
            }
            return bodyStr;
        }
    }

    private String buildFriendlyError(int code, String bodyStr) {
        String msg = "Gemini API lỗi " + code;
        if (bodyStr != null && !bodyStr.isEmpty()) {
            try {
                org.json.JSONObject root = new org.json.JSONObject(bodyStr);
                if (root.has("error")) {
                    org.json.JSONObject err = root.getJSONObject("error");
                    String status = err.optString("status");
                    String message = err.optString("message");
                    if (!message.isEmpty()) {
                        msg += ": " + message;
                    } else {
                        msg += ": " + bodyStr;
                    }
                    if (!status.isEmpty()) {
                        msg += " (" + status + ")";
                    }
                } else {
                    msg += ": " + bodyStr;
                }
            } catch (Exception ignore) {
                msg += ": " + bodyStr;
            }
        }
        if (code == 401 || code == 403) {
            msg += ". Vui lòng kiểm tra API Key và quyền truy cập.";
        }
        return msg;
    }

    private String escapeJsonString(String s) {
        if (s == null)
            return "\"\"";
        String escaped = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }
}
