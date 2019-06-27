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

import com.dexilog.smartkeyboard.input.InputConnectionProvider;
import com.dexilog.smartkeyboard.input.InputController;
import com.dexilog.smartkeyboard.keyboard.KeyboardSwitcher;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

public class ComposingTest {
    private FakeInputConnection inputConnection;
    private InputController inputController;

    @Mock
    InputConnectionProvider inputConnectionProvider;

    @Mock
    private SuggestController suggestController;

    @Mock
    KeyboardSwitcher keyboardSwitcher;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private SmartKeyboard smartKeyboard;

    @Before
    public void setUp() {
        inputConnection = new FakeInputConnection();
        when(inputConnectionProvider.getCurrentInputConnection()).thenReturn(inputConnection);
        inputController = new InputController(inputConnectionProvider, false);
        smartKeyboard = new TestableSmartKeyboard();
        smartKeyboard.mInputController = inputController;
        smartKeyboard.suggestController = suggestController;
        smartKeyboard.mKeyboardSwitcher = keyboardSwitcher;
    }

    @Test
    public void shouldStartComposingAfterNonBreakingSpace() throws Exception {
        smartKeyboard.mPredictionOn = true;
        inputConnection.commitText("Hello\u00a0", 6);
        smartKeyboard.handleCharacter('w', new int[] {'w'}, false, false);
        assertThat(inputController.getPredicting(), is(true));
    }

    private class TestableSmartKeyboard extends SmartKeyboard {
        @Override
        public void postUpdateSuggestions() {
        }

        @Override
        public void updateShiftKeyStateFromEditorInfo() {
        }
    }
}
