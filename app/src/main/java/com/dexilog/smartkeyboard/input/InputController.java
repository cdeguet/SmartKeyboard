package com.dexilog.smartkeyboard.input;

import android.view.inputmethod.InputConnection;

import com.dexilog.smartkeyboard.utils.EditingUtil;
import com.dexilog.smartkeyboard.utils.TextUtils;

public class InputController {

    static final public String wordSeparators = ".\u0020\u00a0,;:!?\n()[]*&@{}/<>_+=|\"\u3002\u3001\u3000\u060c\u061fã€Žã€ï½›ï½ï¼ˆï¼‰ã€Œã€ï¼šï¼›ï¼»ï¼½ï¼ï¼Ÿï½žï¼Šâ€»â™ªâ™¬â€¦ï¼¿ãƒ»â€¢â—¦ã€ã€‘â˜†â˜…â™¥";
    static final public String sentenceSeparators = ".,;:!?\u060c\u061f\u3002\u3001ï¼šï¼ï¼Ÿâ€¦";
    static final int KEYCODE_SPACE = ' ';
    static final int KEYCODE_PERIOD = '.';
    static final int KEYCODE_NON_BREAKABLE_SPACE = '\u00a0';

    private final InputConnectionProvider inputConnectionProvider;
    private boolean apostropheSeparator;
    private WordComposer wordComposer = new WordComposerImpl();
    // Keep track of the last selection range to decide if we need to show word
    // alternatives
    private int lastSelectionStart;
    private int lastSelectionEnd;
    private boolean predicting;

    public InputController(InputConnectionProvider inputConnectionProvider, boolean apostropheSeparator) {
        this.inputConnectionProvider = inputConnectionProvider;
        this.apostropheSeparator = apostropheSeparator;
    }

    public void addCharacterWithComposing(int primaryCode, int[] keyCodes, boolean replace, boolean isShifted) {
        wordComposer.addCharacter(primaryCode, keyCodes, replace, isShifted);
        InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic != null) {
            setComposingText(ic, false);
        }
    }

    public void addCharacterWithoutComposing(int primaryCode, boolean replace, boolean hardKbd) {
        final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }
        if (replace) {
            ic.deleteSurroundingText(1, 0);
        }
        if (hardKbd) {
            // With the hard keyboard, directly send the number, to handle
            // alt+char correctly
            if (ic != null) {
                ic.commitText(String.valueOf((char) primaryCode), 1);
            }
        } else {
            sendKeyCodePoint(primaryCode);
        }
        if (ic != null) {
            ic.endBatchEdit();
        }
    }

    public void setComposingText(InputConnection ic, boolean force) {
        if (ic == null)
            return;

        // For T9, display the best candidate in updateSuggestions()
        if (!force && inputConnectionProvider.isT9PredictionOn()) {
            return;
        }
        wordComposer.convertWord(inputConnectionProvider.getConverter());
        final CharSequence convertedWord = wordComposer.getConvertedWord();
        inputConnectionProvider.setConvertedComposing(convertedWord);
        ic.setComposingText(convertedWord, 1);
    }

    private void sendKeyCodePoint(final int keyCode) {
        if (keyCode <= 0xffff) {
            inputConnectionProvider.sendKeyChar((char) keyCode);
        } else {
            final String text = new String(new int[] { keyCode }, 0, 1);
            final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
            ic.commitText(text, text.length());
        }
    }

    public WordComposer getCurrentWordComposer() {
        return wordComposer;
    }

    public boolean getPredicting() {
        return predicting;
    }

    public void setPredicting(boolean predicting) {
        this.predicting = predicting;
    }

    public int getLastSelectionStart() {
        return lastSelectionStart;
    }

    public void setLastSelectionStart(int lastSelectionStart) {
        this.lastSelectionStart = lastSelectionStart;
    }

    public int getLastSelectionEnd() {
        return lastSelectionEnd;
    }

    public void setLastSelectionEnd(int lastSelectionEnd) {
        this.lastSelectionEnd = lastSelectionEnd;
    }

    public String getWordSeparators() {
        return wordSeparators;
    }

    public boolean isWordSeparator(int code) {
        if (code == '\'') {
            return apostropheSeparator;
        } else {
            return stringContains(wordSeparators, (char) code);
        }
    }

    public boolean isSentenceSeparator(int code) {
        return stringContains(sentenceSeparators, (char) code);
    }

    // Faster implementation of String.contains (without allocation)
    static private boolean stringContains(CharSequence string, char code) {
        final int len = string.length();
        for (int i = 0; i < len; i++) {
            final char c = string.charAt(i);
            if (c == code) {
                return true;
            }
        }
        return false;
    }

    public void resetWordComposer(WordComposerImpl foundWord) {
        if (foundWord != null) {
            wordComposer = new WordComposerImpl(foundWord);
        } else {
            wordComposer.reset();
        }
    }

    public void insertPeriodOnDoubleSpace() {
        // if (mCorrectionMode == Suggest.CORRECTION_NONE) return;
        final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic == null)
            return;
        final CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
        if (lastThree != null && lastThree.length() == 3
                && Character.isLetterOrDigit(lastThree.charAt(0))
                && isSpace(lastThree.charAt(1))
                && isSpace(lastThree.charAt(2))) {
            ic.beginBatchEdit();
            ic.deleteSurroundingText(2, 0);
            ic.commitText(". ", 1);
            ic.endBatchEdit();
            inputConnectionProvider.updateShiftKeyStateFromEditorInfo();
        }
    }

    private boolean isSpace(char character) {
        return character == KEYCODE_SPACE || character == KEYCODE_NON_BREAKABLE_SPACE;
    }

    public boolean isCursorAtEnd() {
        final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic == null)
            return false;
        final CharSequence toRight = ic.getTextAfterCursor(1, 0);
        return (toRight == null || toRight.length() == 0);
    }

    public boolean preferCapitalization() {
        WordComposer word = getCurrentWordComposer();
        return word.isCapitalized();
    }

    public void deleteSelectedWord(InputConnection ic) {
        if (lastSelectionStart < lastSelectionEnd) {
            ic.setSelection(lastSelectionStart, lastSelectionStart);
        }
        EditingUtil.deleteWordAtCursor(ic, getWordSeparators());
    }

    public void commitPickedSuggestion(CharSequence suggestion, boolean correcting) {
        final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic != null) {
            // If text is in correction mode and we're not using composing
            // text to underline, then the word at the cursor position needs
            // to be removed before committing the correction
            if (correcting) {
                deleteSelectedWord(ic);
            }

            ic.commitText(suggestion, 1);
        }
    }

    public void deleteLastPredictingCharacter(InputConnection ic) {
        WordComposer wordComposer = getCurrentWordComposer();
        final int length = wordComposer.size();
        if (length > 0) {
            wordComposer.deleteLast();
            // Update the composing now in T9 only if the whole word has been deleted
            boolean forceUpdate = (length == 1);
            setComposingText(ic, forceUpdate);
            if (wordComposer.size() == 0) {
                predicting = false;
            }
            inputConnectionProvider.postUpdateSuggestions();
        } else {
            ic.deleteSurroundingText(1, 0);
        }
    }

    public void deleteLastCharacters(InputConnection ic, int toDelete) {
        final CharSequence toTheLeft = ic.getTextBeforeCursor(toDelete, 0);
        if (toTheLeft != null && toTheLeft.length() > 0
                && isWordSeparator(toTheLeft.charAt(0))) {
            toDelete--;
        }
        ic.deleteSurroundingText(toDelete, 0);
    }

    public void forceTypedWord(CharSequence word) {
        wordComposer.forceTypedWord(word);
    }

    public boolean isLastSelectionEmpty() {
        return lastSelectionStart == lastSelectionEnd;
    }

    public boolean isCursorTouchingWord() {
        final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic == null)
            return false;
        final CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
        if (!TextUtils.isEmpty(toLeft) && !isWordSeparator(toLeft.charAt(0))) {
            return true;
        }
        final CharSequence toRight = ic.getTextAfterCursor(1, 0);
        if (!TextUtils.isEmpty(toRight) && !isWordSeparator(toRight.charAt(0))) {
            return true;
        }
        return false;
    }

    public boolean isCursorInsideWord() {
        final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic == null)
            return false;
        final CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
        final CharSequence toRight = ic.getTextAfterCursor(1, 0);
        if (TextUtils.isEmpty(toLeft) || isWordSeparator(toLeft.charAt(0))) {
            return false;
        }
        if (TextUtils.isEmpty(toRight) || isWordSeparator(toRight.charAt(0))) {
            return false;
        }
        return true;
    }

    public void reswapPeriodAndSpace() {
        final InputConnection ic = inputConnectionProvider.getCurrentInputConnection();
        if (ic == null)
            return;
        CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
        if (lastThree != null && lastThree.length() == 3
                && lastThree.charAt(0) == KEYCODE_PERIOD
                && lastThree.charAt(1) == KEYCODE_SPACE
                && lastThree.charAt(2) == KEYCODE_PERIOD) {
            ic.beginBatchEdit();
            ic.deleteSurroundingText(3, 0);
            ic.commitText(" ..", 1);
            ic.endBatchEdit();
            inputConnectionProvider.updateShiftKeyStateFromEditorInfo();
        }
    }
}