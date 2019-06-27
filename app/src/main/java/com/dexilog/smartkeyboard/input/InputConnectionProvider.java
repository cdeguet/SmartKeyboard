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

package com.dexilog.smartkeyboard.input;

import android.view.inputmethod.InputConnection;

import com.dexilog.smartkeyboard.lang.Converter;

public interface InputConnectionProvider {
    InputConnection getCurrentInputConnection();

    Converter getConverter();

    void setConvertedComposing(CharSequence convertedWord);

    boolean isT9PredictionOn();

    void sendKeyChar(char keyCode);

    void updateShiftKeyStateFromEditorInfo();

    void postUpdateSuggestions();
}