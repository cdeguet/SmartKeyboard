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

public class Telex implements Converter {
	
	// Tones: z(0) f(1) r(2) x(3) s(4) j(5)
	
	static final String[] PAIR_KEYS = {"aw", "aa", "dd", "ee", "oo", "ow", "uw", "Aw", "Aa", 
		"Dd", "Ee", "Oo", "Ow", "Uw", "AW", "AA", "DD", "EE", "OO", "OW", "UW"};
	static final char[] PAIR_VALUES = {259, 226, 273, 234, 244, 417, 432, 258, 194, 272, 202, 
		212, 416, 431, 258, 194, 272, 202, 212, 416, 431};
	static final char[][] VOWELS = {
		{65, 192, 7842, 195, 193, 7840},      // A
		{258, 7856, 7858, 7860, 7854, 7862},
		{194, 7846, 7848, 7850, 7844, 7852},
		{69, 200, 7866, 7868, 201, 7864},     // E
		{202, 7872, 7874, 7876, 7870, 7878},
		{73, 204, 7880, 296, 205, 7882},      // I
		{79, 210, 7886, 213, 211, 7884},      // O
		{212, 7890, 7892, 7894, 7888, 7896},
		{416, 7900, 7902, 7904, 7898, 7906},
		{85, 217, 7910, 360, 218, 7908},      // U
		{431, 7914, 7916, 7918, 7912, 7920},
		{89, 7922, 7926, 7928, 221, 7924},    // Y
		{97, 224, 7843, 227, 225, 7841},      // a
		{259, 7857, 7859, 7861, 7855, 7863},
		{226, 7847, 7849, 7851, 7845, 7853},
		{101, 232, 7867, 7869, 233, 7865},    // e
		{234, 7873, 7875, 7877, 7871, 7879},
		{105, 236, 7881, 297, 237, 7883},     // i
		{111, 242, 7887, 245, 243, 7885},     // o
		{244, 7891, 7893, 7895, 7889, 7897},
		{417, 7901, 7903, 7905, 7899, 7907},
		{117, 249, 7911, 361, 250, 7909},     // u
		{432, 7915, 7917, 7919, 7913, 7921},
		{121, 7923, 7927, 7929, 253, 7925},   // y
	};
	
	private HashMap<Character, Integer> mBaseVowels = new HashMap<Character, Integer>();
	private HashMap<Integer, Character> mPairMap = new HashMap<Integer, Character>();
	
	public Telex() {
		for (int i=0; i<PAIR_KEYS.length; i++) {
			final String key = PAIR_KEYS[i];
			int keyCode = key.codePointAt(0) * 65536 + key.codePointAt(1);
			mPairMap.put(keyCode, PAIR_VALUES[i]);
		}
		// Fill base vowels map
		for (int i = 0; i < 24; i++) {
			char base = VOWELS[i][0];
			mBaseVowels.put(base, i);
			for (int j = 0; j < 5; j++) {
				mBaseVowels.put(VOWELS[i][j+1], i);
			}
		}
	}
	
	@Override
	public void convert(CharSequence text, StringBuilder output) {
		int len = text.length();
		if (len == 0) return;
		char cur = text.charAt(0);
		StringBuilder word = new StringBuilder();
		int tone = 0;
		int curIndex = 0;
		int toneIndex = -1;
		int prevVowelIndex = -1;
		int diactriticVowel = -1;
		// First pass: convert pairs and detect tones
		for (int i = 0; i < len; i++) {
			char next = 0;
			if (i+1 < len) {
				// Get the next character to detect pairs
				next = text.charAt(i+1);
				int pairKey = (int)cur * 65536 + next;
				Character pairValue = mPairMap.get(pairKey);
				if (pairValue != null) {
					cur = pairValue;
					i++;
					if (i+1 < len) {
						next = text.charAt(i+1);
					}
				}
			}
			int curTone = -1;
			Integer baseVowel = mBaseVowels.get(cur);
			if (baseVowel != null) {
				prevVowelIndex = toneIndex;
				toneIndex = curIndex;
				// Check if the vowel has a diacritic
				if (VOWELS[baseVowel][0] > 121) {
					diactriticVowel = curIndex;
				}
			} else if (toneIndex != -1 ){
				// Check tone marks if a vowel was already found
				curTone = getTone(cur);				
			}
			
			if (curTone != -1) {
				tone = curTone;
			} else {
				word.append(cur);
				curIndex++;
			}
			cur = next;
		}
		len = word.length();
		// Put the tone on vowel with a diacritic
		if (diactriticVowel != -1) {
			toneIndex = diactriticVowel;
		} 
		// If the last letter is a vowel, put the tone mark on the previous vowel if any
		else if (toneIndex == len-1 && prevVowelIndex != -1) {
			toneIndex = prevVowelIndex;
		}
		// Second pass, output the word with tones
		for (int i=0; i<len; i++) {
			cur = word.charAt(i);
			if (i == toneIndex) {
				// Get the vowel with the correct tone
				Integer baseVowel = mBaseVowels.get(cur);
				cur = VOWELS[baseVowel][tone];
			}
			output.append(cur);
		}
	}

	@Override
	public void reverse(CharSequence text, StringBuilder output) {		
	}
	
	private int getTone(char c) {
		switch (Character.toLowerCase(c)) {
		case 'z':
			return 0;
		case 'f':
			return 1;
		case 'r':
			return 2;
		case 'x':
			return 3;
		case 's':
			return 4;
		case 'j':
			return 5;
		default:
			return -1;
		}
	}
	
}
