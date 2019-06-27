package com.dexilog.smartkeyboard.keyboard;

import android.content.Context;
import android.content.res.Resources;

import com.dexilog.smartkeyboard.KeyboardPreferences;
import com.dexilog.smartkeyboard.R;
import com.dexilog.smartkeyboard.ui.KeyboardView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class KeyboardSwitcherTest {

    @Mock
    private KeyboardFactory keyboardFactory;
    private KeyboardSwitcher keyboardSwitcher;
    private KeyboardPreferences preferences;

    @Before
    public void setUp() throws Exception {
        preferences = new KeyboardPreferences();
        keyboardSwitcher = new TestableKeyboardSwitcher(keyboardFactory, preferences);
    }

    @Test
    public void testVoiceCodes() {
        assertVoiceCode(keyboardSwitcher, "EN", "en-US");
        assertVoiceCode(keyboardSwitcher, "BR", "pt-BR");
        assertVoiceCode(keyboardSwitcher, "FR", "FR");
    }

    @Test
    public void testPopupPreviewDisabledByDefaultInSymbolsMode() throws Exception {
        KeyboardView view = getSymbolsView();
        verify(view).setPopupKeyboardDisabled(true);
    }

    @Test
    public void testPopupPreviewEnabledInSymbolsModeIfAltSymbolsOn() throws Exception {
        preferences.altSymbols = true;
        KeyboardView view = getSymbolsView();
        verify(view).setPopupKeyboardDisabled(false);
    }

    @Test
    public void testPopupPreviewEnabledWithMoreSymbols() throws Exception {
        preferences.moreSymbols = true;
        KeyboardView view = getSymbolsView();
        verify(view).setPopupKeyboardDisabled(false);
    }

    @Test
    public void testSwitchToNextLanguageGivenTwoLanguages() throws Exception {
        keyboardSwitcher.setAvailLang(Arrays.asList("EN", "FR"));
        keyboardSwitcher.setCurLang("EN");
        keyboardSwitcher.changeLang(-1);
        assertThat(keyboardSwitcher.getCurLang(), is("FR"));
        keyboardSwitcher.changeLang(-1);
        assertThat(keyboardSwitcher.getCurLang(), is("EN"));
    }

    @Test
    public void testSwitchToEmojiAndBackToPreviousLanguage() throws Exception {
        keyboardSwitcher.setAvailLang(Arrays.asList("EM", "EN", "FR"));
        keyboardSwitcher.setCurLang("EN");
        keyboardSwitcher.changeLang(0);
        assertThat(keyboardSwitcher.getCurLang(), is("EM"));
        keyboardSwitcher.switchToLatestLanguage();
        assertThat(keyboardSwitcher.getCurLang(), is("EN"));
    }

    @Test
    public void testSwitchToLatestLanguageAtStartup() throws Exception {
        keyboardSwitcher.setAvailLang(Arrays.asList("EM", "FR", "RU"));
        keyboardSwitcher.setCurLang("EM");
        keyboardSwitcher.switchToLatestLanguage();
        assertThat(keyboardSwitcher.getCurLang(), is("FR"));
    }

    @Test
    public void testSwitchToLatestLanguageIfOnlyEmoji() throws Exception {
        keyboardSwitcher.setAvailLang(Arrays.asList("EM"));
        keyboardSwitcher.setCurLang("EM");
        keyboardSwitcher.switchToLatestLanguage();
        assertThat(keyboardSwitcher.getCurLang(), is("EM"));
    }

    @Test
    public void testSwitchToEmojiAndBackToEnglish() throws Exception {
        keyboardSwitcher.setAvailLang(Arrays.asList("EN"));
        keyboardSwitcher.switchToEmoji();
        assertThat(keyboardSwitcher.getCurLang(), is("EM"));
        keyboardSwitcher.switchToLatestLanguage();
        assertThat(keyboardSwitcher.getCurLang(), is("EN"));
    }

    private KeyboardView getSymbolsView() {
        Keyboard keyboard = mock(Keyboard.class);
        KeyboardView view = mock(KeyboardView.class);
        when(keyboardFactory.getCachedKeyboard(any(Integer.class), eq(R.id.mode_normal), any(Context.class))).thenReturn(keyboard);
        keyboardSwitcher.setInputView(view);
        keyboardSwitcher.toggleSymbols();
        return view;
    }

    private void assertVoiceCode(KeyboardSwitcher keyboardSwitcher, String langCode, String voiceCode) {
        keyboardSwitcher.setCurLang(langCode);
        assertThat(keyboardSwitcher.getVoiceLang(), is(voiceCode));
    }

    private class TestableKeyboardSwitcher extends KeyboardSwitcher {
        TestableKeyboardSwitcher(KeyboardFactory keyboardFactory, KeyboardPreferences preferences) {
            super(mock(Context.class), keyboardFactory, preferences);
            mCurKeyboard = mock(Keyboard.class);
        }

        @Override
        protected void createLangPopup() {}

        @Override
        protected Resources getResources() {
            return mock(Resources.class);
        }
    }
}
