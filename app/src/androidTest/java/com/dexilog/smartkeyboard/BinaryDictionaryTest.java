/*
 * Copyright (C) 2010-2017 Cyril Deguet  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.
 */

package com.dexilog.smartkeyboard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.input.WordComposerImpl;
import com.dexilog.smartkeyboard.suggest.Dictionary;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BinaryDictionaryTest {

    private static BinaryDictionary english;
    private int[] nextLettersFrequencies = new int[1280];

    @BeforeClass
    static public void setUpBeforeClass() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        Resources res = context.getPackageManager().getResourcesForApplication("net.cdeguet.smartkeyboardpro.en");
        AssetFileDescriptor fd = res.getAssets().openFd("en_dic.mp3");
        english = new BinaryDictionary(fd);
        fd.close();
    }

    @Before
    public void setUp() throws Exception {
        Arrays.fill(nextLettersFrequencies, 0);
    }

    @Test
    public void checkValidWord() throws Exception {
        assertThat(english.isValidWord("hello"), is(true));
        assertThat(english.isValidWord("grmbl"), is(false));
    }

    @Test
    public void getTypedWord() throws Exception {
        final List<String> words = getWordsFor("incredible");
        assertThat(words, is(Arrays.asList("incredible")));
    }

    @NonNull
    private List<String> getWordsFor(String input) {
        WordComposer composer = new WordComposerImpl();
        for (char c: input.toCharArray()) {
            composer.add(c, new int[]{c});
        }
        final List<String> words = new ArrayList<String>();
        english.getWords(composer, new Dictionary.WordCallback() {
            @Override
            public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency) {
                StringBuilder sb = new StringBuilder();
                sb.append(word, wordOffset, wordLength);
                words.add(sb.toString());
                return true;
            }
        }, false, nextLettersFrequencies);
        return words;
    }
}
