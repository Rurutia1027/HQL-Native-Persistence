package org.tus.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility methods for encoding/decoding operations.
 * Minimal implementation for persistence-common module.
 */
public class CodecUtils {
    private static final Base64.Encoder BASE64_URLENCODER = Base64.getUrlEncoder().withoutPadding();

    /**
     * Encodes one byte array to string using the
     * URL and Filename safe type base64 encoding scheme.
     *
     * @param src source byte array
     * @return encoded string in base64
     */
    public static String encodeBase64URLSafeString(byte[] src) {
        return BASE64_URLENCODER.encodeToString(src);
    }
}
