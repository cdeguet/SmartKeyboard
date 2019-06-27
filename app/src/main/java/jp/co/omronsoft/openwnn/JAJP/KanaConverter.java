/*
 * Copyright (C) 2008,2009  OMRON SOFTWARE Co., Ltd.
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

package jp.co.omronsoft.openwnn.JAJP;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import jp.co.omronsoft.openwnn.WnnDictionary;
import jp.co.omronsoft.openwnn.WnnPOS;
import jp.co.omronsoft.openwnn.WnnWord;

/**
 * The EISU-KANA converter class for Japanese IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class KanaConverter {

    /** Decimal format using comma */
    private static final DecimalFormat mFormat = new DecimalFormat("###,###");

    /** List of the generated candidates */
    private List<WnnWord> mAddCandidateList;
    /** Work area for generating string */
    private StringBuffer mStringBuff;

    /** part of speech (default) */
    private WnnPOS mPosDefault;
    /** part of speech (number) */
    private WnnPOS mPosNumber;
    /** part of speech (symbol) */
    private WnnPOS mPosSymbol;
    
    /** Conversion rule for full-width Katakana */
    private static final HashMap<String,String> mFullKatakanaMap = new HashMap<String,String>() {{
        put( "\u3042", "\u30a2");
        put( "\u3044", "\u30a4");
        put( "\u3046", "\u30a6");
        put( "\u3048", "\u30a8");
        put( "\u304a", "\u30aa");
        put( "\u3041", "\u30a1");
        put( "\u3043", "\u30a3");
        put( "\u3045", "\u30a5");
        put( "\u3047", "\u30a7");
        put( "\u3049", "\u30a9");
        put( "\u30f4\u3041", "\u30f4\u30a1");
        put( "\u30f4\u3043", "\u30f4\u30a3");
        put( "\u30f4", "\u30f4");
        put( "\u30f4\u3047", "\u30f4\u30a7");
        put( "\u30f4\u3049", "\u30f4\u30a9");
        put( "\u304b", "\u30ab");
        put( "\u304d", "\u30ad");
        put( "\u304f", "\u30af");
        put( "\u3051", "\u30b1");
        put( "\u3053", "\u30b3");
        put( "\u304c", "\u30ac");
        put( "\u304e", "\u30ae");
        put( "\u3050", "\u30b0");
        put( "\u3052", "\u30b2");
        put( "\u3054", "\u30b4");
        put( "\u3055", "\u30b5");
        put( "\u3057", "\u30b7");
        put( "\u3059", "\u30b9");
        put( "\u305b", "\u30bb");
        put( "\u305d", "\u30bd");
        put( "\u3056", "\u30b6");
        put( "\u3058", "\u30b8");
        put( "\u305a", "\u30ba");
        put( "\u305c", "\u30bc");
        put( "\u305e", "\u30be");
        put( "\u305f", "\u30bf");
        put( "\u3061", "\u30c1");
        put( "\u3064", "\u30c4");
        put( "\u3066", "\u30c6");
        put( "\u3068", "\u30c8");
        put( "\u3063", "\u30c3");
        put( "\u3060", "\u30c0");
        put( "\u3062", "\u30c2");
        put( "\u3065", "\u30c5");
        put( "\u3067", "\u30c7");
        put( "\u3069", "\u30c9");
        put( "\u306a", "\u30ca");
        put( "\u306b", "\u30cb");
        put( "\u306c", "\u30cc");
        put( "\u306d", "\u30cd");
        put( "\u306e", "\u30ce");
        put( "\u306f", "\u30cf");
        put( "\u3072", "\u30d2");
        put( "\u3075", "\u30d5");
        put( "\u3078", "\u30d8");
        put( "\u307b", "\u30db");
        put( "\u3070", "\u30d0");
        put( "\u3073", "\u30d3");
        put( "\u3076", "\u30d6");
        put( "\u3079", "\u30d9");
        put( "\u307c", "\u30dc");
        put( "\u3071", "\u30d1");
        put( "\u3074", "\u30d4");
        put( "\u3077", "\u30d7");
        put( "\u307a", "\u30da");
        put( "\u307d", "\u30dd");
        put( "\u307e", "\u30de");
        put( "\u307f", "\u30df");
        put( "\u3080", "\u30e0");
        put( "\u3081", "\u30e1");
        put( "\u3082", "\u30e2");
        put( "\u3084", "\u30e4");
        put( "\u3086", "\u30e6");
        put( "\u3088", "\u30e8");
        put( "\u3083", "\u30e3");
        put( "\u3085", "\u30e5");
        put( "\u3087", "\u30e7");
        put( "\u3089", "\u30e9");
        put( "\u308a", "\u30ea");
        put( "\u308b", "\u30eb");
        put( "\u308c", "\u30ec");
        put( "\u308d", "\u30ed");
        put( "\u308f", "\u30ef");
        put( "\u3092", "\u30f2");
        put( "\u3093", "\u30f3");
        put( "\u308e", "\u30ee");
        put( "\u30fc", "\u30fc");
    }};

    
    /**
     * Constructor
     */
    public KanaConverter() {
        mAddCandidateList = new ArrayList<WnnWord>();
        mStringBuff = new StringBuffer();
    }

    /**
     * Set The dictionary.
     * <br>
     * {@link KanaConverter} gets part-of-speech tags from the dictionary.
     * 
     * @param dict  The dictionary
     */
    public void setDictionary(WnnDictionary dict) {
        /* get part of speech tags */
        mPosDefault  = dict.getPOS(WnnDictionary.POS_TYPE_MEISI);
        mPosNumber   = dict.getPOS(WnnDictionary.POS_TYPE_SUUJI);
        mPosSymbol   = dict.getPOS(WnnDictionary.POS_TYPE_KIGOU);
    }

    /**
     * Create the pseudo candidate list
     * <br>
     * @param inputHiragana     The input string (Hiragana)
     * @param inputRomaji       The input string (Romaji)
     * @param keyBoardMode      The mode of keyboard
     * @return                  The candidate list
     */
    public List<WnnWord> createPseudoCandidateList(String inputHiragana, String inputRomaji, int keyBoardMode) {
        List<WnnWord> list = mAddCandidateList;

        list.clear();
        if (inputHiragana.length() == 0) {
        	return list;
        }

        /* Create pseudo candidates for all keyboard type */
        /* Hiragana(reading) / Full width katakana / Half width katakana */
        list.add(new WnnWord(inputHiragana, inputHiragana));
        if (createCandidateString(inputHiragana, mFullKatakanaMap, mStringBuff)) {
            list.add(new WnnWord(mStringBuff.toString(), inputHiragana, mPosDefault));
        }
        /*if (createCandidateString(inputHiragana, mHalfKatakanaMap, mStringBuff)) {
            list.add(new WnnWord(mStringBuff.toString(), inputHiragana, mPosDefault));
        }*/

        if (keyBoardMode == OpenWnnEngineJAJP.KEYBOARD_QWERTY) {
            /* Create pseudo candidates for Qwerty keyboard */
            createPseudoCandidateListForQwerty(inputHiragana, inputRomaji);
        }
        return list;
    }

    /**
     * Create the pseudo candidate list for Qwerty keyboard
     * <br>
     * @param inputHiragana     The input string (Hiragana)
     * @param inputRomaji       The input string (Romaji)
     */
    private void createPseudoCandidateListForQwerty(String inputHiragana, String inputRomaji) {
        List<WnnWord> list = mAddCandidateList;

        /* Create pseudo candidates for half width alphabet */
        //String convHanEijiLower = inputRomaji.toLowerCase();
        list.add(new WnnWord(inputRomaji, inputHiragana, mPosDefault));
        //list.add(new WnnWord(convHanEijiLower, inputHiragana, mPosSymbol));
        //list.add(new WnnWord(convertCaps(convHanEijiLower), inputHiragana, mPosSymbol));
        //list.add(new WnnWord(inputRomaji.toUpperCase(), inputHiragana, mPosSymbol));

        /* Create pseudo candidates for the full width alphabet */
     /*   if (createCandidateString(inputRomaji, mFullAlphabetMapQwety, mStringBuff)) {
            String convZenEiji = mStringBuff.toString();
            String convZenEijiLower = convZenEiji.toLowerCase(Locale.JAPAN);
            list.add(new WnnWord(convZenEiji, inputHiragana, mPosSymbol));
            list.add(new WnnWord(convZenEijiLower, inputHiragana, mPosSymbol));
            list.add(new WnnWord(convertCaps(convZenEijiLower), inputHiragana, mPosSymbol));
            list.add(new WnnWord(convZenEiji.toUpperCase(Locale.JAPAN), inputHiragana, mPosSymbol));
        }*/
    }

    /**
     * Create the candidate string
     * <br>
     * @param input     The input string
     * @param map       The hash map
     * @param outBuf    The output string
     * @return          {@code true} if success
     */
    private boolean createCandidateString(String input, HashMap<String,String> map, StringBuffer outBuf) {
        if (outBuf.length() > 0) {
            outBuf.delete(0, outBuf.length());
        }
        for (int index = 0; index < input.length(); index++) {
            String convChar = map.get(input.substring(index, index + 1));
            if (convChar == null) {
                return false;
            }
            outBuf.append(convChar);
        }
        return true;
    }

}
