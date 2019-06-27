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

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

public class FakeInputConnection implements InputConnection {

    private StringBuffer currentText = new StringBuffer();
    public int selectionStart = 0;
    public int selectionEnd = 0;
    public int composingStart = 0;
    public int composingEnd = 0;

    public String getText() {
        return currentText.toString();
    }

    @Override
    public CharSequence getTextBeforeCursor(int n, int flags) {
        if (selectionStart > 0)
            return currentText.subSequence(selectionStart-n, selectionStart);
        else
            return "";
    }

    @Override
    public CharSequence getTextAfterCursor(int n, int flags) {
        return null;
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        return null;
    }

    @Override
    public int getCursorCapsMode(int i) {
        return 0;
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest extractedTextRequest, int i) {
        return null;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        currentText.delete(selectionStart - beforeLength, selectionStart);
        selectionStart -= beforeLength;
        selectionEnd -= beforeLength;
        composingStart -= beforeLength;
        composingEnd -= beforeLength;
        return true;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        currentText.replace(composingStart, composingEnd, text.toString());
        int newCursorPos = currentText.length();
        composingEnd = newCursorPos;
        selectionStart = newCursorPos;
        selectionEnd = newCursorPos;
        return true;
    }

    @Override
    public boolean setComposingRegion(int i, int i1) {
        return false;
    }

    @Override
    public boolean finishComposingText() {
        return false;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        setComposingText(text, newCursorPosition);
        composingStart = composingEnd;
        return true;
    }

    @Override
    public boolean commitCompletion(CompletionInfo completionInfo) {
        return false;
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return false;
    }

    @Override
    public boolean setSelection(int start, int end) {
        return false;
    }

    @Override
    public boolean performEditorAction(int i) {
        return false;
    }

    @Override
    public boolean performContextMenuAction(int i) {
        return false;
    }

    @Override
    public boolean beginBatchEdit() {
        return true;
    }

    @Override
    public boolean endBatchEdit() {
        return false;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        return false;
    }

    @Override
    public boolean clearMetaKeyStates(int i) {
        return false;
    }

    @Override
    public boolean reportFullscreenMode(boolean b) {
        return false;
    }

    @Override
    public boolean performPrivateCommand(String s, Bundle bundle) {
        return false;
    }

    @Override
    public boolean requestCursorUpdates(int i) {
        return false;
    }
}
