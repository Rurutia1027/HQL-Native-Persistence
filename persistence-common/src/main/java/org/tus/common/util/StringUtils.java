package org.tus.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Utility class for string operations.
 * Minimal implementation for persistence-common module.
 */
public class StringUtils {
    public static final String UTF8 = "UTF-8";
    public static final Charset UTF8_CHARSET = Charset.forName(UTF8);
    
    public static final String UTC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Define which characters are invalid for name.
     * Invalid characters: ; / ? : @ = & " < > # % { } | \ ' ^ ~ [ ] ` <blank>
     */
    private static final Pattern INVALID_NAME_PATTERN = Pattern.compile(".*[;/?:@=&\\\"<>#%{}|\\\\'^~\\[\\]`\\s\u0000].*");

    /**
     * Determines whether the given {@link CharSequence} is not {@code null} and is not empty (has length greater
     * than zero). A {@link CharSequence} that contains only whitespace returns {@code true}.
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Determine whether the given {@link CharSequence} has text, that is, that it is not {@code null}, is not
     * empty (has length greater than {@code 0}), and contains at least one non-whitespace character.
     */
    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int length = str.length();
        for (int i = 0; i < length; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasText(String str) {
        return hasText((CharSequence) str);
    }

    public static boolean isEmpty(String str) {
        return !hasText((CharSequence) str);
    }

    /**
     * Joins the given array or variable number of String objects into a single string with components delimited by
     * the given separator.
     */
    public static String join(String separator, Object... args) {
        StringBuilder builder = new StringBuilder();
        boolean firstTime = true;
        for (Object arg : args) {
            if (!firstTime) {
                builder.append(separator);
            }
            builder.append(arg.toString());
            firstTime = false;
        }
        return builder.toString();
    }

    /**
     * Computes the SHA1 digest of the given text.
     */
    public static String digest(String input) {
        return CodecUtils.encodeBase64URLSafeString(DigestUtils.sha1(input.getBytes(UTF8_CHARSET)));
    }

    public static boolean isNameValid(String name) {
        return !INVALID_NAME_PATTERN.matcher(name).matches();
    }
}
