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

package com.dexilog.smartkeyboard.suggest;

import com.dexilog.smartkeyboard.input.WordComposer;

public interface UserDictionary {
    void close();

    void loadDictionaryAsync();

    void addWord(String word, int frequency);

    void getWords(WordComposer codes, Dictionary.WordCallback callback, boolean modeT9,
                  int[] nextLettersFrequencies);

    boolean isValidWord(CharSequence word);
}
