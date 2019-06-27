/*
 * Copyright (C) 2010-2017 Cyril Deguet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dexilog.smartkeyboard;

import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.suggest.Dictionary;

import java.util.ArrayList;
import java.util.List;

public class FakeDictionary extends Dictionary {
    List<String> words = new ArrayList<String>();

    public void addWord(String word) {
        words.add(word);
    }

    @Override
    public void getWords(WordComposer composer, WordCallback callback, boolean modeT9,
                         int[] nextLettersFrequencies) {
        for (String word: words) {
            if (wordMatches(word, composer, modeT9)) {
                callback.addWord(word.toCharArray(), 0, word.length(), 1);
            }
        }
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        return words.contains(word.toString());
    }

    private boolean wordMatches(String word, WordComposer composer, boolean modeT9) {
        int composerSize = composer.size();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (i >= composerSize) {
                return true;
            }
            int codes[] = composer.getCodesAt(i);
            if (!charInCodes(c, codes)) {
                return false;
            }
        }
        return true;
    }

    private boolean charInCodes(char c, int[] codes) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i] == c)
                return  true;
        }
        return false;
    }

}
