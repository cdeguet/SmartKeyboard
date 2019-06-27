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

package com.dexilog.smartkeyboard;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputConnection;

import com.dexilog.smartkeyboard.input.InputController;
import com.dexilog.smartkeyboard.input.TextEntryState;
import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.keyboard.Keyboard;
import com.dexilog.smartkeyboard.lang.Korean;
import com.dexilog.smartkeyboard.ui.CandidateView;

import java.util.ArrayList;
import java.util.List;

public class SuggestController {
    private final SmartKeyboard smartKeyboard;
    private final Suggest suggest;
    private final ExpandableDictionary mAutoDictionary;
    final InputController inputController;
    CandidateView mCandidateView;
    AutoTextDictionary mAutoTextDictionary;
    CharSequence mConvertedComposing = null;
    CharSequence mBestWord;
    CharSequence mT9Suggestion;
    public int mCommittedLength;
    public CharSequence mJustRevertedSeparator;
    public boolean mCandidateSelected = false;
    public boolean mJustAccepted;
    public boolean mWaitingForSuggestions = false;

    public SuggestController(SmartKeyboard smartKeyboard, Suggest suggest,
                             InputController inputController,
                             ExpandableDictionary autoDictionary) {
        this.smartKeyboard = smartKeyboard;
        this.inputController = inputController;
        this.suggest = suggest;
        this.mAutoDictionary = autoDictionary;
    }

    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (SmartKeyboard.DEBUG)
            Log.d("KBD", "onDisplayCompletions");
        if (false) {
            Log.i("foo", "Received completions:");
            for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
                Log.i("foo", "  #" + i + ": " + completions[i]);
            }
        }
        if (smartKeyboard.mCompletionOn) {
            smartKeyboard.mCompletions = completions;
            if (completions == null) {
                mCandidateView.setSuggestions(null, false, false, false);
                return;
            }

            List<CharSequence> stringList = new ArrayList<CharSequence>();
            for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if (ci != null)
                    stringList.add(ci.getText());
            }
            // CharSequence typedWord = mWord.getTypedWord();
            mCandidateView.setSuggestions(stringList, true, true, true);
            mBestWord = null;
            smartKeyboard.setCandidatesViewShown(smartKeyboard.isCandidateStripVisible() || smartKeyboard.mCompletionOn);
        }
    }

    public void updateSuggestions() {

        if (SmartKeyboard.DEBUG)
            Log.d("KBD", "updateSuggestion "
                    + Boolean.toString(suggest != null)
                    + Boolean.toString(smartKeyboard.isPredictionOn())
                    + Boolean.toString(smartKeyboard.mCompletionOn));

        mWaitingForSuggestions = false;

        Keyboard kbd = null;
        if (smartKeyboard.mKeyboardSwitcher.getMainKeyboardView() != null)
            kbd = smartKeyboard.mKeyboardSwitcher.getMainKeyboardView().getKeyboard();
        if (kbd != null) {
            kbd.setPreferredLetters(null);
        }

        // Check if we have a suggestion engine attached.
        if (suggest == null || !smartKeyboard.isPredictionOn()) {
            return;
        }

        if (inputController.getPredicting()) {
            if (SmartKeyboard.DEBUG) Log.d(SmartKeyboard.TAG, "setCandidatesViewShown");
            smartKeyboard.setCandidatesViewShown(smartKeyboard.isCandidateStripVisible() || smartKeyboard.mCompletionOn);
            if (mCandidateView == null) {
                Log.e(SmartKeyboard.TAG, "null candidate view!");
                return;
            }
        } else {
            if (SmartKeyboard.DEBUG) Log.d(SmartKeyboard.TAG, "setNextSuggestions");
            setNextSuggestions();
            return;
        }

        if (kbd != null && smartKeyboard.mDynamicResizing && !smartKeyboard.isModeT9()) {
            int[] nextLettersFrequencies = suggest.getNextLettersFrequencies();
            kbd.setPreferredLetters(nextLettersFrequencies);
        }

        final boolean modeT9 = smartKeyboard.isModeT9();
        final boolean isT9Prediction = smartKeyboard.isT9PredictionOn();

        WordComposer wordComposer = inputController.getCurrentWordComposer();
        updateSuggestionsForCurrentWord(modeT9, isT9Prediction, wordComposer);
    }

    private void updateSuggestionsForCurrentWord(boolean modeT9, boolean isT9Prediction,
                                                 WordComposer wordComposer) {
        final List<CharSequence> stringList = suggest.getSuggestions(wordComposer,
                modeT9, isT9Prediction, smartKeyboard.getConverter());

        boolean correctionAvailable = suggest.hasMinimalCorrection()
                && smartKeyboard.mCorrectionMode > 0;
        final CharSequence typedWord = wordComposer.getTypedWord();
        // || mCorrectionMode == mSuggest.CORRECTION_FULL;
        // If we're in basic correct
        boolean typedWordValid = suggest.isValidWord(typedWord, true, modeT9)
                || (inputController.preferCapitalization() && suggest.isValidWord(typedWord
                .toString().toLowerCase(), true, modeT9));
        if (smartKeyboard.mCorrectionMode == Suggest.CORRECTION_FULL) {
            correctionAvailable |= typedWordValid;
        }
        // Don't auto-correct words with multiple capital letter
        correctionAvailable &= !wordComposer.isMostlyCaps();

        if (smartKeyboard.isModeT9()) {
            // Autocorrect if and only if T9 prediction is off
            if (isT9Prediction) {
                // Don't change anything if there is only one letter
                // TODO add one-letter words to the dictionary
                if (wordComposer.size() > 1) {
                    correctionAvailable = true;
                }
            } else {
                correctionAvailable = false;
            }
        }

        // Make sure the custom autotext is selected by default when it's also a
        // valid word
        final boolean autoTextFound = suggest.wasAutoTextFound();
        if (autoTextFound) {
            correctionAvailable = true;
            typedWordValid = mAutoTextDictionary.isTypedWordValid();
        }

        // For japanese
        if (smartKeyboard.mUseSpaceForNextWord) {
            correctionAvailable = false;
        }

        mCandidateView.setSuggestions(stringList, false, typedWordValid,
                correctionAvailable);
        final int listSize = stringList.size();
        if (stringList.size() > 0) {
            if (correctionAvailable && !typedWordValid && listSize > 1) {
                mBestWord = stringList.get(1);
            } else {
                mBestWord = wordComposer.getConvertedWord();
            }
        } else {
            mBestWord = null;
        }

        if (isT9Prediction) {
            displayBestT9Candidate(wordComposer);
        }
    }

    private void displayBestT9Candidate(WordComposer word) {
        // Only keep the right number of letters
        // TODO display the rest as in the HTC keyboard?
        int displayLen = getT9SuggestionLength(word);
        mT9Suggestion = mBestWord.subSequence(0, displayLen);
        smartKeyboard.getCurrentInputConnection().setComposingText(mT9Suggestion, 1);
        mConvertedComposing = mBestWord;
        smartKeyboard.updateShiftKeyStateFromEditorInfo();
    }

    private int getT9SuggestionLength(WordComposer word) {
        final int wordLen = word.size();
        int displayLen = Math.min(wordLen, mBestWord.length());
        // Take apostrophes into account, to handle dont -> don't for
        // instance
        for (int i = 0; i < displayLen; i++) {
            if (mBestWord.charAt(i) == '\''
                    && word.getCodesAt(i)[0] != '\'') {
                displayLen = Math.min(wordLen + 1, mBestWord.length());
                break;
            }
        }
        return displayLen;
    }

    public List<CharSequence> getTypedSuggestions(WordComposer word) {
        final boolean modeT9 = smartKeyboard.isModeT9();
        final boolean isT9Prediction = modeT9 && smartKeyboard.mKeyboardSwitcher.getMainKeyboardView() != null
                && smartKeyboard.mKeyboardSwitcher.getMainKeyboardView().isT9PredictionOn();
        final List<CharSequence> stringList = suggest.getSuggestions(word,
                modeT9, isT9Prediction, smartKeyboard.getConverter());
        return stringList;
    }

    void promoteToUserDictionary(String word, int frequency) {
        if (smartKeyboard.mDebug)
            Log.d(SmartKeyboard.TAG, "promoteToUserDictionary " + word + " "
                    + Integer.toString(frequency));

        if (suggest != null) {
            suggest.addUserWord(word);
        }
    }

    public void setNextSuggestions() {
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(
                    smartKeyboard.mSuggestPunctuation ? smartKeyboard.mSuggestPuncList : null, false,
                    false, false);
        }
    }

    public void handleNextSuggestion(boolean withNextSpace, SmartKeyboard smartKeyboard) {
        mCandidateView.nextSuggestion(withNextSpace);
        if (withNextSpace || !smartKeyboard.isModeT9()
                || (smartKeyboard.mKeyboardSwitcher.getMainKeyboardView() != null &&
                smartKeyboard.mKeyboardSwitcher.getMainKeyboardView().isT9PredictionOn())) {
            // Update composing word
            mBestWord = mCandidateView.getCurrentSuggestion();
            smartKeyboard.getCurrentInputConnection().setComposingText(mBestWord, 1);
            WordComposer wordComposer = inputController.getCurrentWordComposer();
            wordComposer.setPreferredWord(mBestWord);
        }
    }

    void pickSuggestion(CharSequence suggestion, boolean correcting) {
        if (SmartKeyboard.DEBUG) Log.d(SmartKeyboard.TAG, "pickSuggestion " + suggestion.toString());

        suggestion = adjustSuggestionCapitalization(suggestion);
        inputController.commitPickedSuggestion(suggestion, correcting);
        String lowerCase = getLowerCaseWord(suggestion);
        registerPickedSuggestionInDictionaries(suggestion, lowerCase);
        smartKeyboard.resetPredictionStateAfterPickedSuggestion(suggestion);
        // If we just corrected a word, then don't show punctuations, unless it is the last word
        if (!correcting || inputController.isCursorAtEnd()) {
            setNextSuggestions();
        }
        smartKeyboard.updateShiftKeyStateFromEditorInfo();
    }

    private void registerPickedSuggestionInDictionaries(CharSequence suggestion, String lowerCase) {
        final boolean autoTextFound = suggest.wasAutoTextFound();
        // Add the word to the auto dictionary if it's not a known word
        if ((mAutoDictionary.isValidWord(lowerCase) || !suggest.isValidWord(
                lowerCase, false, false))
                && !autoTextFound) {
            mAutoDictionary.addWord(lowerCase, SmartKeyboard.FREQUENCY_FOR_PICKED);
        }
        if (smartKeyboard.mUseSmartDictionary && !autoTextFound) {
            smartKeyboard.increaseWordCount(lowerCase);
        }
        smartKeyboard.saveWordInHistory(suggestion);
    }

    @NonNull
    private String getLowerCaseWord(CharSequence suggestion) {
        String lowerCase;
        if (smartKeyboard.getConverter() instanceof Korean) {
            // Reverse from hangul to jamo if korean
            StringBuilder sb = new StringBuilder(20);
            smartKeyboard.mCurTranslator.reverse(suggestion, sb);
            lowerCase = sb.toString();
        } else {
            // TODO: check if the word has been typed in upper case on purpose
            lowerCase = suggestion.toString().toLowerCase();
        }
        return lowerCase;
    }

    private CharSequence adjustSuggestionCapitalization(CharSequence suggestion) {
        if (smartKeyboard.mCapsLock) {
            suggestion = suggestion.toString().toUpperCase();
        } else if (inputController.preferCapitalization() || smartKeyboard.isKeyboardShifted()) {
            suggestion = capitalizeWord(suggestion);
        }
        return suggestion;
    }

    @NonNull
    private CharSequence capitalizeWord(CharSequence suggestion) {
        suggestion = suggestion.toString().toUpperCase().charAt(0)
                + suggestion.subSequence(1, suggestion.length()).toString();
        return suggestion;
    }

    public void commitTyped(InputConnection inputConnection) {
        if (SmartKeyboard.DEBUG) Log.d(SmartKeyboard.TAG, "commitTyped");
        if (inputController.getPredicting()) {
            inputController.setPredicting(false);
            CharSequence typedWord = mConvertedComposing;
            if (typedWord.length() > 0) {
                if (inputConnection != null) {
                    inputConnection.commitText(typedWord, 1);
                }
                mCommittedLength = mConvertedComposing.length();
                TextEntryState.acceptedTyped(typedWord);
                // TODO: check if the word has been typed in upper case on
                // purpose
                String lowerCase = getLowerCaseWord(typedWord);
                mAutoDictionary.addWord(lowerCase, SmartKeyboard.FREQUENCY_FOR_TYPED);
                if (smartKeyboard.mUseSmartDictionary) {
                    smartKeyboard.increaseWordCount(lowerCase);
                }
            }
            updateSuggestions();
        }
    }

    void switchOffT9Prediction() {
        // Do as if mT9Suggestion had been actually typed
        inputController.forceTypedWord(mT9Suggestion);
    }

    void pickBestSuggestion() {
        if (mBestWord != null && mBestWord.length() > 0) {
            WordComposer word = inputController.getCurrentWordComposer();
            TextEntryState.acceptedDefault(word.getTypedWord(), mBestWord);
            mJustAccepted = true;
            pickSuggestion(mBestWord, false);
        }
    }

    public void pickDefaultSuggestion() {
        // Complete any pending candidate query first
        smartKeyboard.forceUpdateSuggestions();
        pickBestSuggestion();
    }

    public void deleteLastCharacter() {
        boolean deleteChar = false;
        final InputConnection ic = smartKeyboard.getCurrentInputConnection();
        if (ic == null)	return;

        ic.beginBatchEdit();

        if (inputController.getPredicting()) {
            inputController.deleteLastPredictingCharacter(ic);
        } else {
            deleteChar = true;
        }
        TextEntryState.backspace();
        if (TextEntryState.getState() == TextEntryState.State.UNDO_COMMIT) {
            revertLastWord(deleteChar);
            ic.endBatchEdit();
            return;
        } else if (deleteChar) {
            smartKeyboard.sendDeleteChar();
        }
        mJustRevertedSeparator = null;
        mCandidateSelected = false;
        ic.endBatchEdit();
        smartKeyboard.postUpdateShiftKeyState();
    }

    public void revertLastWord(boolean deleteChar) {
        WordComposer word = inputController.getCurrentWordComposer();
        final int length = word.size();
        if (!inputController.getPredicting() && length > 0) {
            revertPredictingWord(deleteChar, word);
            smartKeyboard.postUpdateSuggestions();
        } else {
            smartKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            mJustRevertedSeparator = null;
        }
    }

    private void revertPredictingWord(boolean deleteChar, WordComposer word) {
        final InputConnection ic = smartKeyboard.getCurrentInputConnection();
        inputController.setPredicting(true);
        mJustRevertedSeparator = ic.getTextBeforeCursor(1, 0);
        if (deleteChar) ic.deleteSurroundingText(1, 0);
        inputController.deleteLastCharacters(ic, mCommittedLength);
        mConvertedComposing = getConvertedComposing(word);
        ic.setComposingText(mConvertedComposing, 1);
        TextEntryState.backspace();
    }

    private CharSequence getConvertedComposing(WordComposer word) {
        return (!smartKeyboard.isModeT9() || mT9Suggestion == null) ? word
                .getConvertedWord()
                : mT9Suggestion;
    }

}