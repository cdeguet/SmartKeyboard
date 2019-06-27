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

package com.dexilog.smartkeyboard.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;


public class EmojiKeyboard extends Keyboard {
    private static final String CODE = "#CODE#";
    private static final String EMPAGE = "#EMPAGE#";
    private static final String TAB = "#TAB#";

    public EmojiKeyboard(Context context, int xmlLayoutResId, int modeId, boolean isPortrait,
                         boolean includeArrows, boolean numbersTop, EmojiCategories emojiCategories) {
        super(context, xmlLayoutResId, modeId, isPortrait, includeArrows, numbersTop, emojiCategories);
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int exactX, int y,
                                   XmlResourceParser parser) {

        Key key = new Key(res, parent, exactX, y, parser);
        if (key.label != null && key.label.equals(CODE)) {
            int index = ((mKeyboardMode-1) * EmojiCategories.EMOJIS_PER_PAGE) + key.codes[0];
            String label = mEmojiCategories.getEmoji(index);
            if (label != null) {
                key.label = label;
                key.codes[0] = label.charAt(0);
                // Treat emojis as text to avoid bug with composing text (emojis appear
                // as squares)
                key.text = key.label;
            } else {
                setUnusedKey(key);
            }
        } else if (key.label != null && key.label.equals(TAB)) {
            int category = key.codes[0];
            String label = mEmojiCategories.getCategoryLabel(category);
            if (label != null) {
                key.label = label;
                key.codes[0] = KEYCODE_EMOJI_TAB1 - category;
            } else {
                setUnusedKey(key);
            }
        }

        handleSpecialCode(parser, key);
        if (key.label != null && key.label.equals(EMPAGE)) {
            key.label = String.format("%02d", mKeyboardMode);
        }
        return key;
    }

    private void setUnusedKey(Key key) {
        key.codes[0] = KEYCODE_EMOJI_NUM; // do nothing
        key.label = "";
    }
}
