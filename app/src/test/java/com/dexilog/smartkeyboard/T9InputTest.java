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
import com.dexilog.smartkeyboard.ui.CandidateView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class T9InputTest {

    private FakeInputConnection inputConnection;
    private InputController inputController;
    private SuggestController suggestController;
    private FakeDictionary mainDictionary;

    @Mock
    InputConnectionProvider inputConnectionProvider;

    @Mock
    SmartKeyboard smartKeyboard;

    @Mock
    CandidateView candidateView;

    @Mock
    KeyboardSwitcher keyboardSwitcher;

    @Mock
    private ExpandableDictionary autoDictionary;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        inputConnection = new FakeInputConnection();
        mainDictionary = new FakeDictionary();
        mainDictionary.addWord("hello");
        when(inputConnectionProvider.getCurrentInputConnection()).thenReturn(inputConnection);
        inputController = new InputController(inputConnectionProvider, false);
        smartKeyboard.mKeyboardSwitcher = keyboardSwitcher;
        suggestController = initSuggestController();
        smartKeyboard.mCorrectionMode = Suggest.CORRECTION_FULL;
    }

    private SuggestController initSuggestController() {
        Suggest suggest = new Suggest(null);
        suggest.mMainDict = mainDictionary;
        SuggestController suggestController = new SuggestController(smartKeyboard, suggest,
                inputController, autoDictionary);
        suggestController.mCandidateView = candidateView;
        when(smartKeyboard.isPredictionOn()).thenReturn(true);
        when(smartKeyboard.isModeT9()).thenReturn(true);
        when(smartKeyboard.isT9PredictionOn()).thenReturn(true);
        when(smartKeyboard.getCurrentInputConnection()).thenReturn(inputConnection);
        inputController.setPredicting(true);
        smartKeyboard.mCapsLock = false;
        return suggestController;
    }

    @Test
    public void testComposeFullT9Word() throws Exception {
        inputSequence("ghi|def|jkl|jkl|mno");
        assertInputText("hello", 0, 5);
    }

    @Test
    public void testComposePartialT9Word() throws Exception {
        inputSequence("ghi|def|jkl");
        assertInputText("hel", 0, 3);
    }

    @Test
    public void testComposeT9WordNotInDictionary() throws Exception {
        inputSequence("ghi|mno|mno");
        assertInputText("gmm", 0, 3);
    }

    @Test
    public void testToggleOffT9KeepsPartialWord() throws Exception {
        inputSequence("ghi|def|jkl");
        suggestController.switchOffT9Prediction();
        assertInputText("hel", 0, 3);
    }

    @Test
    public void testToggleOffT9AndBackspaceKeepsPartialWord() throws Exception {
        inputSequence("ghi|def|jkl|jkl");
        suggestController.switchOffT9Prediction();
        suggestController.deleteLastCharacter();
        assertInputText("hel", 0, 3);
        inputSequence("pqrs");
        assertInputText("help", 0, 4);
    }

    @Test
    public void byDefaultFirstLetterShouldBeSuggested() throws Exception {
        inputSequence("def");
        assertInputText("d", 0, 1);
    }

    @Test
    public void spanishSingleLetterWordsShouldHavePriority() throws Exception {
        inputSequence("wxyz");
        assertInputText("y", 0, 1);
    }

    @Test
    public void russianSingleLetterWordsShouldHavePriority() throws Exception {
        inputSequence("ьэюя");
        assertInputText("я", 0, 1);
    }

    private void assertInputText(String text, int composingStart, int composingEnd) {
        assertEquals(text, inputConnection.getText());
        assertEquals(composingStart, inputConnection.composingStart);
        assertEquals(composingEnd, inputConnection.composingEnd);
    }

    private void inputSequence(String sequence) {
        final String keys[] = sequence.split("\\|");
        for (String key: keys) {
            final int[] codes = getCodesForKey(key);
            inputController.addCharacterWithComposing(codes[0], codes, false, false);
        }
        suggestController.updateSuggestions();
    }

    private int[] getCodesForKey(String key) {
        final int nbCodes = key.length();
        final int codes[] = new int[nbCodes];
        for (int i = 0; i < nbCodes; i++) {
            codes[i] = key.charAt(i);
        }
        return codes;
    }
 }
