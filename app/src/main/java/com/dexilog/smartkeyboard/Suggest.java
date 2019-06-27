/*
 * Copyright (C) 2008 The Android Open Source Project
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
 *
 */

package com.dexilog.smartkeyboard;

import android.util.Log;

import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.lang.Converter;
import com.dexilog.smartkeyboard.lang.Korean;
import com.dexilog.smartkeyboard.suggest.AutoText;
import com.dexilog.smartkeyboard.suggest.Dictionary;
import com.dexilog.smartkeyboard.suggest.DictionaryFactory;
import com.dexilog.smartkeyboard.suggest.SmartDictionary;
import com.dexilog.smartkeyboard.suggest.UserDictionary;
import com.dexilog.smartkeyboard.utils.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class loads a dictionary and provides a list of suggestions for a given sequence of 
 * characters. This includes corrections and completions.
 * @hide pending API Council Approval
 */
public class Suggest implements Dictionary.WordCallback {

	private static final String TAG = "SmartKeyboard";
	private static final boolean DEBUG = false;
	
    public static final int CORRECTION_NONE = 0;
    public static final int CORRECTION_BASIC = 1;
    public static final int CORRECTION_FULL = 2;

    private DictionaryFactory mFactory;
    public Dictionary mMainDict;
    private UserDictionary mUserDictionary;
    private Dictionary mAutoDictionary;
    private Dictionary mContactsDictionary;
    private AutoTextDictionary mAutoTextDictionary;
    private AutoText mAutoText;
    private int mPrefMaxSuggestions = 12;

    private int[] mPriorities = new int[mPrefMaxSuggestions];
    // Handle predictive correction for only the first 1280 characters for performance reasons
    // If we support scripts that need latin characters beyond that, we should probably use some
    // kind of a sparse array or language specific list with a mapping lookup table.
    // 1280 is the size of the BASE_CHARS array in ExpandableDictionary, which is a basic set of
    // latin characters.
    private int[] mNextLettersFrequencies = new int[1280];
    private ArrayList<CharSequence> mSuggestions = new ArrayList<CharSequence>();
    //private boolean mIncludeTypedWordIfValid;
    private ArrayList<CharSequence> mStringPool = new ArrayList<CharSequence>();
    private boolean mHaveCorrection;
    private CharSequence mOriginalWord;
    private String mLowerOriginalWord;
    private boolean mAutoTextFound;
    private int mCorrectionMode = CORRECTION_BASIC;
    private String mCurLang;
    private SmartDictionary mSmartDictionary;
    private int mTypedWordFreq;
    private int mBestWordFreq;
    private int mBestLenMatchingWordFreq; // best frequency with same length
    private boolean mUseSmartDic = false;
    private boolean mT9LengthPriority = true;
    private boolean mModeT9 = false;
    private int mTypedLength = 0;
    private Converter mConverter;
    private boolean mIsChinese;
    private boolean mIsJapanese;
    private boolean mNoEnglishDic = false;
    private AutoTextCallback mAutoTextCB = new AutoTextCallback();

    // TODO: Remove these member variables by passing more context to addWord() callback method
    private boolean mIsFirstCharCapitalized;
    private boolean mIsAllUpperCase;
    
    public Suggest(DictionaryFactory dictionaryFactory) {
        mFactory = dictionaryFactory;
        for (int i = 0; i < mPrefMaxSuggestions; i++) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }
    }
    
    public void setT9LengthPriority(boolean t9LengthPriority) {
    	mT9LengthPriority = t9LengthPriority;
    }
    
    public void useSmartDictionary(boolean useSmartDic) {
    	mUseSmartDic = useSmartDic;
    }
    
    public void addUserWord(String word) {
    	if (mUserDictionary == null) return;
    	if (!mUserDictionary.isValidWord(word)) {
    		mUserDictionary.addWord(word, 128);
    	}
    }
    
    public void tryReloadDic() {
    	if (mMainDict == null) {
    		loadMainDict(mCurLang);
    	}
    }
    
    public void loadDict(String lang) {
    	if ((mCurLang == null || !mCurLang.equals(lang))) {
            if (!lang.equals("EM")) {
                try {
                    loadMainDict(lang);
                    mUserDictionary = mFactory.getUserDictionary(lang);
                    mAutoText = mFactory.getAutoText(lang);
                    mSmartDictionary = mFactory.getSmartDictionary(lang);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
    		mCurLang = lang;
    		mIsChinese = lang.equals("ZH");
    		mIsJapanese = lang.equals("JP");
    		int maxSuggestions = (mIsChinese || mIsJapanese) ? 500 : 12;
    		setMaxSuggestions(maxSuggestions);
		}
    }
    
    private void loadMainDict(String lang) {
    	try {
    		mMainDict = mFactory.getLangDictionary(lang);
    		if (mMainDict == null) {
    			if (DEBUG) Log.i(TAG, "No dictionary for " + lang);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		mMainDict = null;
    	}
    	if (lang.equals("EN")) {
    		mNoEnglishDic = (mMainDict == null);
    	}
    }
    
    public boolean hasNoEnglishDic() {
    	return mNoEnglishDic;
    }

    public int getCorrectionMode() {
        return mCorrectionMode;
    }

    public void setCorrectionMode(int mode) {
        mCorrectionMode = mode;
    }
    

    /**
     * Sets an optional contacts dictionary resource to be loaded.
     */
    public void setContactsDictionary(Dictionary userDictionary) {
        mContactsDictionary = userDictionary;
    }
    
    public void setAutoDictionary(Dictionary autoDictionary) {
        mAutoDictionary = autoDictionary;
    }
    
    
    public void setAutoTextDictionary(AutoTextDictionary autoTextDictionary) {
        mAutoTextDictionary = autoTextDictionary;
    }

    /**
     * Number of suggestions to generate from the input key sequence. This has
     * to be a number between 1 and 100 (inclusive).
     * @param maxSuggestions
     * @throws IllegalArgumentException if the number is out of range
     */
    public void setMaxSuggestions(int maxSuggestions) {
      /*  if (maxSuggestions < 1 || maxSuggestions > 100) {
            throw new IllegalArgumentException("maxSuggestions must be between 1 and 100");
        }*/
        if (mPrefMaxSuggestions == maxSuggestions) {
        	// Nothing to do
        	return;
        }
        mPrefMaxSuggestions = maxSuggestions;
        mPriorities = new int[mPrefMaxSuggestions];
        collectGarbage();
        while (mStringPool.size() < mPrefMaxSuggestions) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }
    }

    private boolean haveSufficientCommonality(String original, CharSequence suggestion) {
        final int originalLength = original.length();
        final int suggestionLength = suggestion.length();
        final int minLength = Math.min(originalLength, suggestionLength);
        if (minLength <= 2) return true;
        int matching = 0;
        int lessMatching = 0; // Count matches if we skip one character
        int i;
        for (i = 0; i < minLength; i++) {
            final char origChar = ExpandableDictionary.toLowerCase(original.charAt(i));
            if (origChar == ExpandableDictionary.toLowerCase(suggestion.charAt(i))) {
                matching++;
                lessMatching++;
            } else if (i + 1 < suggestionLength
                    && origChar == ExpandableDictionary.toLowerCase(suggestion.charAt(i + 1))) {
                lessMatching++;
            }
        }
        matching = Math.max(matching, lessMatching);

        if (minLength <= 4) {
            return matching >= 2;
        } else {
            return matching > minLength / 2;
        }
    }

    /**
     * Returns a list of words that match the list of character codes passed in.
     * This list will be overwritten the next time this function is called.
     * @param a view for retrieving the context for AutoText
     * @param codes the list of codes. Each list item contains an array of character codes
     * in order of probability where the character at index 0 in the array has the highest
     * probability.
     * @return list of suggestions.
     */
    public List<CharSequence> getSuggestions(WordComposer wordComposer, boolean modeT9,
                                             boolean isT9prediction, Converter converter) {
        mHaveCorrection = false;
        mIsFirstCharCapitalized = wordComposer.isCapitalized();
        mIsAllUpperCase = wordComposer.isAllUpperCase();
        collectGarbage();
        Arrays.fill(mPriorities, 0);
        mTypedWordFreq = 0;
        mBestWordFreq = 0;
        mBestLenMatchingWordFreq = 0;
        mModeT9 = modeT9;
        WordComposer origWordComposer = wordComposer;
        if (converter instanceof Korean) {
        	mConverter = converter;
        	// In Korean T9, pre-process the vowels
        	if (modeT9) {
        		wordComposer = ((Korean)converter).convertT9Vowels(wordComposer);
        	}
        } else {
        	mConverter = null;
        }
        //mIncludeTypedWordIfValid = includeTypedWordIfValid;
        
        Arrays.fill(mNextLettersFrequencies, 0);

        mOriginalWord = origWordComposer.getConvertedWord();
        mTypedLength = mOriginalWord.length();
        if (mOriginalWord != null) {
            mOriginalWord = mOriginalWord.toString();
            mLowerOriginalWord = mOriginalWord.toString().toLowerCase();
        } else {
            mLowerOriginalWord = "";
        }

        // Search the dictionary only if there are at least 2 characters
        final int wordSize = wordComposer.size();
        if (wordSize > 1 || mIsChinese) {
            if (mUserDictionary != null || mContactsDictionary != null) {
                if (mUserDictionary != null) {
                    mUserDictionary.getWords(wordComposer, this, modeT9, mNextLettersFrequencies);
                }
                if (mContactsDictionary != null) {
                    mContactsDictionary.getWords(wordComposer, this, modeT9, mNextLettersFrequencies);
                }

                if (mSuggestions.size() > 0 && isValidWord(mOriginalWord, false, false)) {
                    mHaveCorrection = true;
                }
            }
            if (mMainDict != null) {
            	mMainDict.getWords(wordComposer, this, modeT9, mNextLettersFrequencies);
            }
            if (mCorrectionMode == CORRECTION_FULL && mSuggestions.size() > 0) {
                mHaveCorrection = true;
            }
        }
        boolean singleLetterT9 = false;
        if (wordSize == 1 && modeT9) {
            singleLetterT9 = handleT9SingleLetterWords(wordComposer, isT9prediction);
        } else if (mOriginalWord != null) {
        	mSuggestions.add(0, mOriginalWord.toString());
        }
        
        // Check if the first suggestion has a minimum number of characters in common
        if (!modeT9 && mCorrectionMode == CORRECTION_FULL && mSuggestions.size() > 1) {
            if (!haveSufficientCommonality(mLowerOriginalWord, mSuggestions.get(1)) && !mAutoTextFound) {
                mHaveCorrection = false;
            }
        }

        addAutoTextSuggestions(modeT9, singleLetterT9);
        
        // Add custom autotext
        mAutoTextFound = false;
        if (mAutoTextDictionary != null) {
            mAutoTextFound = mAutoTextDictionary.getWords(mLowerOriginalWord, mAutoTextCB);
        }
        
        if (mIsJapanese || mIsChinese) {
        	// Move typed word at the end of the list
        	mSuggestions.add(mSuggestions.remove(0));
        }

        if (!mIsChinese) {
        	removeDupes();
        }
        return mSuggestions;
    }

    private boolean handleT9SingleLetterWords(WordComposer wordComposer, boolean isT9prediction) {
        boolean singleLetterT9;
        singleLetterT9 = true;
        // Add all possible letters for T9
        final int[] codes = wordComposer.getCodesAt(0);
        for (int i=0; i<codes.length; i++) {
            final int code = codes[i];
            if (code == -1) break;
            mSuggestions.add(i, Character.toString((char)code));
        }
        // Hack to suggest "y" and "o"
        if (isT9prediction) {
            if (codes.length >= 3 && (codes[2] == 'y' || codes[2] == 'o' ||
                    codes[2] == 'в' || codes[2] == 'к' || codes[2] == 'о')) {
                mSuggestions.add(1, mSuggestions.remove(2));
                mHaveCorrection = true;
            } else if (codes.length >= 4 && codes[3] == 'я') {
                // Suggest
                mSuggestions.add(1, mSuggestions.remove(3));
                mHaveCorrection = true;
            } else if (codes.length >= 2 && codes[1] == 'с') {
                mHaveCorrection = true;
            }
        }
        return singleLetterT9;
    }

    private void addAutoTextSuggestions(boolean modeT9, boolean singleLetterT9) {
        int i = 0;
        int max = 6;
        // Don't autotext the suggestions from the dictionaries
        if (mCorrectionMode == CORRECTION_BASIC && !modeT9) max = 1;
        final int[] priorities = mPriorities;
        final int prefMaxSuggestions = mPrefMaxSuggestions;
        while (i < mSuggestions.size() && i < max) {
            String suggestedWord = mSuggestions.get(i).toString().toLowerCase();
            CharSequence autoText = null;
            if (mAutoText != null) {
                autoText = mAutoText.lookup(suggestedWord, 0, suggestedWord.length());
            }
            // Is there an AutoText correction?
            boolean canAdd = autoText != null;
            // Is that correction already the current prediction (or original word)?
            canAdd &= !TextUtils.equals(autoText, mSuggestions.get(i));
            // Is that correction already the next predicted word?
            if (canAdd && i + 1 < mSuggestions.size() && mCorrectionMode != CORRECTION_BASIC) {
                canAdd &= !TextUtils.equals(autoText, mSuggestions.get(i + 1));
            }
            if (canAdd) {
                mHaveCorrection = true;
                int pos = i + 1;
                if (singleLetterT9) {
                	// Hack to suggest "I"
                	pos = 1;
                }
                // Hack for French
                if (autoText.equals("\u00E0")) { // a grave
                	mHaveCorrection = false;
                }
                mSuggestions.add(pos, autoText);
                // TODO: update the autotext priority
                if (pos+1 < prefMaxSuggestions) {
                	System.arraycopy(priorities, pos, priorities, pos + 1,
                			prefMaxSuggestions - pos - 1);
                }
                i++;
            }
            i++;
        }
    }

    // Call after getSuggestions
    public boolean wasAutoTextFound() {
    	return mAutoTextFound;
    }

    public int[] getNextLettersFrequencies() {
        return mNextLettersFrequencies;
    }

    private void removeDupes() {
        final ArrayList<CharSequence> suggestions = mSuggestions;
        if (suggestions.size() < 2) return;
        int i = 1;
        final int[] priorities = mPriorities;
        final int prefMaxSuggestions = mPrefMaxSuggestions;
        // Don't cache suggestions.size(), since we may be removing items
        while (i < suggestions.size()) {
            final CharSequence cur = suggestions.get(i);
            // Compare each candidate with each previous candidate
            for (int j = 0; j < i; j++) {
                CharSequence previous = suggestions.get(j);
                if (TextUtils.equals(cur, previous)) {
                    removeFromSuggestions(i);
                    if (i+1 < prefMaxSuggestions) {
                    	System.arraycopy(priorities, i + 1, priorities, i,
                    			prefMaxSuggestions - i - 1);
                    }
                    i--;
                    break;
                }
            }
            i++;
        }
    }

    private void removeFromSuggestions(int index) {
        CharSequence garbage = mSuggestions.remove(index);
        if (garbage != null && garbage instanceof StringBuilder) {
            mStringPool.add(garbage);
        }
    }

    public boolean hasMinimalCorrection() {
        return mHaveCorrection;
    }

    private boolean compareCaseInsensitive(final String mLowerOriginalWord, 
            final char[] word, final int offset, final int length) {
        final int originalLength = mLowerOriginalWord.length();
        if (originalLength == length && Character.isUpperCase(word[offset])) {
            for (int i = 0; i < originalLength; i++) {
                if (mLowerOriginalWord.charAt(i) != Character.toLowerCase(word[offset+i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean compareLowerWords(final String originalWord,
    		final char[] word, final int offset, final int length) {
        final int originalLength = originalWord.length();
        if (originalLength == length) {
            for (int i = 0; i < originalLength; i++) {
                if (originalWord.charAt(i) != Character.toLowerCase(word[offset+i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    

    public boolean addWord(char[] word, int offset, int length, int freq) {
        int pos = 0;
        final int[] priorities = mPriorities;
        final int prefMaxSuggestions = mPrefMaxSuggestions;
        ArrayList<CharSequence> suggestions = mSuggestions;
        
        int poolSize = mStringPool.size();
        StringBuilder sb = poolSize > 0 ? (StringBuilder) mStringPool.remove(poolSize - 1) 
                : new StringBuilder(32);
        sb.setLength(0);
        StringBuilder origWord = sb;
        if (mConverter != null) {
        	// For korean convert to hangul
        	// TODO use pool
        	StringBuilder orig = new StringBuilder();
        	orig.append(word, offset, length);
        	origWord = orig;
        	mConverter.convert(orig, sb);
        	word = sb.toString().toCharArray();
        	offset = 0;
        	length = sb.length();
        } else if (mIsAllUpperCase) {
            sb.append(new String(word, offset, length).toUpperCase());
        } else if (mIsFirstCharCapitalized) {
            sb.append(Character.toUpperCase(word[offset]));
            if (length > 1) {
                sb.append(word, offset + 1, length - 1);
            }
        } else {
            sb.append(word, offset, length);
        }
        
        // No smart dic for chinese at the moment
        if (mUseSmartDic && !mIsChinese) {
        	int count = mSmartDictionary.getWordCount(origWord.toString().toLowerCase());
        	freq = 1 + freq / (32 * length) + count;
        	if (DEBUG) Log.d(TAG, "freq " + origWord.toString() + ": " + Integer.toString(freq));
        }
        final boolean t9LengthPriority = mModeT9 && mT9LengthPriority;
        
    	if (compareLowerWords(mLowerOriginalWord, word, offset, length)) {
    		mTypedWordFreq = freq;
    	} else {
    		// Update the frequency of the best prediction
			if (t9LengthPriority) {
				if (mTypedLength == length) {
					if (freq > mBestLenMatchingWordFreq) {
						mBestLenMatchingWordFreq = freq;
						mBestWordFreq = freq;
					}
				}
			}
    		if (freq > mBestWordFreq && mBestLenMatchingWordFreq == 0) {
    			mBestWordFreq = freq;
    		}
    	}
        
        // Check if it's the same word, only caps are different
        if (compareCaseInsensitive(mLowerOriginalWord, word, offset, length) && !mUseSmartDic) {
            pos = 0;
        } else 
    	if (t9LengthPriority) {
    		// In this mode give priority to the words with same length as typed
    		final int typedLength = mOriginalWord.length();
			// Don't take apostrophes into account, to handle dont -> don't for instance
    		final int minLen = Math.min(typedLength, length);
    		int lenOffset = 0;
			for (int i=0; i<minLen; i++) {
				if (word[offset+i] == '\'' && mOriginalWord.charAt(i) != '\'') {
					lenOffset++;
				}
			}
    		final boolean hasSameLength = (length-lenOffset == typedLength);
    		while (pos < prefMaxSuggestions) {
    			final int curFreq = priorities[pos];
    			if (curFreq == 0) {
    				break;
    			}
    			
    			CharSequence curSuggestion = suggestions.get(pos);
    			int curLen = curSuggestion.length();
    			// Don't take apostrophes into account, to handle dont -> don't for instance
        		final int minCurLen = Math.min(typedLength, curLen);
    			for (int i=0; i<minCurLen; i++) {
    				if (curSuggestion.charAt(i) == '\'' && mOriginalWord.charAt(i) != '\'') {
    					curLen--;
    				}
    			}
    			
    			if (hasSameLength) {
    				if (curFreq < freq || curLen != typedLength) {
    					break;
    				}
    			} else {
    				if (curLen != typedLength && curFreq < freq) {
    					break;
    				}
    			}
    			pos++;
    		}
    	} else {
            // Check the last one's priority and bail
            if (priorities[prefMaxSuggestions - 1] >= freq) return true;
            while (pos < prefMaxSuggestions) {
                if (priorities[pos] < freq
                        || (priorities[pos] == freq && length < suggestions
                                .get(pos).length())) {
                    break;
                }
                pos++;
            }
        }

        if (pos >= prefMaxSuggestions) {
            return true;
        }
        System.arraycopy(priorities, pos, priorities, pos + 1,
                prefMaxSuggestions - pos - 1);
        priorities[pos] = freq;
        suggestions.add(pos, sb);
        if (DEBUG) Log.d(TAG, "Add suggestion " + sb.toString() + " " + Integer.toString(pos) + " freq=" + Integer.toString(freq));
        if (mSuggestions.size() > prefMaxSuggestions) {
            CharSequence garbage = mSuggestions.remove(prefMaxSuggestions);
            if (garbage instanceof StringBuilder) {
                mStringPool.add(garbage);
            }
        }
        return true;
    }

    public boolean isValidWord(final CharSequence word, boolean checkFrequency, boolean modeT9) {
    	final int wordLength = word.length();
        if (word == null || wordLength == 0) {
            return false;
        }
        boolean isValid = (mMainDict != null && mMainDict.isValidWord(word))
                     || (mAutoDictionary != null && mAutoDictionary.isValidWord(word))
                     || (mContactsDictionary != null && mContactsDictionary.isValidWord(word));
        boolean isUserWordOnly = false;
        if (!isValid) {
        	// Also check in the user dictionary
        	isValid = (mUserDictionary != null) && mUserDictionary.isValidWord(word);
        	isUserWordOnly = true;
        }
        if (isValid && checkFrequency && mSuggestions.size() > 1) {
        	// With T9 or smart dictionary, make sure the typed word has a bigger frequency to pick it
        	if (modeT9) {
            	if ((mTypedWordFreq < mBestWordFreq) &&
            			(!mT9LengthPriority || mSuggestions.get(1).length() == wordLength)) {
            			return false;
        		}
            // Only check typed word frequency if it's a user word
        	} else if (mUseSmartDic && isUserWordOnly) {
        		final int wordCount = mSmartDictionary.getWordCount(word);
        		if (2*mTypedWordFreq < mBestWordFreq && wordCount < 3) {
    				return false;
    			}
        	}
        }
        return isValid;
    }
    
    public void increaseWordCount(String word) {
    	mSmartDictionary.increaseWordCount(word);
    }
    
    private void collectGarbage() {
        int poolSize = mStringPool.size();
        int garbageSize = mSuggestions.size();
        while (poolSize < mPrefMaxSuggestions && garbageSize > 0) {
            CharSequence garbage = mSuggestions.get(garbageSize - 1);
            if (garbage != null && garbage instanceof StringBuilder) {
                mStringPool.add(garbage);
                poolSize++;
            }
            garbageSize--;
        }
        if (poolSize == mPrefMaxSuggestions + 1) {
            Log.w("Suggest", "String pool got too big: " + poolSize);
        }
        mSuggestions.clear();
    }
  
    // Callback for Custom Autotext
    public class AutoTextCallback implements Dictionary.WordCallback {
        public boolean addWord(char[] word, int offset, int length, int freq) {
            int pos = 1;
            final int[] priorities = mPriorities;
            final int prefMaxSuggestions = mPrefMaxSuggestions;
            
            int poolSize = mStringPool.size();
            StringBuilder sb = poolSize > 0 ? (StringBuilder) mStringPool.remove(poolSize - 1) 
                    : new StringBuilder(32);
            sb.setLength(0);
            if (mConverter != null) {
            	// For korean convert to hangul
            	// TODO use pool
            	StringBuilder orig = new StringBuilder();
            	orig.append(word, offset, length);
            	mConverter.convert(orig, sb);
            	word = sb.toString().toCharArray();
            	offset = 0;
            	length = sb.length();
            } else {
            	sb.append(word, offset, length);
            }

            System.arraycopy(priorities, pos, priorities, pos + 1,
                    prefMaxSuggestions - pos - 1);
            priorities[pos] = freq;
            mSuggestions.add(pos, sb);
            if (DEBUG) Log.d(TAG, "Add suggestion " + sb.toString() + " " + Integer.toString(pos) + " freq=" + Integer.toString(freq));
            if (mSuggestions.size() > prefMaxSuggestions) {
                CharSequence garbage = mSuggestions.remove(prefMaxSuggestions);
                if (garbage instanceof StringBuilder) {
                    mStringPool.add(garbage);
                }
            }
            return true;
        }
    }
}
