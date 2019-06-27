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

package com.dexilog.smartkeyboard.lang;

import java.util.HashMap;

/**
 * The Romaji to Hiragana converter class for Japanese IME.
 *
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
@SuppressWarnings("serial")
public class RomajiKana implements Converter {
    /** HashMap for Romaji-to-Kana conversion (Japanese mode) */
	private static final HashMap<String, String> mRomkanTable = new HashMap<String, String>() {{
        put("la", "\u3041");        put("xa", "\u3041");        put("a", "\u3042");
        put("li", "\u3043");        put("lyi", "\u3043");       put("xi", "\u3043");
        put("xyi", "\u3043");       put("i", "\u3044");         put("yi", "\u3044");
        put("ye", "\u3044\u3047");      put("lu", "\u3045");        put("xu", "\u3045");
        put("u", "\u3046");         put("whu", "\u3046");       put("wu", "\u3046");
        put("wha", "\u3046\u3041");     put("whi", "\u3046\u3043");     put("wi", "\u3046\u3043");
        put("we", "\u3046\u3047");      put("whe", "\u3046\u3047");     put("who", "\u3046\u3049");
        put("le", "\u3047");        put("lye", "\u3047");       put("xe", "\u3047");
        put("xye", "\u3047");       put("e", "\u3048");         put("lo", "\u3049");
        put("xo", "\u3049");        put("o", "\u304a");         put("ca", "\u304b");
        put("ka", "\u304b");        put("ga", "\u304c");        put("ki", "\u304d");
        put("kyi", "\u304d\u3043");     put("kye", "\u304d\u3047");     put("kya", "\u304d\u3083");
        put("kyu", "\u304d\u3085");     put("kyo", "\u304d\u3087");     put("gi", "\u304e");
        put("gyi", "\u304e\u3043");     put("gye", "\u304e\u3047");     put("gya", "\u304e\u3083");
        put("gyu", "\u304e\u3085");     put("gyo", "\u304e\u3087");     put("cu", "\u304f");
        put("ku", "\u304f");        put("qu", "\u304f");        put("kwa", "\u304f\u3041");
        put("qa", "\u304f\u3041");      put("qwa", "\u304f\u3041");     put("qi", "\u304f\u3043");
        put("qwi", "\u304f\u3043");     put("qyi", "\u304f\u3043");     put("qwu", "\u304f\u3045");
        put("qe", "\u304f\u3047");      put("qwe", "\u304f\u3047");     put("qye", "\u304f\u3047");
        put("qo", "\u304f\u3049");      put("qwo", "\u304f\u3049");     put("qya", "\u304f\u3083");
        put("qyu", "\u304f\u3085");     put("qyo", "\u304f\u3087");     put("gu", "\u3050");
        put("gwa", "\u3050\u3041");     put("gwi", "\u3050\u3043");     put("gwu", "\u3050\u3045");
        put("gwe", "\u3050\u3047");     put("gwo", "\u3050\u3049");     put("ke", "\u3051");
        put("ge", "\u3052");        put("co", "\u3053");        put("ko", "\u3053");
        put("go", "\u3054");        put("sa", "\u3055");        put("za", "\u3056");
        put("ci", "\u3057");        put("shi", "\u3057");       put("si", "\u3057");
        put("syi", "\u3057\u3043");     put("she", "\u3057\u3047");     put("sye", "\u3057\u3047");
        put("sha", "\u3057\u3083");     put("sya", "\u3057\u3083");     put("shu", "\u3057\u3085");
        put("syu", "\u3057\u3085");     put("sho", "\u3057\u3087");     put("syo", "\u3057\u3087");
        put("ji", "\u3058");        put("zi", "\u3058");        put("jyi", "\u3058\u3043");
        put("zyi", "\u3058\u3043");     put("je", "\u3058\u3047");      put("jye", "\u3058\u3047");
        put("zye", "\u3058\u3047");     put("ja", "\u3058\u3083");      put("jya", "\u3058\u3083");
        put("zya", "\u3058\u3083");     put("ju", "\u3058\u3085");      put("jyu", "\u3058\u3085");
        put("zyu", "\u3058\u3085");     put("jo", "\u3058\u3087");      put("jyo", "\u3058\u3087");
        put("zyo", "\u3058\u3087");     put("su", "\u3059");        put("swa", "\u3059\u3041");
        put("swi", "\u3059\u3043");     put("swu", "\u3059\u3045");     put("swe", "\u3059\u3047");
        put("swo", "\u3059\u3049");     put("zu", "\u305a");        put("ce", "\u305b");
        put("se", "\u305b");        put("ze", "\u305c");        put("so", "\u305d");
        put("zo", "\u305e");        put("ta", "\u305f");        put("da", "\u3060");
        put("chi", "\u3061");       put("ti", "\u3061");        put("cyi", "\u3061\u3043");
        put("tyi", "\u3061\u3043");     put("che", "\u3061\u3047");     put("cye", "\u3061\u3047");
        put("tye", "\u3061\u3047");     put("cha", "\u3061\u3083");     put("cya", "\u3061\u3083");
        put("tya", "\u3061\u3083");     put("chu", "\u3061\u3085");     put("cyu", "\u3061\u3085");
        put("tyu", "\u3061\u3085");     put("cho", "\u3061\u3087");     put("cyo", "\u3061\u3087");
        put("tyo", "\u3061\u3087");     put("di", "\u3062");        put("dyi", "\u3062\u3043");
        put("dye", "\u3062\u3047");     put("dya", "\u3062\u3083");     put("dyu", "\u3062\u3085");
        put("dyo", "\u3062\u3087");     put("ltsu", "\u3063");      put("ltu", "\u3063");
        put("xtu", "\u3063");       put("", "\u3063");          put("tsu", "\u3064");
        put("tu", "\u3064");        put("tsa", "\u3064\u3041");     put("tsi", "\u3064\u3043");
        put("tse", "\u3064\u3047");     put("tso", "\u3064\u3049");     put("du", "\u3065");
        put("te", "\u3066");        put("thi", "\u3066\u3043");     put("the", "\u3066\u3047");
        put("tha", "\u3066\u3083");     put("thu", "\u3066\u3085");     put("tho", "\u3066\u3087");
        put("de", "\u3067");        put("dhi", "\u3067\u3043");     put("dhe", "\u3067\u3047");
        put("dha", "\u3067\u3083");     put("dhu", "\u3067\u3085");     put("dho", "\u3067\u3087");
        put("to", "\u3068");        put("twa", "\u3068\u3041");     put("twi", "\u3068\u3043");
        put("twu", "\u3068\u3045");     put("twe", "\u3068\u3047");     put("two", "\u3068\u3049");
        put("do", "\u3069");        put("dwa", "\u3069\u3041");     put("dwi", "\u3069\u3043");
        put("dwu", "\u3069\u3045");     put("dwe", "\u3069\u3047");     put("dwo", "\u3069\u3049");
        put("na", "\u306a");        put("ni", "\u306b");        put("nyi", "\u306b\u3043");
        put("nye", "\u306b\u3047");     put("nya", "\u306b\u3083");     put("nyu", "\u306b\u3085");
        put("nyo", "\u306b\u3087");     put("nu", "\u306c");        put("ne", "\u306d");
        put("no", "\u306e");        put("ha", "\u306f");        put("ba", "\u3070");
        put("pa", "\u3071");        put("hi", "\u3072");        put("hyi", "\u3072\u3043");
        put("hye", "\u3072\u3047");     put("hya", "\u3072\u3083");     put("hyu", "\u3072\u3085");
        put("hyo", "\u3072\u3087");     put("bi", "\u3073");        put("byi", "\u3073\u3043");
        put("bye", "\u3073\u3047");     put("bya", "\u3073\u3083");     put("byu", "\u3073\u3085");
        put("byo", "\u3073\u3087");     put("pi", "\u3074");        put("pyi", "\u3074\u3043");
        put("pye", "\u3074\u3047");     put("pya", "\u3074\u3083");     put("pyu", "\u3074\u3085");
        put("pyo", "\u3074\u3087");     put("fu", "\u3075");        put("hu", "\u3075");
        put("fa", "\u3075\u3041");      put("fwa", "\u3075\u3041");     put("fi", "\u3075\u3043");
        put("fwi", "\u3075\u3043");     put("fyi", "\u3075\u3043");     put("fwu", "\u3075\u3045");
        put("fe", "\u3075\u3047");      put("fwe", "\u3075\u3047");     put("fye", "\u3075\u3047");
        put("fo", "\u3075\u3049");      put("fwo", "\u3075\u3049");     put("fya", "\u3075\u3083");
        put("fyu", "\u3075\u3085");     put("fyo", "\u3075\u3087");     put("bu", "\u3076");
        put("pu", "\u3077");        put("he", "\u3078");        put("be", "\u3079");
        put("pe", "\u307a");        put("ho", "\u307b");        put("bo", "\u307c");
        put("po", "\u307d");        put("ma", "\u307e");        put("mi", "\u307f");
        put("myi", "\u307f\u3043");     put("mye", "\u307f\u3047");     put("mya", "\u307f\u3083");
        put("myu", "\u307f\u3085");     put("myo", "\u307f\u3087");     put("mu", "\u3080");
        put("me", "\u3081");        put("mo", "\u3082");        put("lya", "\u3083");
        put("xya", "\u3083");       put("ya", "\u3084");        put("lyu", "\u3085");
        put("xyu", "\u3085");       put("yu", "\u3086");        put("lyo", "\u3087");
        put("xyo", "\u3087");       put("yo", "\u3088");        put("ra", "\u3089");
        put("ri", "\u308a");        put("ryi", "\u308a\u3043");     put("rye", "\u308a\u3047");
        put("rya", "\u308a\u3083");     put("ryu", "\u308a\u3085");     put("ryo", "\u308a\u3087");
        put("ru", "\u308b");        put("re", "\u308c");        put("ro", "\u308d");
        put("lwa", "\u308e");       put("xwa", "\u308e");       put("wa", "\u308f");
        put("wo", "\u3092");        put("xn", "\u3093");
        put("vu", "\u30f4");        put("va", "\u30f4\u3041");      put("vi", "\u30f4\u3043");
        put("vyi", "\u30f4\u3043");     put("ve", "\u30f4\u3047");      put("vye", "\u30f4\u3047");
        put("vo", "\u30f4\u3049");      put("vya", "\u30f4\u3083");     put("vyu", "\u30f4\u3085");
        put("vyo", "\u30f4\u3087"); put("nn", "\u3093");
    }};

    /**
     * Default constructor
     */
    public RomajiKana() {
    }

    public void convert(CharSequence text, StringBuilder output) {
    	final int len = text.length();
    	int i = 0;
    	while (i<len) {
    		final int lookupLen = Math.min(len - i, 3);
    		String kana = null;
    		int j = lookupLen;
    		while (j > 0) {
    			final CharSequence key = text.subSequence(i, i+j);
    			kana = mRomkanTable.get(key);
    			if (kana != null) {
    				break;
    			}
    			j--;
    		}
			// check double consonant
			final int sbLen = output.length();
			if (sbLen>0) {
				char prevChar = output.charAt(sbLen-1);
				if (prevChar == 'n') {
					output.replace(sbLen-1, sbLen, "\u3093");
				} else if (prevChar == text.charAt(i) && prevChar != '\u30FC' && prevChar < '\u3000') {
					output.replace(sbLen-1, sbLen, "\u3063");
				} 
			}
    		if (kana != null) {
    			output.append(kana);
    			i += j;
    		} else {
    			output.append(text.charAt(i));
    			i++;
    		}
    	}
    }
    
	public void reverse(CharSequence text, StringBuilder output) {
		// not implemented!
	}

}
