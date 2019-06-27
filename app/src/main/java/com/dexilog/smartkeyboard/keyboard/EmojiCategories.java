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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;

import com.dexilog.smartkeyboard.R;

public class EmojiCategories {

    private static final String COMMA_REGEX = ",";
    private static final String VERTICAL_BAR_REGEX = "\\|";
    public static final int EMOJIS_PER_PAGE = 24;
    public static final int MAX_EMOJIS = 1350;

    String mEmojis[] = new String[MAX_EMOJIS];
    int categoryIndexes[] = new int[8];
    String categoryLabels[] = new String[8];
    int mCount = 0;
    int mNbPages = 0;
    int nbCategories = 0;

    private static final String SYMBOL_PEOPLE = "\u263a";
    private static final String SYMBOL_NATURE = "\uD83C\uDF34";
    private static final String SYMBOL_FOOD = "\uD83C\uDF55";
    private static final String SYMBOL_PLACES =  "\uD83D\uDE97";
    private static final String SYMBOL_ACTIVITY = "\u26bd";
    private static final String SYMBOL_OBJECTS = "\uD83D\uDC51";
    private static final String SYMBOL_SYMBOLS = "\uD83D\uDD3A";
    private static final String SYMBOL_FLAGS = "\uD83D\uDEA9";

    public EmojiCategories(Context context) {
        if (canShowUnicodeEightEmoji()) {
            addCategory(context, R.array.emoji_eight_smiley_people, SYMBOL_PEOPLE);
            addCategory(context, R.array.emoji_eight_animals_nature, SYMBOL_NATURE);
            addCategory(context, R.array.emoji_eight_food_drink, SYMBOL_FOOD);
            addCategory(context, R.array.emoji_eight_travel_places, SYMBOL_PLACES);
            addCategory(context, R.array.emoji_eight_activity, SYMBOL_ACTIVITY);
            addCategory(context, R.array.emoji_eight_objects, SYMBOL_OBJECTS);
            addCategory(context, R.array.emoji_eight_symbols, SYMBOL_SYMBOLS);
            addCategory(context, R.array.emoji_flags, SYMBOL_FLAGS);
        } else {
            addCategory(context, R.array.emoji_faces, SYMBOL_PEOPLE);
            addCategory(context, R.array.emoji_objects, SYMBOL_OBJECTS);
            addCategory(context, R.array.emoji_nature, SYMBOL_NATURE);
            addCategory(context, R.array.emoji_places, SYMBOL_PLACES);
            addCategory(context, R.array.emoji_symbols, SYMBOL_SYMBOLS);
            if (canShowFlagEmoji()) {
                addCategory(context, R.array.emoji_flags, SYMBOL_FLAGS);
            }
        }
        mNbPages = (mCount + EMOJIS_PER_PAGE - 1) / EMOJIS_PER_PAGE;
    }

    private void addCategory(Context context, int array, String categoryLabel)
    {
        categoryIndexes[nbCategories] = mCount / EMOJIS_PER_PAGE;
        categoryLabels[nbCategories] = categoryLabel;
        nbCategories++;
        final String[] category = context.getResources().getStringArray(array);
        for (String codesArraySpec: category) {
            final String[] strs = codesArraySpec.split(VERTICAL_BAR_REGEX, -1);
            String labelSpec;
            if (strs.length <= 1) {
                labelSpec = codesArraySpec;
            } else {
                labelSpec = strs[0];
            }
            final StringBuilder sb = new StringBuilder();
            for (final String codeInHex: labelSpec.split(COMMA_REGEX)) {
                int codePoint = Integer.parseInt(codeInHex, 16);
                sb.appendCodePoint(codePoint);
            }
            final String label = sb.toString();
            mEmojis[mCount] = label;
            mCount++;
        }
        int lastPageSize = category.length % EMOJIS_PER_PAGE;
        int padding = 0;
        if (lastPageSize < EMOJIS_PER_PAGE) {
            padding = EMOJIS_PER_PAGE - lastPageSize;
        }
        for (int i = 0; i < padding; i++) {
            mEmojis[mCount++] = null;
        }
    }

    @TargetApi(23)
    private static boolean canShowFlagEmoji() {
        Paint paint = new Paint();
        String switzerland = "\uD83C\uDDE8\uD83C\uDDED"; //  U+1F1E8 U+1F1ED Flag for Switzerland
        try {
            return paint.hasGlyph(switzerland);
        } catch (NoSuchMethodError e) {
            // Compare display width of single-codepoint emoji to width of flag emoji to determine
            // whether flag is rendered as single glyph or two adjacent regional indicator symbols.
            float flagWidth = paint.measureText(switzerland);
            float standardWidth = paint.measureText("\uD83D\uDC27"); //  U+1F427 Penguin
            return flagWidth < standardWidth * 1.25;
            // This assumes that a valid glyph for the flag emoji must be less than 1.25 times
            // the width of the penguin.
        }
    }

    @TargetApi(23)
    private static boolean canShowUnicodeEightEmoji() {
        Paint paint = new Paint();
        String cheese = "\uD83E\uDDC0"; //  U+1F9C0 Cheese wedge
        try {
            return paint.hasGlyph(cheese);
        } catch (NoSuchMethodError e) {
            float cheeseWidth = paint.measureText(cheese);
            float tofuWidth = paint.measureText("\uFFFE");
            return cheeseWidth > tofuWidth;
            // This assumes that a valid glyph for the cheese wedge must be greater than the width
            // of the noncharacter.
        }
    }

    public String getEmoji(int index) {
        return mEmojis[index];
    }

    public int getNbPages() {
        return mNbPages;
    }

    public int getCategoryIndex(int category) {
        return categoryIndexes[category];
    }

    public String getCategoryLabel(int category) {
        return categoryLabels[category];
    }
}
