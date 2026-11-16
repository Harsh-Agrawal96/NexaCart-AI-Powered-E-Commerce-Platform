package com.n11bootcamp.ai_service.util;


public final class LlmJsonExtractUtils {

    private LlmJsonExtractUtils() {
    }

    public static String extractFirstJsonObject(String raw) {
        if (raw == null) {
            return null;
        }
        int a = raw.indexOf('{');
        int b = raw.lastIndexOf('}');
        if (a >= 0 && b > a) {
            return raw.substring(a, b + 1);
        }
        return null;
    }
}
