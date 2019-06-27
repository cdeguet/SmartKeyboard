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

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class SwipeGestures {
    public static final int SWIPE_ACTION_NONE = 0;
    public static final int SWIPE_ACTION_SHIFT = 1;
    public static final int SWIPE_ACTION_SYMBOLS = 2;
    public static final int SWIPE_ACTION_ALT_SYMBOLS = 3;
    public static final int SWIPE_ACTION_ARROWS = 4;
    public static final int SWIPE_ACTION_CLOSE = 5;
    public static final int SWIPE_ACTION_SPEECH = 6;
    public static final int SWIPE_ACTION_LANG = 7;
    public static final int SWIPE_ACTION_T9 = 8;
    public static final int SWIPE_ACTION_COMPACT = 9;
    public static final int SWIPE_ACTION_COMPACT_OR_T9 = 10;
    public static final int SWIPE_ACTION_USER_DIC = 11;
    public static final int SWIPE_ACTION_CUSTOM_AUTOTEXT = 12;
    public static final int SWIPE_ACTION_BACKSPACE = 13;
    public static final int SWIPE_ACTION_SMILEY_KEY = 14;
    public static final int SWIPE_ACTION_DELETE_WORD = 15;
    public static final int SWIPE_ACTION_NEXT = 16;
    public static final int SWIPE_ACTION_CURSOR_LEFT = 17;
    public static final int SWIPE_ACTION_CURSOR_RIGHT = 18;
    public static final int SWIPE_ACTION_SPACE = 19;
    public static final int SWIPE_ACTION_PREV_LANG = 20;
    public static final int SWIPE_ACTION_CURSOR_UP = 21;
    public static final int SWIPE_ACTION_CURSOR_DOWN = 22;
    public static final int SWIPE_ACTION_CHANGE_IME = 23;
    public static final int SWIPE_ACTION_EMOJIS = 24;

    public SwipeGestures() {
    }

    public int getSwipeAction(String s) {
        if (s.equals("Symbols")) {
            return SWIPE_ACTION_SYMBOLS;
        } else if (s.equals("AltSymbols")) {
            return SWIPE_ACTION_ALT_SYMBOLS;
        } else if (s.equals("Close")) {
            return SWIPE_ACTION_CLOSE;
        } else if (s.equals("Shift")) {
            return SWIPE_ACTION_SHIFT;
        } else if (s.equals("ArrowKeypad")) {
            return SWIPE_ACTION_ARROWS;
        } else if (s.equals("SpeechToText")) {
            return SWIPE_ACTION_SPEECH;
        } else if (s.equals("ChangeLang")) {
            return SWIPE_ACTION_LANG;
        } else if (s.equals("PrevLang")) {
            return SWIPE_ACTION_PREV_LANG;
        } else if (s.equals("ToggleT9")) {
            return SWIPE_ACTION_T9;
        } else if (s.equals("ToggleCompact")) {
            return SWIPE_ACTION_COMPACT;
        } else if (s.equals("ToggleCompactOrT9")) {
            return SWIPE_ACTION_COMPACT_OR_T9;
        } else if (s.equals("UserDic")) {
            return SWIPE_ACTION_USER_DIC;
        } else if (s.equals("CustomAutoText")) {
            return SWIPE_ACTION_CUSTOM_AUTOTEXT;
        } else if (s.equals("Backspace")) {
            return SWIPE_ACTION_BACKSPACE;
        } else if (s.equals("SmileyKey")) {
            return SWIPE_ACTION_SMILEY_KEY;
        } else if (s.equals("DeleteWord")) {
            return SWIPE_ACTION_DELETE_WORD;
        } else if (s.equals("NextSuggestion")) {
            return SWIPE_ACTION_NEXT;
        } else if (s.equals("CursorLeft")) {
            return SWIPE_ACTION_CURSOR_LEFT;
        } else if (s.equals("CursorRight")) {
            return SWIPE_ACTION_CURSOR_RIGHT;
        } else if (s.equals("Space")) {
            return SWIPE_ACTION_SPACE;
        } else if (s.equals("CursorUp")) {
            return SWIPE_ACTION_CURSOR_UP;
        } else if (s.equals("CursorDown")) {
            return SWIPE_ACTION_CURSOR_DOWN;
        } else if (s.equals("ChangeIME")) {
            return SWIPE_ACTION_CHANGE_IME;
        } else if (s.equals("Emojis")) {
            return SWIPE_ACTION_EMOJIS;
        } else {
            return SWIPE_ACTION_NONE;
        }
    }

    public void changeIME(Context context) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
    }
}