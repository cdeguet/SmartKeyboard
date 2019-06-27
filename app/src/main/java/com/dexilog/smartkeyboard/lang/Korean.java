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

// http://gernot-katzers-spice-pages.com/var/korean_hangul_unicode.html

import com.dexilog.smartkeyboard.input.WordComposer;

public class Korean implements Converter {

    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_LEAD = 1;
    private static final int STATE_VOWEL_1 = 2;
    private static final int STATE_VOWEL_2 = 3;
    private static final int STATE_TAIL = 4;

    private static final int LEAD_CHARS[] =
        {0, 1, 2, 0, 3, 0, 0, 4, 5, 6, 0, 0, 0, 0, 0, 0, 0, 7, 8, 9, 0, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19};

    private static final int TAIL_CHARS[] =
        {0, 1, 2, 3, 4, 5, 6, 7, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
        0, 18, 19, 20, 21, 22, 0, 23, 24, 25, 26, 27};

    private static final int TAIL_L_CHARS[] =
        {9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 11, 0, 12, 0, 0, 0, 0, 0, 13, 14, 15};

    private static final int REVERSE_LEAD[] =
        {1,2,4,7,8,9,17,18,19,21,22,23,24,25,26,27,28,29,30};

    private static final int REVERSE_TAIL[] =
        {1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,20,21,22,23,24,26,27,28,29,30};

    private static class VowelNode {
        public int mVowel = 0;
        public VowelNode[] mChildren = new VowelNode[3];

        public VowelNode() {
        }

        public void add(int[] seq, int vowel) {
            add(seq, vowel, 0);
        }

        public void add(int[] seq, int vowel, int offset) {
            if (offset < seq.length) {
                int index = seq[offset];
                // Create the child node if needed
                VowelNode child = mChildren[index];
                if (child == null) {
                    child = new VowelNode();
                    mChildren[index] = child;
                }
                child.add(seq, vowel, offset+1);
            } else {
                // Terminal node
                mVowel = vowel;
            }
        }
    }

    // For the T9 layout
    static private VowelNode mVowelFSM = new VowelNode();

    static {
        // Initialize the vowel FSM for T9
        mVowelFSM.add(new int[] {0, 2}, 0x3157);
        mVowelFSM.add(new int[] {0, 0, 2}, 0x315B);
        mVowelFSM.add(new int[] {1, 0, 1}, 0x3150);
        mVowelFSM.add(new int[] {1, 0, 0, 1}, 0x3152);
        mVowelFSM.add(new int[] {0, 1, 1}, 0x3154);
        mVowelFSM.add(new int[] {0, 0, 1, 1}, 0x3156);
        mVowelFSM.add(new int[] {0, 1}, 0x3153);
        mVowelFSM.add(new int[] {0, 0, 1}, 0x3155);
        mVowelFSM.add(new int[] {1, 0}, 0x314F);
        mVowelFSM.add(new int[] {1, 0, 0}, 0x3151);
        mVowelFSM.add(new int[] {2, 0}, 0x315C);
        mVowelFSM.add(new int[] {2, 0, 0}, 0x3160);
        mVowelFSM.add(new int[] {2, 0, 0, 1}, 0x315D);
        mVowelFSM.add(new int[] {2, 0, 0, 1, 1}, 0x315E);
    }

    WordComposer mT9WordComposer;

    public Korean(WordComposer t9WordComposer) {
        mT9WordComposer = t9WordComposer;
    }

    // TODO : no copy paste !!!
    public WordComposer convertT9Vowels(WordComposer word) {
        CharSequence text = word.getTypedWord();
        final int len = text.length();
        mT9WordComposer.reset();
        for (int i=0; i<len; i++) {
            int c = text.charAt(i);
            // Convert vowels entered with T9 keyboard thanks to the vowels FSM
            int vowelIndex = getVowelIndex(c);
            if (vowelIndex >= 0) {
                int foundVowel = 0;
                VowelNode vowelNode = mVowelFSM;
                int j = 0;
                while (vowelIndex != -1) {
                    // Try to get a child node
                    vowelNode = vowelNode.mChildren[vowelIndex];
                    if (vowelNode != null) {
                        foundVowel = vowelNode.mVowel;
                    } else {
                        break;
                    }
                    // Next letter
                    j++;
                    if (i+j >= len) {
                        break;
                    }
                    vowelIndex = getVowelIndex(text.charAt(i + j));
                }
                // Found a vowel sequence?
                if (foundVowel != 0) {
                    c = foundVowel;
                    i += j - 1;
                }
            }
            // Yes, that's called a hack
            switch (c) {
            case 0x315d:
                mT9WordComposer.add(0x315C, new int[] {0x315c});
                mT9WordComposer.add(0x3153, new int[] {0x3153});
                break;
            case 0x315e:
                mT9WordComposer.add(0x315C, new int[] {0x315c});
                mT9WordComposer.add(0x3154, new int[] {0x3154});
                break;
            default:
                mT9WordComposer.add(c, new int[] {c});
            }
        }
        return mT9WordComposer;
    }


    public void convert(CharSequence text, StringBuilder output) {
        // Translate a jamo sequence to hangul
        final int len = text.length();
        output.setLength(0);
        int state = STATE_UNKNOWN;
        int lead = 0;
        int vowel = 0;
        int tail = 0;
        int prevLetter = 0;
        int c = 0;

        for (int i=0; i<len; i++) {
            prevLetter = c;
            c = text.charAt(i);

            // Convert vowels entered with T9 keyboard thanks to the vowels FSM
            int vowelIndex = getVowelIndex(c);
            if (vowelIndex >= 0) {
                int foundVowel = 0;
                VowelNode vowelNode = mVowelFSM;
                int j = 0;
                while (vowelIndex != -1) {
                    // Try to get a child node
                    vowelNode = vowelNode.mChildren[vowelIndex];
                    if (vowelNode != null) {
                        foundVowel = vowelNode.mVowel;
                    } else {
                        break;
                    }
                    // Next letter
                    j++;
                    if (i+j >= len) {
                        break;
                    }
                    vowelIndex = getVowelIndex(text.charAt(i + j));
                }
                // Found a vowel sequence?
                if (foundVowel != 0) {
                    c = foundVowel;
                    i += j - 1;
                }
            }

            // Check if korean letter
            if (c >= 0x3130 && c < 0x3164) {
                final boolean isVowel = (c >= 0x314F);
                if (isVowel) {
                    // Adjust to ease computation
                    c = c - 0x314E;  // first vowel - 1
                } else {
                    c = c - 0x3130;  // first consonant - 1
                }

                switch (state) {
                case STATE_UNKNOWN:
                    if (isVowel) {
                        // Isolated vowel, but it may be a compound one
                        lead = 0;
                        vowel = c;
                        state = STATE_VOWEL_1;
                    } else {
                        lead = c;
                        state = STATE_LEAD;
                    }
                    break;
                case STATE_LEAD:
                    if (isVowel) {
                        vowel = c;
                        state = STATE_VOWEL_1;
                    } else {
                        // Previous char was an isolated consonant
                        output.append(combineAll(lead, 0, 0));
                        lead = c;
                    }
                    break;
                case STATE_VOWEL_1:
                    if (isVowel) {
                        // check if vowel can be combined with previous one
                        int combined = combineVowels(vowel, c);
                        if (combined != 0) {
                            // go on...
                            vowel = combined;
                            state = STATE_VOWEL_2;
                        } else {
                            // cannot be combined, so it means the second vowel is isolated
                            output.append(combineAll(lead, vowel, 0));
                            lead = 0;
                            vowel = c;
                            state = STATE_VOWEL_1;
                        }
                    } else {
                        // Start the tail
                        tail = TAIL_CHARS[c];
                        state = STATE_TAIL;
                    }
                    break;
                case STATE_VOWEL_2:
                    if (isVowel) {
                        // Three vowels ? Then output the hangul and start a new one
                        output.append(combineAll(lead, vowel, 0));
                        lead = 0;
                        vowel = c;
                        state = STATE_VOWEL_1;
                    } else {
                        // Start the tail
                        tail = TAIL_CHARS[c];
                        if (tail == 0) {
                            // Invalid tail, restart new hangul
                            output.append(combineAll(lead, vowel, 0));
                            lead = c;
                            vowel = 0;
                            state = STATE_LEAD;
                        } else {
                            state = STATE_TAIL;
                        }
                    }
                    break;
                case STATE_TAIL:
                    if (isVowel) {
                        // Hangul finished: output and restart with previous consonant
                        output.append(combineAll(lead, vowel, 0));
                        lead = prevLetter;
                        vowel = c;
                        tail = 0;
                        state = STATE_VOWEL_1;
                    } else {
                        int combined = 0;
                        // Check if valid consonant for a tail
                        final int newTail = TAIL_CHARS[c];
                        if (newTail != 0)
                        {
                            // try to combine consonants
                            combined = combineTail(tail, newTail);
                        }
                        // Check if next jamo is a vowel (then don't combine the consonants)
                        boolean nextIsVowel = false;
                        if (i+1<len) {
                            final char next = text.charAt(i+1);
                            nextIsVowel = (next >= 0x314F && next < 0x3164 || next == 0x318D);
                        }
                        if (combined != 0 && !nextIsVowel) {
                            // Hangul finished
                            output.append(combineAll(lead, vowel, combined));
                            lead = 0;
                            vowel = 0;
                            tail = 0;
                            state = STATE_UNKNOWN;
                        } else {
                            // Cannot combine: output hangul and start a new one
                            output.append(combineAll(lead, vowel, tail));
                            lead = c;
                            vowel = 0;
                            tail = 0;
                            state = STATE_LEAD;
                        }
                    }
                    break;
                }
            } else {
                // not a korean letter
                // Check if hangul was being composed
                if (state != STATE_UNKNOWN) {
                    output.append(combineAll(lead, vowel, tail));
                    lead = 0;
                    vowel = 0;
                    tail = 0;
                }
                output.append((char)c);
                state = STATE_UNKNOWN;
            }
        }

        // Check if hangul was being composed
        if (state != STATE_UNKNOWN) {
            output.append(combineAll(lead, vowel, tail));
        }
    }

    public void reverse(CharSequence text, StringBuilder output) {
        // Convert hangul to jamo
        final int len = text.length();
        for (int i=0; i<len; i++) {
            int c = text.charAt(i);

            if (c < 40000) {
                output.append((char)c);
            } else {
                int tail = (c - 44032) % 28;
                int vowel = ((c - 44032 - tail) % 588) / 28;
                int lead = 1 + ((c - 44032) / 588);
                output.append((char)(REVERSE_LEAD[lead-1] + 0x3130));
                output.append((char)(vowel + 0x3130 + 31));
                if (tail > 0) {
                    output.append((char)(REVERSE_TAIL[tail-1] + 0x3130));
                }
            }
        }
    }

    private int getVowelIndex(int c) {
        switch (c) {
        case 0x318D:
            return 0;
        case 0x3163:
            return 1;
        case 0x3161:
            return 2;
        default:
            return -1;
        }
    }


    private int combineVowels(int first, int second) {
        // Check for valid combinations
        switch (first) {
        case 9:  // O
            switch (second) {
            case 1:
                return 10; // WA
            case 2:
                return 11; // WAE
            case 21:
                return 12; // OE
            default:
                // Invalid combination
                return 0;
            }
        case 14: // U
            switch (second) {
            case 5:
                return 15; // WEO
            case 6:
                return 16; // WE
            case 21:
                return 17; // WI
            default:
                // Invalid combination
                return 0;
            }
        case 19:
            if (second == 21) {
                return 20; // YI
            } else {
                // Invalid combination
                return 0;
            }
        default:
            // invalid combination
            return 0;
        }
    }


    private int combineTail(int first, int second) {
        switch (first) {
        case 1: // G
            switch (second) {
            case 19:
                return 3; // GS
            default:
                // Invalid combination
                return 0;
            }
        case 4: // N
            switch (second) {
            case 22:
                return 5; // NJ
            case 27:
                return 6; // NH
            default:
                // Invalid combination
                return 0;
            }
        case 8: // L
            return TAIL_L_CHARS[second-1];
        case 17: // B
            if (second == 19) {
                return 18; // BS
            } else {
                // Invalid combination
                return 0;
            }
        default:
            return 0;
        }
    }

    private char combineAll(int lead, int vowel, int tail) {
        if (lead == 0) {
            // Single vowel
            return (char)(0x314E + vowel);
        } else if (vowel == 0) {
            // Single consonant
            return (char)(0x3130 + lead);
        } else {
            return (char)(tail + 28 * (vowel - 1) + 588 * (LEAD_CHARS[lead] - 1) + 44032);
        }
    }
}
