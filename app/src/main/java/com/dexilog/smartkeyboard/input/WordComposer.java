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

package com.dexilog.smartkeyboard.input;

import com.dexilog.smartkeyboard.lang.Converter;

public interface WordComposer {
    void add(int primaryCode, int[] codes);
    void convertWord(Converter converter);
    void deleteLast();
    int[] getCodesAt(int index);
    CharSequence getConvertedWord();
    CharSequence getPreferredWord();
    CharSequence getTypedWord();
    void handleDakuten();
    boolean isAllUpperCase();
    boolean isCapitalized();
    boolean isMostlyCaps();
    void reset();
    void setCapitalized(boolean capitalized);
    void setPreferredWord(CharSequence preferred);
    int size();
    int addCharacter(int primaryCode, int[] keyCodes, boolean replace, boolean isShifted);

    void forceTypedWord(CharSequence word);
}
