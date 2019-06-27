package com.dexilog.smartkeyboard;

import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.dexilog.smartkeyboard.keyboard.Keyboard;
import com.dexilog.smartkeyboard.keyboard.KeyboardSwitcher;
import com.dexilog.smartkeyboard.ui.OnKeyboardActionListener;
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

class MyOnKeyboardActionListener implements OnKeyboardActionListener {

    // Key events coming any faster than this are long-presses.
    private static final int QUICK_PRESS = 200;

    private SmartKeyboard smartKeyboard;
    public int mDeleteCount;
    private long mLastKeyTime;

    public MyOnKeyboardActionListener(SmartKeyboard smartKeyboard) {
        this.smartKeyboard = smartKeyboard;
    }

	@Override
	public void onKey(int primaryCode, int[] keyCodes, boolean hardKbd,
                      boolean replace) {

		final long when = SystemClock.uptimeMillis();
        if (primaryCode != Keyboard.KEYCODE_DELETE
				|| when > mLastKeyTime + QUICK_PRESS) {
			mDeleteCount = 0;
		}
		mLastKeyTime = when;
		final KeyboardSwitcher keyboardSwitcher = smartKeyboard.mKeyboardSwitcher;
		switch (primaryCode) {
			case Keyboard.KEYCODE_DELETE:
				smartKeyboard.handleBackspace();
				mDeleteCount++;
				break;
			case Keyboard.KEYCODE_SHIFT:
				smartKeyboard.handleShift(false);
				break;
			case Keyboard.KEYCODE_CANCEL:
				if (smartKeyboard.mOptionsDialog == null || !smartKeyboard.mOptionsDialog.isShowing()) {
					smartKeyboard.handleClose();
				}
				break;
			case Keyboard.KEYCODE_MODE_CHANGE:
				smartKeyboard.changeKeyboardMode();
				break;
			case Keyboard.KEYCODE_ARROWS:
				keyboardSwitcher.toggleArrows();
				break;
			case Keyboard.KEYCODE_LANG:
				smartKeyboard.switchLang(-1);
				break;
			case Keyboard.KEYCODE_LEFT:
				smartKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
				break;
			case Keyboard.KEYCODE_RIGHT:
				smartKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
				break;
			case Keyboard.KEYCODE_UP:
				smartKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
				break;
			case Keyboard.KEYCODE_DOWN:
				smartKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
				break;
			case Keyboard.KEYCODE_TAB:
				smartKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB);
				break;
			case Keyboard.KEYCODE_MIC:
				if (smartKeyboard.mMicButton) {
					onVoiceInput();
				} else {
					// comma key otherwise
					smartKeyboard.handleSeparator(44, replace, hardKbd);
				}
				break;
			case Keyboard.KEYCODE_T9: {
				smartKeyboard.toggleT9();
				break;
			}
			case Keyboard.KEYCODE_NEXT:
				smartKeyboard.suggestController.handleNextSuggestion(false, smartKeyboard);
				// Cancel the just reverted state
				smartKeyboard.suggestController.mJustRevertedSeparator = null;
				break;
			case Keyboard.KEYCODE_NEXT_SPACE:
			case Keyboard.KEYCODE_NEXT_SPACE2:
				if (smartKeyboard.mInputController.getPredicting()) {
					smartKeyboard.suggestController.handleNextSuggestion(true, smartKeyboard);
					smartKeyboard.suggestController.mCandidateSelected = true;
					// Cancel the just reverted state
					smartKeyboard.suggestController.mJustRevertedSeparator = null;
				} else {
					smartKeyboard.handleSeparator(32, replace, hardKbd);
				}
				break;
			case Keyboard.KEYCODE_SETLANG:
				// Retrieve the lang index
				int langIndex = keyCodes[0];
				smartKeyboard.switchLang(langIndex);
				break;
			case Keyboard.KEYCODE_DAKUTEN:
				// Special handling for Japanese Dakuten
				smartKeyboard.handleDakuten();
				break;
			case Keyboard.KEYCODE_EMOJI_NEXT:
				keyboardSwitcher.changeEmoji(1);
				break;
			case Keyboard.KEYCODE_LATEST_LANG:
				smartKeyboard.switchToLatestLanguage();
				break;
			case Keyboard.KEYCODE_EMOJI_PREV:
				keyboardSwitcher.changeEmoji(-1);
				break;
			case Keyboard.KEYCODE_EMOJI_NUM:
				break;
			case Keyboard.KEYCODE_EMOJI_TAB1:
				keyboardSwitcher.changeEmojiCategory(0);
				break;
			case Keyboard.KEYCODE_EMOJI_TAB2:
				keyboardSwitcher.changeEmojiCategory(1);
				break;
			case Keyboard.KEYCODE_EMOJI_TAB3:
				keyboardSwitcher.changeEmojiCategory(2);
				break;
			case Keyboard.KEYCODE_EMOJI_TAB4:
				keyboardSwitcher.changeEmojiCategory(3);
				break;
			case Keyboard.KEYCODE_EMOJI_TAB5:
				keyboardSwitcher.changeEmojiCategory(4);
				break;
			case Keyboard.KEYCODE_EMOJI_TAB6:
				keyboardSwitcher.changeEmojiCategory(5);
				break;
			case Keyboard.KEYCODE_EMOJI_TAB7:
				keyboardSwitcher.changeEmojiCategory(6);
				break;
			case Keyboard.KEYCODE_EMOJI_TAB8:
				keyboardSwitcher.changeEmojiCategory(7);
				break;
			default:
				if (smartKeyboard.mInputController.isWordSeparator(primaryCode)) {
					smartKeyboard.handleSeparator(primaryCode, replace, hardKbd);
				} else {
					smartKeyboard.handleCharacter(primaryCode, keyCodes, replace, hardKbd);
				}
				// Cancel the just reverted state
				smartKeyboard.suggestController.mJustRevertedSeparator = null;
		}
		if (keyboardSwitcher.onKey(primaryCode)) {
			smartKeyboard.changeKeyboardMode();
		}
		// Reset after any single keystroke
		// mEnteredText = null;

		// Avoid race condition if a word is entered just after a number
		if (smartKeyboard.mHandler.hasMessages(SmartKeyboard.MSG_UPDATE_OLD_SUGGESTIONS)) {
			smartKeyboard.mHandler.removeMessages(SmartKeyboard.MSG_UPDATE_OLD_SUGGESTIONS);
		}
	}

	@Override
	public void onText(CharSequence text) {
		if (smartKeyboard.voiceInputController.mVoiceInputHighlighted) {
			smartKeyboard.commitVoiceInput();
		}
		final InputConnection ic = smartKeyboard.getCurrentInputConnection();
		if (smartKeyboard.mDebug)
			Log.d(SmartKeyboard.TAG, "onText " + text + " " +
					Boolean.toString(smartKeyboard.mInputController.getPredicting()));
		if (ic == null)
			return;
		ic.beginBatchEdit();
		if (smartKeyboard.mInputController.getPredicting()) {
			smartKeyboard.suggestController.commitTyped(ic);
		}
		ic.commitText(text, 1);
		ic.endBatchEdit();
		smartKeyboard.updateShiftKeyState(smartKeyboard.getCurrentInputEditorInfo());
		smartKeyboard.suggestController.mJustRevertedSeparator = null;
		// mEnteredText = text;
	}

	@Override
    public void swipeRight() {
		smartKeyboard.swipeAction(smartKeyboard.mSwipeRight);
	}

	@Override
	public void swipeLeft() {
		smartKeyboard.swipeAction(smartKeyboard.mSwipeLeft);
	}

	@Override
	public void swipeDown() {
		smartKeyboard.swipeAction(smartKeyboard.mSwipeDown);
	}

	@Override
	public void swipeUp() {
		smartKeyboard.swipeAction(smartKeyboard.mSwipeUp);

	}

	@Override
	public void onPress(int primaryCode) {
		smartKeyboard.vibrate(primaryCode);
		smartKeyboard.playKeyClick(primaryCode);
	}

	@Override
	public void onRelease(int primaryCode) {
		// vibrate();
	}

    @Override
	public boolean onDisplayPrefScreen() {
		return smartKeyboard.launchSettings();
	}

	@Override
	public void onVoiceInput() {
		smartKeyboard.voiceInputController.voiceSearch();
	}

	@Override
	public void onShowEmojis() {
		smartKeyboard.switchToEmoji();
	}
}
