package com.dexilog.smartkeyboard.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TamilTest {

    private Tamil tamil = new Tamil();

    @Test
    public void singleVowel() {
        // single "a"
        assertConvert("\u0b85", "\u0b85");
    }

    @Test
    public void consonantPlusDefaultA() {
        // "ka"
        assertConvert("\u0b95", "\u0b95");
    }

    @Test
    public void consonantPlusExplicitI() {
        // "ki"
        assertConvert("\u0b95\u0b87", "\u0b95\u0bbf");
    }

    @Test
    public void consonantPlusExplicitAFollowedBySingleVowel() {
        // "ka-i"
        assertConvert("\u0b95\u0b85\u0b87", "\u0b95\u0b87");
    }

    @Test
    public void tamil() {
        // "tamil"
        assertConvert("\u0ba4\u0bae\u0b87\u0bb4\u0bcd", "தமிழ்");
    }

    @Test
    public void sri() {
        // "sri" ligature
        assertConvert("\u0b84", "ஶ்ரீ");
    }

    @Test
    public void ksi() {
        // "ksi"
        assertConvert("\u0b98\u0b87", "\u0b95\u0bcd\u0bb7\u0bbf");
    }

    private void assertConvert(String input, String expectedOutput) {
        assertEquals(expectedOutput, convert(input));
    }

    private String convert(String input) {
        StringBuilder output = new StringBuilder();
        tamil.convert(input, output);
        return output.toString();
    }
}
