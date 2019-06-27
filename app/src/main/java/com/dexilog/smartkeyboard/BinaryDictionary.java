/*
 * Copyright (C) 2008-2009 Google Inc.
 * Copyright (C) 2009 Spiros Papadimitriou <spapadim@cs.cmu.edu>
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

import java.io.FileDescriptor;
import java.util.Arrays;


import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.suggest.Dictionary;

/**
 * Implements a static, compacted, binary dictionary of standard words.
 */
public class BinaryDictionary extends Dictionary {
    private static final String TAG = "SmartKeyboard";
    
    public static final int MAX_WORD_LENGTH = 48;
    private static final int MAX_ALTERNATIVES = 16;
    private static final int MAX_WORDS = 16;
    
    private static final int TYPED_LETTER_MULTIPLIER = 2;

    private long mNativeDict;
    private int[] mInputCodes = new int[MAX_WORD_LENGTH * MAX_ALTERNATIVES];
    private char[] mOutputChars = new char[MAX_WORD_LENGTH * MAX_WORDS];
    private int[] mFrequencies = new int[MAX_WORDS];

    
    static {
        try {
            System.loadLibrary("smartkbddict");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Could not load native library nativeim", ule);
        }
    }

    /**
     * Create a dictionary from a raw resource file
     * @param context application context for reading resources
     * @param resId the resource containing the raw binary dictionary
     */
    public BinaryDictionary(AssetFileDescriptor afd) {
        loadDictionary(afd);
    }
    
    private native long openNative(FileDescriptor fd, long offset, long length,
            int typedLetterMultiplier, int fullWordMultiplier);
    private native void closeNative(long dict);
    private native boolean isValidWordNative(long nativeData, char[] word, int wordLength);
    private native int getSuggestionsNative(long dict, int[] inputCodes, int codesSize,
            char[] outputChars, int[] frequencies,
            int maxWordLength, int maxWords, int maxAlternatives, int skipPos, boolean modeT9,
            int[] nextLettersFrequencies, int nextLettersSize);
    
    public native static long openExpandableNative();
    public native static void addWordExpandableNative(long dict, String word, int freq);
    public native static void addCharArrayExpandableNative(long dict, char[] word, int size, int freq);
    public native static int getWordFrequencyExpandableNative(long dict, String word);
    public native static int increaseWordFrequencyExpandableNative(long dict, String word);
    public native static int getSuggestionsExpandableNative(long dict, int[] inputCodes, int codesSize,
            char[] outputChars, int[] frequencies,
            int maxWordLength, int maxWords, int maxAlternatives, int skipPos, boolean modeT9,
            int[] nextLettersFrequencies, int nextLettersSize);
    public native static void closeExpandableNative(long dict);

    private final void loadDictionary(AssetFileDescriptor afd) {
        long startTime = System.currentTimeMillis();
        mNativeDict = openNative(afd.getFileDescriptor(), 
                afd.getStartOffset()+8, afd.getLength()-8,
                TYPED_LETTER_MULTIPLIER, FULL_WORD_FREQ_MULTIPLIER);
        Log.i(TAG, "Loaded dictionary in " + (System.currentTimeMillis() - startTime) + "msec");
    }

    @Override
    public void getWords(final WordComposer codes, final WordCallback callback, boolean modeT9,
                         int[] nextLettersFrequencies) {
        final int codesSize = codes.size();
        // Wont deal with really long words.
        if (codesSize > MAX_WORD_LENGTH - 1) return;
        
        Arrays.fill(mInputCodes, -1);
        for (int i = 0; i < codesSize; i++) {
            int[] alternatives = codes.getCodesAt(i);
            System.arraycopy(alternatives, 0, mInputCodes, i * MAX_ALTERNATIVES,
                    Math.min(alternatives.length, MAX_ALTERNATIVES));
        }
        Arrays.fill(mOutputChars, (char) 0);
        Arrays.fill(mFrequencies, 0);

        int count = getSuggestionsNative(mNativeDict, mInputCodes, codesSize,
                mOutputChars, mFrequencies,
                MAX_WORD_LENGTH, MAX_WORDS, MAX_ALTERNATIVES, -1, modeT9,
                nextLettersFrequencies,
                nextLettersFrequencies != null ? nextLettersFrequencies.length : 0);

        // If there aren't sufficient suggestions, search for words by allowing wild cards at
        // the different character positions. This feature is not ready for prime-time as we need
        // to figure out the best ranking for such words compared to proximity corrections and
        // completions.
        if (count < 5) {
            for (int skip = 0; skip < codesSize; skip++) {
                int tempCount = getSuggestionsNative(mNativeDict, mInputCodes, codesSize,
                        mOutputChars, mFrequencies,
                        MAX_WORD_LENGTH, MAX_WORDS, MAX_ALTERNATIVES, skip, modeT9, null, 0);
                count = Math.max(count, tempCount);
                if (tempCount > 0) break;
            }
        }

        for (int j = 0; j < count; j++) {
            if (mFrequencies[j] < 1) break;
            int start = j * MAX_WORD_LENGTH;
            int len = 0;
            while (mOutputChars[start + len] != 0) {
                len++;
            }
            if (len > 0) {
                callback.addWord(mOutputChars, start, len, mFrequencies[j]);
            }
        }
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        if (word == null) return false;
        char[] chars = word.toString().toCharArray();
        return isValidWordNative(mNativeDict, chars, chars.length);
    }
    
    public synchronized void close() {
        if (mNativeDict != 0) {
            closeNative(mNativeDict);
            mNativeDict = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
