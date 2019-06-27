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

import java.util.List;

public class TypedWordAlternatives extends WordAlternatives {
    private SmartKeyboard smartKeyboard;
    public WordComposer word;

    public TypedWordAlternatives(SmartKeyboard smartKeyboard, CharSequence chosenWord,
								 WordComposer wordComposer) {
        super(chosenWord);
        this.smartKeyboard = smartKeyboard;
        word = wordComposer;
    }

    @Override
    public CharSequence getOriginalWord() {
        return word.getTypedWord();
    }

    @Override
    public List<CharSequence> getAlternatives() {
        return smartKeyboard.getTypedSuggestions(word);
    }
}
