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

import com.dexilog.smartkeyboard.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyboardFactory {
    static private String EMPTY_STRING = "";

    private static final int ARROWS_MAIN_PORTRAIT = 1;
    private static final int ARROWS_MAIN_LANDSCAPE = 2;
    private static final int ARROWS_MAIN_ALWAYS = 3;

    Map<String,Integer> mLangLayouts = null;
    Map<String,Integer> mCompactLayouts = null;
    Map<String,Integer> mT9Layouts = null;
    Map<String,Keyboard> mKeyboardCache = new HashMap<String, Keyboard>();
    private Set<String> mLatinLayoutList;
    boolean mPortrait; // true if portrait, false if landscape
    int mArrowsMain = 0;
    int mNumbersTop = 0;
    private String mLatinLayout = "";
    private boolean mHebrewAlt = false;
    private boolean mCzechFull = true;
    private boolean mAltCompact = false;
    EmojiCategories mEmojiCategories;

    public KeyboardFactory(final String[] latinLayouts, boolean portrait) {
        mPortrait = portrait;
        final int latinLayoutsSize = latinLayouts.length;
        mLatinLayoutList = new HashSet<String>();
        for (int i=0; i<latinLayoutsSize; i++) {
            mLatinLayoutList.add(latinLayouts[i]);
        }
    }

    public void makeKeyboards(boolean clear) {
        if (mLangLayouts == null) {
            mLangLayouts = new HashMap<String,Integer>();
            mCompactLayouts = new HashMap<String,Integer>();
            mT9Layouts = new HashMap<String,Integer>();

            mLangLayouts.put("AF", R.xml.qwerty_af);
            mCompactLayouts.put("AF", R.xml.qwerty_compact_intl);
            mT9Layouts.put("AF", R.xml.t9_intl);

            mLangLayouts.put("AR", R.xml.arabic);
            mCompactLayouts.put("AR", R.xml.arabic);
            mT9Layouts.put("AR", R.xml.t9_ar);

            mLangLayouts.put("AZ", R.xml.azerbaijani);
            mCompactLayouts.put("AZ", R.xml.azerbaijani);
            mT9Layouts.put("AZ", R.xml.azerbaijani);

            mLangLayouts.put("BE", R.xml.belarusian);
            mCompactLayouts.put("BE", R.xml.belarusian);
            mT9Layouts.put("BE", R.xml.belarusian);

            mLangLayouts.put("BS", R.xml.qwertz_sr);
            mCompactLayouts.put("BS", R.xml.qwertz_sr_compact);
            mT9Layouts.put("BS", R.xml.t9_sr);

            mLangLayouts.put("BG_BDS", R.xml.bulgarian_bds);
            mCompactLayouts.put("BG_BDS", R.xml.bulgarian_bds);

            mLangLayouts.put("BG_YaVERT", R.xml.bulgarian_yavert);
            mCompactLayouts.put("BG_YaVERT", R.xml.bulgarian_yavert);

            mLangLayouts.put("BG_TchYaVER", R.xml.bulgarian_alt);
            mCompactLayouts.put("BG_TchYaVER", R.xml.bulgarian_alt);

            mLangLayouts.put("CA", R.xml.qwerty_ca);

            mLangLayouts.put("CZ_QWERTY", R.xml.qwerty_cz_small);
            mLangLayouts.put("CZ_QWERTZ", R.xml.qwertz_cz_small);
            mCompactLayouts.put("CZ_QWERTY", R.xml.qwerty_cz_compact);
            mCompactLayouts.put("CZ_QWERTZ", R.xml.qwertz_cz_compact);
            mT9Layouts.put("CZ_QWERTY", R.xml.t9_czsl);
            mT9Layouts.put("CZ_QWERTZ", R.xml.t9_czsl);

            mLangLayouts.put("DA", R.xml.qwerty_da);
            mCompactLayouts.put("DA", R.xml.qwerty_scand_compact);
            mT9Layouts.put("DA", R.xml.t9_da);

            mLangLayouts.put("DE", R.xml.qwertz);
            mLangLayouts.put("DE_FULL", R.xml.qwertz_full);
            mCompactLayouts.put("DE", R.xml.qwertz_compact);
            mCompactLayouts.put("DE_FULL", R.xml.qwertz_compact);
            mT9Layouts.put("DE", R.xml.t9_de);
            mT9Layouts.put("DE_FULL", R.xml.t9_de);

            mLangLayouts.put("ET", R.xml.qwerty_et);
            mCompactLayouts.put("ET", R.xml.qwerty_compact_intl);
            mT9Layouts.put("ET", R.xml.t9_intl);

            mLangLayouts.put("EL", R.xml.greek);
            mCompactLayouts.put("EL", R.xml.greek_compact);
            mT9Layouts.put("EL", R.xml.t9_greek);

            mLangLayouts.put("EN", R.xml.qwerty);
            mLangLayouts.put("EN_UK", R.xml.qwerty_british);
            mLangLayouts.put("EN_INTL", R.xml.qwerty_intl);
            mLangLayouts.put("EN_DVORAK", R.xml.dvorak);
            mLangLayouts.put("EN_AZERTY", R.xml.azerty);
            mLangLayouts.put("EN_COLEMAK", R.xml.colemak);
            mCompactLayouts.put("EN_INTL", R.xml.qwerty_compact_intl);
            mCompactLayouts.put("EN_AZERTY", R.xml.azerty_compact);
            mT9Layouts.put("EN", R.xml.t9);
            mT9Layouts.put("EN_UK", R.xml.t9);
            mT9Layouts.put("EN_INTL", R.xml.t9_intl);

            mLangLayouts.put("ES", R.xml.qwerty_es);
            mCompactLayouts.put("ES", R.xml.qwerty_es_compact);
            mT9Layouts.put("ES", R.xml.t9_es);

            mLangLayouts.put("EO", R.xml.qwerty_eo);
            mCompactLayouts.put("EO", R.xml.qwerty_eo);

            mLangLayouts.put("FA", R.xml.farsi);
            mCompactLayouts.put("FA", R.xml.farsi);
            mT9Layouts.put("FA", R.xml.t9_fa);

            mLangLayouts.put("FI", R.xml.qwerty_fi_2);
            mLangLayouts.put("FI_SMALL", R.xml.qwerty_fi);
            mCompactLayouts.put("FI", R.xml.qwerty_scand_compact);
            mCompactLayouts.put("FI_SMALL", R.xml.qwerty_scand_compact);
            mT9Layouts.put("FI", R.xml.t9_fi);
            mT9Layouts.put("FI_SMALL", R.xml.t9_fi);

            mLangLayouts.put("FR", R.xml.azerty);
            mLangLayouts.put("FR_QWERTY", R.xml.qwerty_fr);
            mLangLayouts.put("FR_QWERTY_FULL", R.xml.qwerty_fr_full);
            mLangLayouts.put("FR_QWERTZ", R.xml.qwertz_fr);
            mCompactLayouts.put("FR", R.xml.azerty_compact);
            mCompactLayouts.put("FR_QWERTZ", R.xml.qwertz_compact);
            mT9Layouts.put("FR", R.xml.t9_fr);
            mT9Layouts.put("FR_QWERTY", R.xml.t9_fr);
            mT9Layouts.put("FR_QWERTY_FULL", R.xml.t9_fr);
            mT9Layouts.put("FR_QWERTZ", R.xml.t9_fr);

            mLangLayouts.put("GL", R.xml.qwerty_es);
            mCompactLayouts.put("GL", R.xml.qwerty_es_compact);
            mT9Layouts.put("GL", R.xml.t9_es);

            mLangLayouts.put("HI", R.xml.hindi);

            mLangLayouts.put("HE", R.xml.hebrew_iphone);
            mLangLayouts.put("HE_IPHONE", R.xml.hebrew_iphone);
            mCompactLayouts.put("HE", R.xml.hebrew_compact);
            mCompactLayouts.put("HE_IPHONE", R.xml.hebrew_compact);
            mT9Layouts.put("HE", R.xml.t9_hebrew);
            mT9Layouts.put("HE_IPHONE", R.xml.t9_hebrew);

            mLangLayouts.put("HR", R.xml.qwertz_sr);
            mCompactLayouts.put("HR", R.xml.qwertz_sr_compact);
            mT9Layouts.put("HR", R.xml.t9_sr);

            mLangLayouts.put("HU", R.xml.qwertz_hu);
            mLangLayouts.put("HU_QWERTY", R.xml.qwerty_hu);
            mCompactLayouts.put("HU", R.xml.qwertz_compact);
            mCompactLayouts.put("HU_QWERTY", R.xml.qwerty_compact_intl);
            mT9Layouts.put("HU", R.xml.t9_intl);
            mT9Layouts.put("HU_QWERTY", R.xml.t9_intl);

            mLangLayouts.put("HY", R.xml.armenian);
            mLangLayouts.put("HY_FULL", R.xml.armenian_full);
            mCompactLayouts.put("HY", R.xml.armenian);
            mCompactLayouts.put("HY_FULL", R.xml.armenian_full);
            mT9Layouts.put("HY", R.xml.armenian);
            mT9Layouts.put("HY_FULL", R.xml.armenian_full);

            mLangLayouts.put("ID", R.xml.qwerty);
            mCompactLayouts.put("ID", R.xml.qwerty_compact);
            mT9Layouts.put("ID", R.xml.t9);

            mLangLayouts.put("JP", R.xml.qwerty_jp);
            //mCompactLayouts.put("JP", R.xml.qwerty_jp);
            mT9Layouts.put("JP", R.xml.t9_jp);

            mLangLayouts.put("KA", R.xml.qwerty_ka);
            mCompactLayouts.put("KA", R.xml.qwerty_ka);

            mLangLayouts.put("KK", R.xml.kazakh);
            mCompactLayouts.put("KK", R.xml.kazakh);
            mT9Layouts.put("KK", R.xml.kazakh);

            mLangLayouts.put("KU_HAWAR", R.xml.kurdish_hawar);

            mLangLayouts.put("KU_SORANI", R.xml.kurdish_sorani);

            mLangLayouts.put("KO", R.xml.korean_numbers_priority);
            mLangLayouts.put("KO_SHORT", R.xml.korean_short_numbers);
            mT9Layouts.put("KO", R.xml.t9_ko);
            mT9Layouts.put("KO_SHORT", R.xml.t9_ko);

            mLangLayouts.put("IS", R.xml.qwerty_is);
            mCompactLayouts.put("IS", R.xml.qwerty_scand_compact);

            mLangLayouts.put("IT", R.xml.qwerty_it);
            mLangLayouts.put("IT_FULL", R.xml.qwerty_it_full);
            mCompactLayouts.put("IT", R.xml.qwerty_compact_intl);
            mCompactLayouts.put("IT_FULL", R.xml.qwerty_compact_intl);
            mT9Layouts.put("IT", R.xml.t9_intl);
            mT9Layouts.put("IT_FULL", R.xml.t9_intl);

            mLangLayouts.put("LB", R.xml.qwertz_lb);
            mCompactLayouts.put("LB", R.xml.qwertz_compact);
            mT9Layouts.put("LB", R.xml.t9_intl);

            mLangLayouts.put("LV", R.xml.qwerty_lv);
            mCompactLayouts.put("LV", R.xml.qwerty_compact_intl);
            mT9Layouts.put("LV", R.xml.t9_intl);

            mLangLayouts.put("LT", R.xml.qwerty_lt);
            mCompactLayouts.put("LT", R.xml.qwerty_compact_intl);
            mT9Layouts.put("LT", R.xml.t9_intl);

            mLangLayouts.put("MK", R.xml.macedonian);
            mCompactLayouts.put("MK", R.xml.macedonian);
            mT9Layouts.put("MK", R.xml.macedonian);

            mLangLayouts.put("MR", R.xml.hindi);

            mLangLayouts.put("MN", R.xml.mongolian);
            mCompactLayouts.put("MN", R.xml.mongolian);
            mT9Layouts.put("MN", R.xml.mongolian);

            mLangLayouts.put("NL", R.xml.qwerty_intl);
            mLangLayouts.put("NL_AZERTY", R.xml.azerty);
            mCompactLayouts.put("NL", R.xml.qwerty_compact_intl);
            mCompactLayouts.put("NL_AZERTY", R.xml.azerty_compact);
            mT9Layouts.put("NL_AZERTY", R.xml.t9_intl);
            mT9Layouts.put("NL", R.xml.t9_intl);

            mLangLayouts.put("NO", R.xml.qwerty_no);
            mCompactLayouts.put("NO", R.xml.qwerty_scand_compact);
            mT9Layouts.put("NO", R.xml.t9_intl);

            mLangLayouts.put("PL", R.xml.qwerty_pl);
            mCompactLayouts.put("PL", R.xml.qwerty_compact_intl);
            mT9Layouts.put("PL", R.xml.t9_pl);

            mLangLayouts.put("BR", R.xml.qwerty_intl);
            mCompactLayouts.put("BR", R.xml.qwerty_pt_compact);
            mT9Layouts.put("BR", R.xml.t9_pt);

            mLangLayouts.put("PT", R.xml.qwerty_intl);
            mCompactLayouts.put("PT", R.xml.qwerty_pt_compact);
            mT9Layouts.put("PT", R.xml.t9_pt);

            mLangLayouts.put("RO", R.xml.qwerty_ro);
            mCompactLayouts.put("RO", R.xml.qwerty_ro_compact);
            mT9Layouts.put("RO", R.xml.t9_ro);

            mLangLayouts.put("RU", R.xml.russian);
            mLangLayouts.put("RU_SMALL", R.xml.russian_small);
            mLangLayouts.put("RU_YaShERT", R.xml.russian_yashert);
            mCompactLayouts.put("RU", R.xml.russian_compact);
            mCompactLayouts.put("RU_SMALL", R.xml.russian_compact);
            mCompactLayouts.put("RU_YaShERT", R.xml.russian_compact);
            mT9Layouts.put("RU", R.xml.russian_t9);
            mT9Layouts.put("RU_SMALL", R.xml.russian_t9);
            mT9Layouts.put("RU_YaShERT", R.xml.russian_t9);

            mLangLayouts.put("SK_QWERTY", R.xml.qwerty_cz_small);
            mCompactLayouts.put("SK_QWERTY", R.xml.qwerty_cz_compact);
            mT9Layouts.put("SK_QWERTY", R.xml.t9_czsl);
            mLangLayouts.put("SK_QWERTZ", R.xml.qwertz_cz_small);
            mCompactLayouts.put("SK_QWERTZ", R.xml.qwertz_cz_compact);
            mT9Layouts.put("SK_QWERTZ", R.xml.t9_czsl);

            mLangLayouts.put("SL", R.xml.qwertz_sr);
            mCompactLayouts.put("SL", R.xml.qwertz_sr_compact);
            mT9Layouts.put("SL", R.xml.t9_sr);

            mLangLayouts.put("SQ", R.xml.qwertz_sq);
            mCompactLayouts.put("SQ", R.xml.qwertz_compact);
            mT9Layouts.put("SQ", R.xml.t9_fr);

            mLangLayouts.put("SR", R.xml.qwertz_sr);
            mCompactLayouts.put("SR", R.xml.qwertz_sr_compact);
            mT9Layouts.put("SR", R.xml.t9_sr);

            mLangLayouts.put("SR_CYRILLIC", R.xml.serbian_cyrillic);
            mCompactLayouts.put("SR_CYRILLIC", R.xml.qwertz_sr_compact);
            mT9Layouts.put("SR_CYRILLIC", R.xml.t9_sr);

            mLangLayouts.put("SV", R.xml.qwerty_sv);
            mCompactLayouts.put("SV", R.xml.qwerty_scand_compact);
            mT9Layouts.put("SV", R.xml.t9_sv);

            mLangLayouts.put("TA", R.xml.tamil);

            mLangLayouts.put("TH", R.xml.thai);
            mCompactLayouts.put("TH", R.xml.thai);
            mT9Layouts.put("TH", R.xml.t9_thai);

            mLangLayouts.put("TT", R.xml.tatar);
            mCompactLayouts.put("TT", R.xml.tatar);
            mT9Layouts.put("TT", R.xml.tatar);

            mLangLayouts.put("TR", R.xml.qwerty_tr);
            mCompactLayouts.put("TR", R.xml.qwerty_tr_compact);
            mT9Layouts.put("TR", R.xml.t9_tr);

            mLangLayouts.put("UK", R.xml.ukrainian);
            mCompactLayouts.put("UK", R.xml.ukrainian_compact);
            mT9Layouts.put("UK", R.xml.ukrainian_t9);

            mLangLayouts.put("VI", R.xml.qwerty);
            mCompactLayouts.put("VI", R.xml.qwerty_compact);
            mT9Layouts.put("VI", R.xml.t9);

            mLangLayouts.put("ZH", R.xml.qwerty_zh);

            mLangLayouts.put("PY", R.xml.pinyin);
        }
        if (clear) {
            mKeyboardCache.clear();
        }
    }

    public void setHebrewAlt(boolean hebrewAlt) {
        mHebrewAlt = hebrewAlt;
    }

    public void setCzechFull(boolean czechFull) {
        mCzechFull = czechFull;
    }

    public void setAltCompact(boolean altCompact) {
        mAltCompact = altCompact;
    }

    Keyboard getLangKeyboard(String lang, int mode, int portraitMode, Context context) {
        // Get the layout corresponding to the language
        String layout = lang;
        if (!getLatinLayout().equals(EMPTY_STRING) && mLatinLayoutList.contains(lang)) {
            layout = getLatinLayout();
        }

        // Check if keyboard already created
        Integer xml;
        switch (portraitMode) {
        case KeyboardSwitcher.PORTRAIT_T9:
            // Check if T9 exists for that language otherwise fallback to normal
            xml = mT9Layouts.get(layout);
            if (xml == null) {
                xml = R.xml.t9;
            }
            if (mAltCompact && xml == R.xml.t9) {
                xml = R.xml.t9_alt;
            }
            break;
        case KeyboardSwitcher.PORTRAIT_COMPACT:
            xml = mCompactLayouts.get(layout);
            if (xml == null) {
                xml = R.xml.qwerty_compact;
            }
            if (mAltCompact && xml == R.xml.qwerty_compact) {
                xml = R.xml.qwerty_compact_alt;
            }
            break;
        default:
            if (mHebrewAlt && layout.equals("HE")) {
                xml = R.xml.hebrew;
            } else if (mCzechFull && (layout.equals("CZ_QWERTY"))) {
                xml = R.xml.qwerty_cz;
            } else if (mCzechFull && (layout.equals("CZ_QWERTZ"))) {
                xml = R.xml.qwertz_cz;
            } else if (mCzechFull && (layout.equals("SK_QWERTY"))) {
                xml = R.xml.qwerty_sk;
            } else if (mCzechFull && (layout.equals("SK_QWERTZ"))) {
                xml = R.xml.qwertz_sk;
            } else {
                xml = mLangLayouts.get(layout);
            }
        }
        Keyboard kbd = getCachedKeyboard(xml, mode, context);
        kbd.setLanguage(lang.substring(0, 2));
        return kbd;
    }

    public Keyboard getCachedKeyboard(int xml, int mode, Context context) {
        String id = getKeyboardId(xml, mode, mPortrait, mNumbersTop);
        if (!mKeyboardCache.containsKey(id)) {
            // Check if arrows must be displayed
            final boolean showArrows = (mArrowsMain == ARROWS_MAIN_ALWAYS) ||
                (mPortrait && (mArrowsMain == ARROWS_MAIN_PORTRAIT))
                || (!mPortrait && (mArrowsMain == ARROWS_MAIN_LANDSCAPE));
            final boolean showNumbers = (mNumbersTop == ARROWS_MAIN_ALWAYS) ||
                    (mPortrait && (mNumbersTop == ARROWS_MAIN_PORTRAIT))
                    || (!mPortrait && (mNumbersTop == ARROWS_MAIN_LANDSCAPE));
            Keyboard kbd;
            if (xml == R.xml.emoji || xml == R.xml.emoji_lang) {
                kbd = new EmojiKeyboard(context, xml, mode, mPortrait, showArrows, showNumbers,
                        mEmojiCategories);
            } else {
                kbd = new Keyboard(context, xml, mode, mPortrait, showArrows, showNumbers,
                        mEmojiCategories);
            }
            mKeyboardCache.put(id, kbd);
            return kbd;
        }
        else {
            return mKeyboardCache.get(id);
        }
    }

    private String getKeyboardId(int xml, int mode, boolean portrait, int numbersTop) {
        return Integer.toString(xml) + "." + Integer.toString(mode) + "." + (portrait ? "P" : "L") + Integer.toString(numbersTop);
    }

    Keyboard getEmoji(Context context, int emojiIndex, boolean showLangKey) {
        // For Emoji, use the emoji index as mode
        if (mEmojiCategories == null) {
            mEmojiCategories = new EmojiCategories(context);
        }
        int xml = showLangKey ? R.xml.emoji_lang : R.xml.emoji;
        Keyboard kbd = getCachedKeyboard(xml, emojiIndex, context);
        kbd.setLanguage("EM");
        return kbd;
    }


    public String getLatinLayout() {
        return mLatinLayout;
    }

    public void setLatinLayout(String mLatinLayout) {
        this.mLatinLayout = mLatinLayout;
    }
}
