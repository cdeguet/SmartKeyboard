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

package com.dexilog.smartkeyboard.utils;


public class Workarounds
{
	//Determine whether this device has the fix for RTL in the suggestions list
	private static final boolean ms_requiresRtlWorkaround;
	private static StringBuilder mStringBuilder = new StringBuilder();

	private static final char KOREAN_UPPER[] = 
		{0, 0x2, 0, 0, 0, 0, 0, 0x8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x13, 
		0, 0, 0x16, 0, 0, 0x19, 0, 0, 0, 0, 0, 0, 0, 0x22, 0, 0, 0, 0x26};

	private static final char THAI_UPPER[] = 
		{3584 , 3599 , 3672 , 3587 , 3669 , 3587 , 3590 , 46 , 3671 , 3593 , 3673 , 3595 , 
		3596 , 3597 , 3598 , 3599 , 3600 , 3601 , 3602 , 3603 , 3650 , 3670 , 3668 , 63 , 
		3608 , 3631 , 3600 , 41 , 40 , 3622 , 3601 , 3620 , 3667 , 3602 , 3597 , 3603 , 3620 , 
		44 , 3622 , 3595 , 3624 , 3625 , 3624 , 3590 , 3628 , 3630 , 3630 , 3631 , 3608 , 3661 , 
		3625 , 3598 , 3642 , 3658 , 3647 , 3660 , 3641 , 3641 , 3642 , 3643 , 3644 , 3645 , 3646 , 
		3647 , 3596 , 3593 , 3650 , 3628 , 34 , 3653 , 3666 , 3655 , 3659 , 3655 , 3658 , 3659 , 
		3660 , 3661 , 3662 , 3663 , 3665 , 3665};

	static
	{
		boolean requiresRtlWorkaround = true;//all devices required this fix (in 2.1 it is still required)
		
		final String model = android.os.Build.MODEL.toLowerCase();
		if (!android.os.Build.USER.toLowerCase().contains("root"))//there is no rooted ROM with a fix.
		{
			if (model.contains("galaxy"))
			{
				//see issue 132
				//and issue 285
				//no fix: 1251851795000
				//fix: 1251970876000
				//no fix: 1251851795000
				//fix: 1251970876000
				//fix: 1261367883000
	//			//final int buildInc = Integer.parseInt(android.os.Build.VERSION.INCREMENTAL);
	//			//requiresRtlWorkaround = (buildInc < 20090831);
				requiresRtlWorkaround =  (android.os.Build.TIME <= 1251851795000l);
			}
			else if (android.os.Build.DEVICE.toLowerCase().contains("spica"))
			{
				//(see issue 285):
				//fixed: 1263807011000
				requiresRtlWorkaround =  (android.os.Build.TIME < 1263807011000l);//this is a lower "L" at the end
			}
		}
		ms_requiresRtlWorkaround = requiresRtlWorkaround;
	}

	public static boolean isRightToLeftCharacter(final char key)
	{
    	final byte direction = Character.getDirectionality(key);

    	switch(direction)
		{
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
			return true;
		default:
			return false;
		}
	}
	
	public static int workaroundParenthesisDirectionFix(int primaryCode)
	{
		//Android does not support the correct direction of parenthesis in right-to-left langs.
		if (!ms_requiresRtlWorkaround)
			return primaryCode;//I hope Galaxy has the fix...
		
		if (primaryCode == (int)')')
			return '(';
		else if (primaryCode == (int)'(')
			return ')';
		
		return primaryCode;
	}
	
	public static CharSequence workaroundCorrectStringDirection(CharSequence suggestion) 
    {
		//Hebrew letters are to be drawn in the other direction.
    	//Also, this is not valid for Galaxy (Israel's Cellcom Android)
    	if (!ms_requiresRtlWorkaround)
			return suggestion;
		
    	//this function is a workaround! In the official 1.5 firmware, there is a RTL bug.
    	if (suggestion.length() > 0 && isRightToLeftCharacter(suggestion.charAt(0)))
    	{
    		mStringBuilder.setLength(0);
			for(int charIndex = suggestion.length() - 1; charIndex>=0; charIndex--)
			{
				mStringBuilder.append(suggestion.charAt(charIndex));
			}
			return mStringBuilder.toString();
    	}
    	else
    		return suggestion;
	}

	/**
	 * Adjust the case including for korean characters
	 */
	public static CharSequence adjustCase(CharSequence text) {
		mStringBuilder.setLength(0);
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			mStringBuilder.append(toUpper(c));
		}
		return mStringBuilder.toString();
	}

	public static char toUpper(char c) {
		if (c < 3584) {
			return Character.toUpperCase(c);
		} else {
			if (c < 3666) {
				// Thai
				return (char)THAI_UPPER[c-3584];
			} else if (c < 0x3131 || c > 0x3154) {
				return Character.toUpperCase(c);
			} else {
				// Korean characters
				final char upper = KOREAN_UPPER[c-0x3130];
				if (upper != 0) {
					return (char)(upper + 0x3130);
				} else {
					return c;
				}
			}
		}
	}
	
}
