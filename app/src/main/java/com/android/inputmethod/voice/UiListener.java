package com.android.inputmethod.voice;

import java.util.List;
import java.util.Map;


/**
 * Events relating to the recognition UI. You must implement these.
 */
public interface UiListener {

	/**
	 * @param recognitionResults a set of transcripts for what the user
	 *   spoke, sorted by likelihood.
	 */
	public void onVoiceResults(
			List<String> recognitionResults,
			Map<String, List<CharSequence>> alternatives);

	/**
	 * Called when the user cancels speech recognition.
	 */
	public void onCancelVoice();
}