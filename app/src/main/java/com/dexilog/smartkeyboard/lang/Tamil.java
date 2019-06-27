package com.dexilog.smartkeyboard.lang;

public class Tamil implements Converter {

    public static final int COMPOUND_VOWEL_OFFSET = 56;

    @Override
    public void convert(CharSequence text, StringBuilder output) {
        final int len = text.length();
        boolean prevIsConsonant = false;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            char newChar = c;
            if (prevIsConsonant && isVowel(c)) {
                newChar += COMPOUND_VOWEL_OFFSET;
            }
            if (c == '\u0b84') {
                // SRI
                output.append("\u0bb6\u0bcd\u0bb0\u0bc0");
            } else if (c == '\u0b98') {
                // KS
                output.append("\u0b95\u0bcd\u0bb7");
            } else if (!prevIsConsonant || c != '\u0b85') {
                output.append(newChar);
            }
            prevIsConsonant = isConsonant(c);
        }
    }

    private boolean isVowel(char c) {
        return c > '\u0b85' && c <= '\u0b94';
    }

    private boolean isConsonant(char c) {
        return c >= '\u0b95' && c <= '\u0bb9';
    }

    @Override
    public void reverse(CharSequence text, StringBuilder output) {
    }
}
