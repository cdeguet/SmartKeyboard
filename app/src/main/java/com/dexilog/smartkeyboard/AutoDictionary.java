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


class AutoDictionary extends ExpandableDictionary {
	// A word that is frequently typed and get's promoted to the user
	// dictionary, uses this
	// frequency.
	static final int FREQUENCY_FOR_AUTO_ADD = 250;
	// If the user touches a typed word 2 times or more, it will become
    // valid.
    private static final int VALIDITY_THRESHOLD = 2 * SmartKeyboard.FREQUENCY_FOR_PICKED;
    // If the user touches a typed word 4 times or more, it will be added to
    // the user dict.
    private static final int PROMOTION_THRESHOLD = 4 * SmartKeyboard.FREQUENCY_FOR_PICKED;

    private SmartKeyboard smartKeyboard;

    public AutoDictionary(SmartKeyboard smartKeyboard) {
        super(smartKeyboard);
        this.smartKeyboard = smartKeyboard;
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        final int frequency = getWordFrequency(word);
        return frequency > VALIDITY_THRESHOLD;
    }

    @Override
    public void addWord(String word, int addFrequency) {
        // Log.d("KBD", "addWord " + word + " " +
        // Integer.toString(addFrequency));

        final int length = word.length();
        // Don't add very short or very long words.
        if (length < 2 || length > getMaxWordLength())
            return;
        super.addWord(word, addFrequency);
        final int freq = getWordFrequency(word);
        if (freq > PROMOTION_THRESHOLD || smartKeyboard.mAutoAddToUserDic) {
            smartKeyboard.promoteToUserDictionary(word,
                    FREQUENCY_FOR_AUTO_ADD);
        }
    }
}
