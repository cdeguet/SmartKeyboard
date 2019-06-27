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

package com.dexilog.smartkeyboard.input

import android.view.inputmethod.InputConnection

import com.dexilog.smartkeyboard.utils.EditingUtil
import com.dexilog.smartkeyboard.utils.TextUtils

class InputController(private val inputConnectionProvider: InputConnectionProvider, private val apostropheSeparator: Boolean) {
    var currentWordComposer: WordComposer = WordComposerImpl()
        private set
    // Keep track of the last selection range to decide if we need to show word
    // alternatives
    var lastSelectionStart: Int = 0
    var lastSelectionEnd: Int = 0
    var predicting: Boolean = false

    fun addCharacterWithComposing(primaryCode: Int, keyCodes: IntArray, replace: Boolean, isShifted: Boolean) {
        currentWordComposer.addCharacter(primaryCode, keyCodes, replace, isShifted)
        val ic = inputConnectionProvider.currentInputConnection
        setComposingText(ic, false)
    }

    fun addCharacterWithoutComposing(primaryCode: Int, replace: Boolean, hardKbd: Boolean) {
        val ic = inputConnectionProvider.currentInputConnection ?: return
        ic.beginBatchEdit()
        if (replace) {
            ic.deleteSurroundingText(1, 0)
        }
        if (hardKbd) {
            // With the hard keyboard, directly send the number, to handle
            // alt+char correctly
            ic.commitText(primaryCode.toChar().toString(), 1)
        } else {
            sendKeyCodePoint(primaryCode)
        }
        ic.endBatchEdit()
    }

    fun setComposingText(ic: InputConnection?, force: Boolean) {
        ic ?: return

        // For T9, display the best candidate in updateSuggestions()
        if (!force && inputConnectionProvider.isT9PredictionOn) {
            return
        }
        currentWordComposer.convertWord(inputConnectionProvider.converter)
        val convertedWord = currentWordComposer.convertedWord
        inputConnectionProvider.setConvertedComposing(convertedWord)
        ic.setComposingText(convertedWord, 1)
    }

    private fun sendKeyCodePoint(keyCode: Int) {
        if (keyCode <= 0xffff) {
            inputConnectionProvider.sendKeyChar(keyCode.toChar())
        } else {
            val text = String(intArrayOf(keyCode), 0, 1)
            val ic = inputConnectionProvider.currentInputConnection
            ic.commitText(text, text.length)
        }
    }

    fun getWordSeparators() = wordSeparators

    fun isWordSeparator(code: Int): Boolean {
        if (code == KEYCODE_APOSTROPHE.toInt()) {
            return apostropheSeparator
        } else {
            return stringContains(wordSeparators, code.toChar())
        }
    }

    fun isSentenceSeparator(code: Int): Boolean {
        return stringContains(sentenceSeparators, code.toChar())
    }

    fun resetWordComposer(foundWord: WordComposerImpl?) {
        if (foundWord != null) {
            currentWordComposer = WordComposerImpl(foundWord)
        } else {
            currentWordComposer.reset()
        }
    }

    fun insertPeriodOnDoubleSpace() {
        // if (mCorrectionMode == Suggest.CORRECTION_NONE) return;
        val ic = inputConnectionProvider.currentInputConnection ?: return
        val lastThree = ic.getTextBeforeCursor(3, 0)
        if (lastThree != null && lastThree.length == 3
                && Character.isLetterOrDigit(lastThree[0])
                && isSpace(lastThree[1])
                && isSpace(lastThree[2])) {
            ic.beginBatchEdit()
            ic.deleteSurroundingText(2, 0)
            ic.commitText(". ", 1)
            ic.endBatchEdit()
            inputConnectionProvider.updateShiftKeyStateFromEditorInfo()
        }
    }

    private fun isSpace(character: Char): Boolean {
        return character == KEYCODE_SPACE || character == KEYCODE_NON_BREAKABLE_SPACE
    }

    fun isCursorAtEnd(): Boolean {
        val ic = inputConnectionProvider.currentInputConnection ?: return false
        val toRight = ic.getTextAfterCursor(1, 0)
        return toRight == null || toRight.isEmpty()
    }

    fun preferCapitalization(): Boolean {
        val word = currentWordComposer
        return word.isCapitalized
    }

    fun deleteSelectedWord(ic: InputConnection) {
        if (lastSelectionStart < lastSelectionEnd) {
            ic.setSelection(lastSelectionStart, lastSelectionStart)
        }
        EditingUtil.deleteWordAtCursor(ic, getWordSeparators())
    }

    fun commitPickedSuggestion(suggestion: CharSequence, correcting: Boolean) {
        val ic = inputConnectionProvider.currentInputConnection
        if (ic != null) {
            // If text is in correction mode and we're not using composing
            // text to underline, then the word at the cursor position needs
            // to be removed before committing the correction
            if (correcting) {
                deleteSelectedWord(ic)
            }

            ic.commitText(suggestion, 1)
        }
    }

    fun deleteLastPredictingCharacter(ic: InputConnection) {
        val wordComposer = currentWordComposer
        val length = wordComposer.size()
        if (length > 0) {
            wordComposer.deleteLast()
            // Update the composing now in T9 only if the whole word has been deleted
            val forceUpdate = length == 1
            setComposingText(ic, forceUpdate)
            if (wordComposer.size() == 0) {
                predicting = false
            }
            inputConnectionProvider.postUpdateSuggestions()
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    fun deleteLastCharacters(ic: InputConnection, toDelete: Int) {
        var remainingChars = toDelete
        val toTheLeft = ic.getTextBeforeCursor(remainingChars, 0)
        if (toTheLeft != null && toTheLeft.isNotEmpty()
                && isWordSeparator(toTheLeft[0].toInt())) {
            remainingChars--
        }
        ic.deleteSurroundingText(remainingChars, 0)
    }

    fun forceTypedWord(word: CharSequence) {
        currentWordComposer.forceTypedWord(word)
    }

    fun isLastSelectionEmpty(): Boolean {
        return lastSelectionStart == lastSelectionEnd
    }

    fun isCursorTouchingWord(): Boolean {
        val ic = inputConnectionProvider.currentInputConnection ?: return false
        val toLeft = ic.getTextBeforeCursor(1, 0)
        if (!TextUtils.isEmpty(toLeft) && !isWordSeparator(toLeft[0].toInt())) {
            return true
        }
        val toRight = ic.getTextAfterCursor(1, 0)
        if (!TextUtils.isEmpty(toRight) && !isWordSeparator(toRight[0].toInt())) {
            return true
        }
        return false
    }

    fun isCursorInsideWord(): Boolean {
        val ic = inputConnectionProvider.currentInputConnection ?: return false
        val toLeft = ic.getTextBeforeCursor(1, 0)
        val toRight = ic.getTextAfterCursor(1, 0)
        if (TextUtils.isEmpty(toLeft) || isWordSeparator(toLeft[0].toInt())) {
            return false
        }
        if (TextUtils.isEmpty(toRight) || isWordSeparator(toRight[0].toInt())) {
            return false
        }
        return true
    }

    fun reswapPeriodAndSpace() {
        val ic = inputConnectionProvider.currentInputConnection ?: return
        val lastThree = ic.getTextBeforeCursor(3, 0)
        if (lastThree != null && lastThree.length == 3
                && lastThree[0] == KEYCODE_PERIOD
                && lastThree[1] == KEYCODE_SPACE
                && lastThree[2] == KEYCODE_PERIOD) {
            ic.beginBatchEdit()
            ic.deleteSurroundingText(3, 0)
            ic.commitText(" ..", 1)
            ic.endBatchEdit()
            inputConnectionProvider.updateShiftKeyStateFromEditorInfo()
        }
    }

    companion object {

        val wordSeparators = ".\u0020\u00a0,;:!?\n()[]*&@{}/<>_+=|\"\u3002\u3001\u3000\u060c\u061f『』｛｝（）「」：；［］！？～＊※♪♬…＿・•◦【】☆★♥"
        val sentenceSeparators = ".,;:!?\u060c\u061f\u3002\u3001：！？…"
        internal val KEYCODE_SPACE = ' '
        internal val KEYCODE_PERIOD = '.'
        internal val KEYCODE_NON_BREAKABLE_SPACE = '\u00a0'
        internal val KEYCODE_APOSTROPHE = '\''

        // Faster implementation of String.contains (without allocation)
        private fun stringContains(string: CharSequence, code: Char): Boolean {
            val len = string.length
            for (i in 0..len - 1) {
                val c = string[i]
                if (c == code) {
                    return true
                }
            }
            return false
        }
    }
}
