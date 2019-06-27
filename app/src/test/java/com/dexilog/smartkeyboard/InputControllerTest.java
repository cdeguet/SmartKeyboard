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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class InputControllerTest {

    private FakeInputConnection inputConnection;
    private InputController inputController;

    @Mock
    InputConnectionProvider inputConnectionProvider;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();


    @Before
    public void setUp() throws Exception {
        inputConnection = new FakeInputConnection();
        when(inputConnectionProvider.getCurrentInputConnection()).thenReturn(inputConnection);
        inputController = new InputController(inputConnectionProvider, false);
    }

    @Test
    public void shouldInsertPeriodAfterDoubleSpace() throws Exception {
        inputConnection.commitText("Hello  ", 6);
        inputController.insertPeriodOnDoubleSpace();
        assertThat(inputConnection.getText(), is("Hello. "));
    }

    @Test
    public void shouldInsertPeriodAfterNonBreakableSpaceAndSpace() throws Exception {
        inputConnection.commitText("Hello\u00a0 ", 6);
        inputController.insertPeriodOnDoubleSpace();
        assertThat(inputConnection.getText(), is("Hello. "));
    }
}
