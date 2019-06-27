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

public class Pinyin implements Converter {
	
	static private final char VOWELS[][] = 		
		{{'a', '\u0101', '\u00E1', '\u01CE', '\u00E0'},
		{'e', '\u0113', '\u00E9', '\u011B', '\u00E8'},
		{'i', '\u012B', '\u00ED', '\u01D0', '\u00EC'},
		{'o', '\u014D', '\u00F3', '\u01D2', '\u00F2'},
		{'u', '\u016b', '\u00fa', '\u01d4', '\u00f9'},
		{'\u00fc', '\u01d6' ,'\u01d8', '\u01da' ,'\u01dc'},
		{'A', '\u0100', '\u00C1', '\u01CD', '\u00C0'},
		{'E', '\u0112', '\u00C9', '\u011A', '\u00C8'},
		{'I', '\u012A', '\u00CD', '\u01CF', '\u00CC'},
		{'O', '\u014C', '\u00D3', '\u01D1', '\u00D2'},
		{'U', '\u016A', '\u00da', '\u01d3', '\u00D9'},
		{'\u00dc', '\u01d5' ,'\u01d7', '\u01d9' ,'\u01db'}};

	private HashMap<Character, Integer> mBaseVowels = new HashMap<Character, Integer>();
	
	public Pinyin() {
		// Fill base vowels map
		for (int i = 0; i < 12; i++) {
			char base = VOWELS[i][0];
			mBaseVowels.put(base, i);
			for (int j = 0; j < 4; j++) {
				mBaseVowels.put(VOWELS[i][j+1], i);
			}
		}
	}
	
	@Override
	public void convert(CharSequence text, StringBuilder output) {
		final int len = text.length();
		char prevVowel = 0;
		boolean prevHasTone = false;
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (isVowel(c)) {
				boolean hasTone = false;
				int tone = 0;
				char lowerC = Character.toLowerCase(c);
				// Check if the vowel takes the tone mark
				if (lowerC == 'a' || lowerC == 'e') {
					// In any case, a or e takes the tone
					hasTone = true;
				} else if (prevVowel == 0) {
					// Previous letter was not a vowel
					if (i+1 == len) {
						// 1st case: last letter of the word -> tone
						hasTone= true;
					} else {
						char nextLower = Character.toLowerCase(text.charAt(i+1));
						// 2nd case: next letter is not a vowel -> tone
						// 3rd case: o takes the tone in "ou"
						if (!isVowel(nextLower) || (lowerC == 'o' && nextLower == 'u')) {
							hasTone = true;
						}
					}
				} else {
					// If previous letter was a vowel and doesn't take the tone, the second vowel takes it
					hasTone = !prevHasTone;
				}
				
				if (hasTone) {
					// Check if there is a tone mark later
					int j = i + 1;
					boolean consonantFound = false;
					while (j < len) {
						char next = text.charAt(j);
						if (isVowel(next)) {
							if (consonantFound) {
								// It means we are already in the next syllable
								break;
							}
						} else {
							// Check if letter is a tone
							int curTone = getTone(next);
							if (curTone != 0) {
								// Keep the last tone mark if several ones are found
								tone = curTone;
							} else {
								// Otherwise, it's a consonant
								consonantFound = true;
							}
						}
						j++;
					}
				}
				
				prevVowel = c;
				prevHasTone = hasTone;

				if (tone != 0) {
					// Output vowel with correct tone
					c = VOWELS[mBaseVowels.get(c)][tone];
				}
				output.append(c);
			} else if (getTone(c) == 0) {
				// Just output consonants
				output.append(c);
				prevVowel = 0;
				prevHasTone = false;
			}
		}
	}

	@Override
	public void reverse(CharSequence text, StringBuilder output) {		
	}
	
	private boolean isVowel(char c) {
		return mBaseVowels.containsKey(c);
	}
 
	private int getTone(char c) {
		if (c >= 711 && c <= 715) {
			switch (c) {
			case 713:
				return 1;
			case 714:
				return 2;
			case 711:
				return 3;
			case 715:
				return 4;
			default:
				return 0;
			}
		}
		return 0;
	}
}
