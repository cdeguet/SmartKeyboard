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

package com.dexilog.smartkeyboard.keyboard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;

import com.dexilog.smartkeyboard.KeyboardPreferences;
import com.dexilog.smartkeyboard.input.HardKeyboardTranslator;
import com.dexilog.smartkeyboard.input.WordComposerImpl;
import com.dexilog.smartkeyboard.lang.Converter;
import com.dexilog.smartkeyboard.lang.Korean;
import com.dexilog.smartkeyboard.lang.Pinyin;
import com.dexilog.smartkeyboard.lang.RomajiKana;
import com.dexilog.smartkeyboard.lang.Tamil;
import com.dexilog.smartkeyboard.lang.Telex;
import com.dexilog.smartkeyboard.lang.Unicode;

import com.dexilog.smartkeyboard.R;
import com.dexilog.smartkeyboard.ui.KeyboardView;


public class KeyboardSwitcher {

	static final int SHIFT_OFF = 0;
	static final int SHIFT_ON = 1;
	static final int SHIFT_LOCK = 2;

	public static final int MODE_TEXT = 0;
	public static final int MODE_IM = 1;
	public static final int MODE_URL = 2;
	public static final int MODE_SYMBOLS = 3;
	public static final int MODE_NUMBERS = 3;

	public static final int KBD_NORMAL = 0;
	public static final int KBD_SYMBOLS = 1;
	public static final int KBD_ARROWS = 2;
	public static final int KBD_UNICODE = 3;
	
	public static final int PORTRAIT_NORMAL = 0;
	public static final int PORTRAIT_T9 = 1;
	public static final int PORTRAIT_COMPACT = 2;

	private static final int SYMBOLS_MODE_STATE_NONE = 0;
	private static final int SYMBOLS_MODE_STATE_BEGIN = 1;
	private static final int SYMBOLS_MODE_STATE_SYMBOL = 2;

	private static final Map<String, String> VOICE_MAPPINGS;
	public static final String EMOJI_LANG = "EM";

	static {
		VOICE_MAPPINGS = new HashMap<>();
		VOICE_MAPPINGS.put("JP", "JA");
		VOICE_MAPPINGS.put("CZ", "CS");
		VOICE_MAPPINGS.put("EN", "en-US");
		VOICE_MAPPINGS.put("PT", "pt-PT");
		VOICE_MAPPINGS.put("BR", "pt-BR");
	}

	private final KeyboardPreferences preferences;

	private Context mContext;
	private KeyboardView mInputView;
	List<String> mLangList;
	Keyboard mCurKeyboard;
	String mCurLanguage;
	int mCurLangIndex = 0;
	int mShiftState = SHIFT_OFF;
	private int mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
	private int mKbdMode = 0;
	int mMode;
	int mOptions;
	private boolean mArrowKeybadEnabled = false;
	private boolean mMicButton = true;
	private int mPortraitMode = PORTRAIT_NORMAL;
	private boolean mT9prediction = true;
	private boolean mPrediction = true;
	private boolean mAlphaMode = true;
	private int mSmileyMode = Keyboard.SMILEY_KEY_AUTO;
	public boolean mComposingLang = false; // True for languages needing composing (like korean)
	private Converter mKorean;
	private Converter mRomajiKana;
	private Converter mPinyin;
	private Converter mTelex;
	private Converter mUnicode;
	private Converter mConverter;
	public boolean mAutoCap = false; // True if language needs autocap
	private int mArrowsStyle = 0;
	private boolean mHasVoice = false;
	private boolean mT9NextKey = true;
	private Keyboard mLangPopup;
	private HardKeyboardTranslator mHardTranslator;
	int mLangIcon;
	public boolean mIsUnicode = false;
	public boolean mHideLangKey = false;
	private int mEmojiIndex = 1;

	private final Set<String> mVoiceLangs = new HashSet<String>(Arrays.asList(
			new String[] {"AF", "EN", "JP", "DE", "ES", "FR", "ID", "IT", "KO", "NL", "PT", "PL", "CZ",
					"RU", "ZH", "TR", "HE", "EL", "AR", "FA", "FI", "DA", "HR", "IS", "HU", "RO", "SK",
					"SV", "VI", "BG", "UK", "TH", "HI", "NO", "BR"}));

	private KeyboardFactory keyboardFactory;
	private String mPrevLanguage = null;
	private Tamil tamil;

	public KeyboardSwitcher(Context context, KeyboardFactory keyboardFactory, KeyboardPreferences preferences) {
		mContext = context;
		this.keyboardFactory = keyboardFactory;
		this.preferences = preferences;
	}

	public void setArrowsStyle(int arrowsStyle) {
		switch (arrowsStyle) {
		case 0:
			mArrowsStyle = R.xml.arrows_only;
			break;
		case 1:
			mArrowsStyle = R.xml.arrows_numbers;
			break;
		case 2:
			mArrowsStyle = R.xml.arrows_calculator;
			break;
		}
	}
	
	public void setT9Prediction(boolean state) {
		mT9prediction = state;
	}
	
	public void setT9NextKey(boolean t9NextKey) {
		if (t9NextKey != mT9NextKey) {
			mT9NextKey = t9NextKey;
		}
	}
	
	public void setHebrewAlt(boolean hebrewAlt) {
		keyboardFactory.setHebrewAlt(hebrewAlt);
	}
	
	public void setCzechFull(boolean czechFull) {
		keyboardFactory.setCzechFull(czechFull);
	}

	public void setAltCompact(boolean altCompact) {
		keyboardFactory.setAltCompact(altCompact);
	}
	
	public void setInputView(KeyboardView inputView) {
		mInputView = inputView;
	}

	public KeyboardView getMainKeyboardView() {
		return mInputView;
	}

	public void setHideLangKey(boolean hideLangKey) {
		mHideLangKey = hideLangKey;
	}

	// Set the list of displayed languages (lang key)
	public void setAvailLang(List<String> langList) {
		mLangList = langList;
		if (langList.size() > 0) {
			createLangPopup();
		}
	}

	protected void createLangPopup() {
		mLangPopup = new Keyboard(mContext, R.xml.popup, mLangList);
	}

	public void setCurLang(final String lang) {
		if (mCurLanguage != null && !mCurLanguage.equals(EMOJI_LANG)) {
			mPrevLanguage = mCurLanguage;
		}
		mCurLanguage = lang;
		mComposingLang = false;
		mConverter = null;
		mAutoCap = true;
		mIsUnicode = false;
		final String langCode = lang.substring(0, 2);
		mHasVoice = mVoiceLangs.contains(langCode);
		if (langCode.equals("KO")) {
			mComposingLang = true;
			mConverter = getKorean();
			mAutoCap = false;
		} else if (langCode.equals("JP")) {
			mComposingLang = true;
			mConverter = getJapanese();
			mAutoCap = false;	
		} else if (langCode.equals("TH")) {
			mAutoCap = false;
		} else if (langCode.equals("ZH")) {
			mComposingLang = true;
			mAutoCap = false;
		} else if (langCode.equals("PY")) {
			mAutoCap = false;
			mConverter = getPinyin();
		} else if (langCode.equals("VI")) {
			mConverter = getTelex();
		} else if (langCode.equals(EMOJI_LANG)) {
			mAutoCap = false;
		} else if (langCode.equals("TA")) {
			mComposingLang = true;
			mAutoCap = false;
			mConverter = getTamil();
		}
		if (mHardTranslator != null) {
			mHardTranslator.setLang(langCode, lang);
		}
		setLangIcon(langCode);
	}

	private Converter getTamil() {
		if (tamil == null) {
			tamil = new Tamil();
		}
		return tamil;
	}

	protected void setLangIcon(String langCode) {
		StringBuilder sb = new StringBuilder("icon_");
		sb.append(langCode.toLowerCase());
		if (mLangList != null && mLangList.size() > 1) {
			Resources res = getResources();
			mLangIcon = res.getIdentifier(sb.toString(), "drawable", mContext.getPackageName());
		} else {
			mLangIcon = 0;
		}
	}

	public String getCurLang() {
		return mCurLanguage;
	}
	
	public String getCurLangCode() {
		return mCurLanguage.substring(0, 2);
	}
	
	public String getVoiceLang() {
		// Fix the wrong language codes...
		final String code = mCurLanguage.substring(0, 2);
		final String voiceCode = VOICE_MAPPINGS.get(code);
		return voiceCode != null ? voiceCode : code;
	}
	
	public Converter getConverter() {
		return mIsUnicode ? getUnicodeConverter() : mConverter;
	}
	
	public HardKeyboardTranslator getHardKeyboardTranslator() {
		if (mHardTranslator == null) {
			mHardTranslator = new HardKeyboardTranslator(mContext);
			mHardTranslator.setLang(mCurLanguage.substring(0, 2), mCurLanguage);
		}
		return mHardTranslator;
	}
		
	public void setPortraitMode(boolean isPortrait, int portraitMode, boolean restart) {
		keyboardFactory.mPortrait = isPortrait;
		mPortraitMode = portraitMode;
		if (restart) {
			setKeyboardMode(mMode, mOptions, mKbdMode);
		}
	}
	
	public void setSmileyMode(int smileyMode, boolean restart) {
		mSmileyMode = smileyMode;
		if (restart) {
			setKeyboardMode(mMode, mOptions, mKbdMode);
		}
	}
	
	public void setEnableArrowKeypad(boolean enableArrowKeypad) {
		mArrowKeybadEnabled = enableArrowKeypad;
	}
	
	public void setArrowsMain(int arrowsMain) {
		keyboardFactory.mArrowsMain = arrowsMain;
	}
	
	public void setNumbersTop(int numbersTop) {
		keyboardFactory.mNumbersTop = numbersTop;
	}
	
	public void setMicButton(boolean showMicButton) {
		mMicButton = showMicButton;
	}

	public void setPrediction(boolean predictionOn) {
		mPrediction = predictionOn;
		if (mCurKeyboard != null) {
			mCurKeyboard.enableT9(mT9prediction && mPrediction);
		}
	}
	
	public void changeEmoji(int offset) {
		final int nbPages = getEmojiCategories().getNbPages();
		int index = (mEmojiIndex + nbPages + offset - 1) % nbPages + 1;
		changeEmojiPage(index);
	}

	public void changeEmojiCategory(int category) {
		int index = getEmojiCategories().getCategoryIndex(category) + 1;
		changeEmojiPage(index);
	}

	private void changeEmojiPage(int newEmojiIndex) {
		mEmojiIndex = newEmojiIndex;
		setKeyboardMode(mMode, mOptions, KBD_NORMAL);
	}

	private EmojiCategories getEmojiCategories() {
		if (keyboardFactory.mEmojiCategories == null) {
			keyboardFactory.mEmojiCategories = new EmojiCategories(mContext);
		}
		return keyboardFactory.mEmojiCategories;
	}

	private Converter getKorean() {
		if (mKorean == null) {
			mKorean = new Korean(new WordComposerImpl());
		}
		return mKorean;
	}
	
	private Converter getJapanese() {
		if (mRomajiKana == null) {
			mRomajiKana = new RomajiKana();
		}
		return mRomajiKana;
	}
	
	private Converter getPinyin() {
		if (mPinyin == null) {
			mPinyin = new Pinyin();
		}
		return mPinyin;
	}
	
	private Converter getTelex() {
		if (mTelex == null) {
			mTelex = new Telex();
		}
		return mTelex;
	}
	
	private Converter getUnicodeConverter() {
		if (mUnicode == null) {
			mUnicode = new Unicode();
		}
		return mUnicode;
	}

	public void makeKeyboards(boolean clear) {
		keyboardFactory.makeKeyboards(clear);
	}

	public void setKeyboardMode(int mode, int options, int kbdMode) {
		
		//Log.d("KBD", "setKeyboardMode " + Integer.toString(mode) + " " + Integer.toString(kbdMode));
		
		mMode = mode;
		mOptions = options;
		mKbdMode = kbdMode;
		mIsUnicode = false;
		//mInputView.setPreviewEnabled(true);

		// Set keyboard mode
		int keyboardMode;
		if (mLangList == null || mLangList.size() <= 1 || mHideLangKey) {
			keyboardMode = (mode == MODE_URL) ? R.id.mode_url : R.id.mode_normal;
		} else {
			keyboardMode = (mode == MODE_URL) ? R.id.mode_lang_url : R.id.mode_lang;
			// Check if the current language is in the list
			int langIndex = mLangList.indexOf(mCurLanguage);
			if (langIndex != -1) {
				mCurLangIndex = langIndex;
			} else {
				// Otherwise, consider the first language is selected
				mCurLangIndex = 0;
			}
		}

		switch (kbdMode) {
		case KBD_SYMBOLS:
			mSymbolsModeState = SYMBOLS_MODE_STATE_BEGIN;
			if (mShiftState == SHIFT_OFF) {
				mCurKeyboard = getSymbols();
			} else {
				mCurKeyboard = getAltSymbols();
			}
			mAlphaMode = false;
			break;
			
		case KBD_NORMAL:
			mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
			if (mode == MODE_NUMBERS) {
				mCurKeyboard = getNumbers();
				mAlphaMode = false;
			} else if (mCurLanguage.equals(EMOJI_LANG)) {
				// Emoji keyboard
				mCurKeyboard = keyboardFactory.getEmoji(mContext, mEmojiIndex, showEmojiLangKey());
				mAlphaMode = false;
			} else {
				mCurKeyboard = keyboardFactory.getLangKeyboard(mCurLanguage, keyboardMode,
						mPortraitMode, mContext);
				mAlphaMode = true;
			}
			break;
			
		case KBD_ARROWS:
			mShiftState = SHIFT_OFF;
			mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
			mCurKeyboard = getArrows();
			mAlphaMode = false;
			break;
		case KBD_UNICODE:
			mShiftState = SHIFT_OFF;
			mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
			mCurKeyboard = getUnicode();
			mAlphaMode = true;
			mIsUnicode = true;
			break;
		}
		if (mCurKeyboard != null) {
			configureKeyboard(mode, options, mCurKeyboard);
		}
		if (mInputView != null) {
			configureInputView();
		}
	}

	private boolean showEmojiLangKey() {
		return (mLangList != null && mLangList.size() >= 3 && mLangList.contains(EMOJI_LANG));
	}

	private void configureKeyboard(int mode, int options, Keyboard keyboard) {
		keyboard.setShifted(mShiftState != SHIFT_OFF);
		keyboard.setShiftLocked(mShiftState != SHIFT_OFF);
		keyboard.setOptions(getResources(), mode, options, mSmileyMode);
		keyboard.setMicButton(getResources(), getMicDisplay());
		keyboard.enableT9(mT9prediction && mPrediction);
		keyboard.setT9NextKey(mT9NextKey);
	}

	private Keyboard.MicDisplay getMicDisplay() {
		if (mHasVoice && mMicButton) {
			return preferences.micAboveComma ?
					Keyboard.MicDisplay.ABOVE_COMMA : Keyboard.MicDisplay.REPLACE_COMMA;
		} else {
			return Keyboard.MicDisplay.HIDDEN;
		}
	}

	protected Resources getResources() {
		return mContext.getResources();
	}

	private void configureInputView() {
		mInputView.setKeyboard(mCurKeyboard);
		mInputView.setCompactLayout(mPortraitMode != PORTRAIT_NORMAL);
		mInputView.setLangPopup(mLangPopup);
		boolean displayAlt = (mKbdMode == KBD_SYMBOLS) ? preferences.altSymbols : preferences.displayAlt;
		mInputView.setDisplayAlt(displayAlt);
		boolean disablePopupKeyboard = (mKbdMode == KBD_SYMBOLS) &&
				!preferences.altSymbols && !preferences.moreSymbols;
		mInputView.setPopupKeyboardDisabled(disablePopupKeyboard);
		mInputView.setCustomSmileys(preferences.customSmileys);
	}

	private Keyboard getSymbols() {
		final int symbolMode = mArrowKeybadEnabled ? R.id.mode_arrows : R.id.mode_normal;
		int xml = preferences.moreSymbols ? R.xml.symbols_more : R.xml.symbols;
		return keyboardFactory.getCachedKeyboard(xml, symbolMode, mContext);
	}
	
	private Keyboard getAltSymbols() {
		final int symbolMode = mArrowKeybadEnabled ? R.id.mode_arrows : R.id.mode_normal;
		int xml = preferences.moreSymbols ? R.xml.symbols_shift_more : R.xml.symbols_shift;
		return keyboardFactory.getCachedKeyboard(xml, symbolMode, mContext);
	}

	private Keyboard getArrows() {
		return keyboardFactory.getCachedKeyboard(mArrowsStyle, R.id.mode_normal, mContext);
	}
	
	private Keyboard getNumbers() {
		return keyboardFactory.getCachedKeyboard(R.xml.numbers, R.id.mode_normal, mContext);
	}
	
	private Keyboard getUnicode() {
		return keyboardFactory.getCachedKeyboard(R.xml.unicode, R.id.mode_normal, mContext);
	}

	public boolean isAlphabetMode() {
		return mAlphaMode;
	}

	// If index == -1, next language
	public void changeLang(int index) {
		if (mLangList == null || mLangList.size() == 0) {
			return;
		}
		// Change language
		if (index == -1) { // Next 
			mCurLangIndex = (mCurLangIndex + 1) % mLangList.size();
		} else if (index == -2) { // Previous
			final int size = mLangList.size();
			mCurLangIndex = (mCurLangIndex + size - 1) % size;
		} else {
			mCurLangIndex = index;
		}
		setCurLang(mLangList.get(mCurLangIndex));
		mShiftState = SHIFT_OFF;
		mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
		setKeyboardMode(mMode, mOptions, KBD_NORMAL);
	}
	
	public int getLangIcon() {
		return mLangIcon;
	}

	public boolean toggleT9() {
		if (mPrediction) {
			mT9prediction = !mT9prediction;
		}
		mCurKeyboard.enableT9(mT9prediction && mPrediction);
		return mT9prediction;
	}
	
	public void toggleSymbols() {
		final int kbdMode = (mKbdMode == 0 ? 1 : 0);
		setKeyboardMode(mMode, mOptions, kbdMode);
	}

	public void toggleArrows() {
		final int kbdMode = (mKbdMode == 2 ? 0 : 2);
		setKeyboardMode(mMode, mOptions, kbdMode);
	}
	
	public void toggleUnicode() {
		final int kbdMode = (mKbdMode == 3 ? 0 : 3);
		setKeyboardMode(mMode, mOptions, kbdMode);
	}

	public void toggleShift() {
		if (mSymbolsModeState != SYMBOLS_MODE_STATE_NONE) {
			if (!mCurKeyboard.isShifted()) {
				mCurKeyboard = getAltSymbols();
				mCurKeyboard.setShifted(true);
				mCurKeyboard.setOptions(getResources(), mMode, mOptions, mSmileyMode);
				mInputView.setKeyboard(mCurKeyboard);
			} else {
				mCurKeyboard = getSymbols();
				mCurKeyboard.setShifted(false);
				mInputView.setKeyboard(mCurKeyboard);
			}
		} else {
			mShiftState = (mShiftState + 1) % 3;
			setKeyboardMode(mMode, mOptions, mKbdMode);
		}
	}

	/**
	 * Updates state machine to figure out when to automatically switch back to alpha mode.
	 * Returns true if the keyboard needs to switch back 
	 */
	public boolean onKey(int key) {
		if (mMode == MODE_SYMBOLS) {
			// Don't do anything in symbol mode
			return false;
		}
		// Switch back to alpha mode if user types one or more non-space/enter characters
		// followed by a space/enter
		switch (mSymbolsModeState) {
		case SYMBOLS_MODE_STATE_BEGIN:
			if (key != ' ' && key != 10 && key > 0) {
				mSymbolsModeState = SYMBOLS_MODE_STATE_SYMBOL;
			}
			break;
		case SYMBOLS_MODE_STATE_SYMBOL:
			if (key == 10 || key == ' ') return true;
			break;
		}
		return false;
	}

	public void switchToLatestLanguage() {
		int prevLangIndex = -1;
		if (mPrevLanguage != null && mLangList != null) {
			prevLangIndex = mLangList.indexOf(mPrevLanguage);
		}
		changeLang(prevLangIndex);
	}

	public void switchToEmoji() {
		setCurLang(EMOJI_LANG);
		setKeyboardMode(mMode, mOptions, KBD_NORMAL);
	}
}
