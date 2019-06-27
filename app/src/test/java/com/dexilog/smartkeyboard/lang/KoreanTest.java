package com.dexilog.smartkeyboard.lang;

import com.dexilog.smartkeyboard.input.WordComposerImpl;
import com.dexilog.smartkeyboard.lang.Korean;

import static org.junit.Assert.*;

import org.junit.Test;

public class KoreanTest {

    Korean korean = new Korean(new WordComposerImpl());

    @Test
    public void testConvertEmpty() {
        assertConvert("", "");
    }

    @Test
    public void testConvertJamoToHangeul() {
        // JamoHangeul‰
        assertConvert("ㅈㅏㅁㅗㅎㅏㄴㄱㅡㄹ", "자모한글");
        // SeoulPyeongYang
        assertConvert("ㅅㅓㅇㅜㄹㅍㅕㅇㅇㅑㅇ", "서울평양");
    }

    @Test
    public void testConvertHangeulToJamo() {
        // JamoHangeul
        assertReverse("자모한글", "ㅈㅏㅁㅗㅎㅏㄴㄱㅡㄹ");
        // SeoulPyeongYang
        assertReverse("서울평양", "ㅅㅓㅇㅜㄹㅍㅕㅇㅇㅑㅇ");
    }

    private void assertReverse(String input, String expectedOutput) {
        assertEquals(expectedOutput, reverse(input));
    }

    private void assertConvert(String input, String expectedOutput) {
        assertEquals(expectedOutput, convert(input));
    }

    private String convert(String input) {
        StringBuilder output = new StringBuilder();
        korean.convert(input, output);
        return output.toString();
    }

    private String reverse(String input) {
        StringBuilder output = new StringBuilder();
        korean.reverse(input, output);
        return output.toString();
    }
}