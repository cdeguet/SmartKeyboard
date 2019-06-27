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

package com.dexilog.smartkeyboard.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dexilog.smartkeyboard.R;
import com.dexilog.smartkeyboard.input.TextEntryState;
import com.dexilog.smartkeyboard.utils.CompatUtils;
import com.dexilog.smartkeyboard.utils.Workarounds;

@SuppressLint("WrongCall")
public class CandidateView extends View {

    private static final int OUT_OF_BOUNDS = -1;
    private static final List<CharSequence> EMPTY_LIST = new ArrayList<CharSequence>();

    private CandidateInputService mService;
    private List<CharSequence> mSuggestions = EMPTY_LIST;
    private boolean mShowingCompletions;
    private CharSequence mSelectedString;
    private int mSelectedIndex;
    private int mCurrentIndex;
    private int mTouchX = OUT_OF_BOUNDS;
    private Drawable mSelectionHighlight;
    private boolean mTypedWordValid;
    
    private boolean mHaveMinimalSuggestion;
    
    private Rect mBgPadding;

    private TextView mPreviewText;
    private PopupWindow mPreviewPopup;
    private int mCurrentWordIndex;
    private Drawable mDivider;
    
    private static final int MAX_SUGGESTIONS = 510;
    private static final int SCROLL_PIXELS = 20;
    
    private static final int MSG_REMOVE_PREVIEW = 1;
    private static final int MSG_REMOVE_THROUGH_PREVIEW = 2;
    
    private int[] mWordWidth = new int[MAX_SUGGESTIONS];
    private int[] mWordX = new int[MAX_SUGGESTIONS];
    private int mPopupPreviewX;
    private int mPopupPreviewY;

    private static final int X_GAP = 10;
    
    private int mColorNormal;
    private int mColorRecommended;
    private int mColorOther;
    private Integer mColorHighlight;
    private int mPrefColorRecommended;
    private Paint mPaint;
    private int mDescent;
    private boolean mScrolled;
    private int mTargetScrollX;
    private boolean mRTLSuggestions;
    
    private int mMinTouchableWidth;
    
    private int mTotalWidth;
    
    private GestureDetector mGestureDetector;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REMOVE_PREVIEW:
                    mPreviewPopup.dismiss();
                    //mPreviewText.setVisibility(GONE);
                    break;
                case MSG_REMOVE_THROUGH_PREVIEW:
                    mPreviewPopup.dismiss();
                    //mPreviewText.setVisibility(GONE);
                    if (mTouchX != OUT_OF_BOUNDS) {
                        removeHighlight();
                    }
                    break;
            }

        }
    };

    /**
     * Construct a CandidateView for showing suggested words for completion.
     * @param context
     * @param attrs
     */
    public CandidateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflate =
            (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPreviewPopup = new PopupWindow(context);
        mPreviewText = (TextView) inflate.inflate(R.layout.candidate_preview, null);
        mPreviewText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mPreviewPopup.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mPreviewPopup.setContentView(mPreviewText);
        mPreviewPopup.setBackgroundDrawable(null);
        CompatUtils.setPopupUnattachedToDecor(mPreviewPopup);
        mColorNormal = context.getResources().getColor(R.color.candidate_normal);
        mColorRecommended = context.getResources().getColor(R.color.candidate_orange);
        mColorOther = context.getResources().getColor(R.color.candidate_other);
        mRTLSuggestions = true;

        mPaint = new Paint();
        mPaint.setColor(mColorNormal);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mPreviewText.getTextSize());
        mPaint.setStrokeWidth(0);
        mPaint.setTextAlign(Align.CENTER);
        mDescent = (int) mPaint.descent();
        // 80 pixels for a 160dpi device would mean half an inch
        mMinTouchableWidth = (int) (getResources().getDisplayMetrics().density * 50);
        
        mGestureDetector = new GestureDetector(context, 
        		new CandidateStripGestureListener(mMinTouchableWidth));
        setHorizontalFadingEdgeEnabled(true);
        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        scrollTo(0, getScrollY());
    }

    private class CandidateStripGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final int mTouchSlopSquare;

        public CandidateStripGestureListener(int touchSlop) {
            // Slightly reluctant to scroll to be able to easily choose the suggestion
            mTouchSlopSquare = touchSlop * touchSlop;
        }
    	
        @Override
        public void onLongPress(MotionEvent me) {
            if (mSuggestions.size() > 0) {
                if (me.getX() + getScrollX() < mWordWidth[0] && getScrollX() < 10) {
                    longPressFirstWord();
                }
            }
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
            mScrolled = false;
            return false;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            if (!mScrolled) {
                // This is applied only when we recognize that scrolling is starting.
                final int deltaX = (int) (e2.getX() - e1.getX());
                final int deltaY = (int) (e2.getY() - e1.getY());
                final int distance = (deltaX * deltaX) + (deltaY * deltaY);
                if (distance < mTouchSlopSquare) {
                    return true;
                }
                mScrolled = true;
            }

            final int width = getWidth();
            mScrolled = true;
            scrollBy((int) distanceX, 0);
            if (getScrollX() < 0) {
                scrollTo(0, getScrollY());
            }
            if (distanceX > 0 && getScrollX() + width > mTotalWidth) {                    
                scrollBy(-(int) distanceX, 0);
            }
            mTargetScrollX = getScrollX();
            hidePreview();
            invalidate();
            return true;
        }
    }
        
    public void setHighlightBackground(Drawable image) {
    	if (image != null) {
            mSelectionHighlight = image;
        } else {
            mSelectionHighlight = getContext().getResources().getDrawable(
                    R.drawable.list_selector_background_pressed);        	
        }
    }
    
    public void setCandidateColor(int color) {
    	mPrefColorRecommended = color;
    }
    
    public void setDivider(Drawable divider) {
    	mDivider = divider;
    }
    
    public void setColors(Integer normal, Integer recommended, Integer other, Integer highlight) {
    	final Resources res = getContext().getResources();
    	if (normal != null) {
    		mColorNormal = normal;
    	} else {
    		mColorNormal = res.getColor(R.color.candidate_normal);
    	}
    	if (mPrefColorRecommended != 0) {
    		mColorRecommended = mPrefColorRecommended;
    	} else if (recommended != null) {
    		mColorRecommended = recommended;
    	} else {
    		mColorRecommended = res.getColor(R.color.candidate_orange);
    	}    	
    	if (other != null) {
    		mColorOther = other;
    	} else {
    		mColorOther = res.getColor(R.color.candidate_other);
    	}
    	mColorHighlight = highlight;
    }
    
    public void setRTLSuggestions(boolean RTLsuggestions) {
    	mRTLSuggestions = RTLsuggestions;
    }
    
    /**
     * A connection back to the service to communicate with the text field
     * @param listener
     */
    public void setService(CandidateInputService listener) {
        mService = listener;
    }
    
    @Override
    public int computeHorizontalScrollRange() {
        return mTotalWidth;
    }

    /**
     * If the canvas is null, then only touch calculations are performed to pick the target
     * candidate.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
        }
        final int barWidth = getMeasuredWidth();
        mTotalWidth = 0;
        if (mSuggestions == null) return;
        
        final int height = getHeight();
        if (mBgPadding == null) {
            mBgPadding = new Rect(0, 0, 0, 0);
            if (getBackground() != null) {
                getBackground().getPadding(mBgPadding);
            }
            mDivider.setBounds(0, 0, mDivider.getIntrinsicWidth(),
                    mDivider.getIntrinsicHeight());
        }
        int x = 0;
        final int count = mSuggestions.size(); 
        //final int width = getWidth();
        final Rect bgPadding = mBgPadding;
        final Paint paint = mPaint;
        final int touchX = mTouchX;
        final int scrollX = getScrollX();
        final boolean scrolled = mScrolled;
        int highlightIndex = 0;
        if (mCurrentIndex == -1) {
        	// Initially highlighted word
        	highlightIndex = mTypedWordValid ? 0 : 1;
        } else {
        	highlightIndex = mCurrentIndex;
        }
        final int y = (int) (height + mPaint.getTextSize() - mDescent) / 2;

        for (int i = 0; i < count; i++) {
            CharSequence suggestion = mSuggestions.get(i);
            if (suggestion == null) continue;
            final int wordLength = suggestion.length();
            
        	// toString should be useless, but it seems needed to make bidi work with CM
            suggestion = suggestion.toString();
            paint.setColor(mColorNormal);
            if (mHaveMinimalSuggestion && highlightIndex == i) {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setColor(mColorRecommended);
            } else if (i != 0 || (wordLength == 1 && count > 1)) {
                // HACK: even if i == 0, we use mColorOther when this suggestion's length is 1 and
                // there are multiple suggestions, such as the default punctuation list.
                paint.setColor(mColorOther);
            }
            final int wordWidth;
            if (mWordWidth[i] != 0) {
                wordWidth = mWordWidth[i];
            } else {
                float textWidth =  paint.measureText(suggestion, 0, wordLength);
                wordWidth = Math.max(mMinTouchableWidth, (int) textWidth + X_GAP * 2);
                mWordWidth[i] = wordWidth;
            }

            mWordX[i] = x;

            if (touchX + scrollX >= x && touchX + scrollX < x + wordWidth && !scrolled &&
                    touchX != OUT_OF_BOUNDS) {
                if (canvas != null) {
                    canvas.translate(x, 0);
                    mSelectionHighlight.setBounds(0, bgPadding.top, wordWidth, height);
                    mSelectionHighlight.draw(canvas);
                    canvas.translate(-x, 0);
                    showPreview(i, null);
                    if (mColorHighlight != null) {
                    	paint.setColor(mColorHighlight);
                    }
                }
                mSelectedString = suggestion;
                mSelectedIndex = i;
            }

            if (canvas != null) {
            	// Check if it's worth displaying this word
            	if (x >= scrollX - barWidth && x < scrollX + 2 * barWidth) {
            		// Don't reverse the text if the option is set (for arabic)
            		CharSequence directionCorrectedSuggestion = mRTLSuggestions ?
            				Workarounds.workaroundCorrectStringDirection(suggestion) : suggestion;
            				canvas.drawText(directionCorrectedSuggestion, 0, directionCorrectedSuggestion.length(),
            						x + wordWidth / 2, y, paint);
            				paint.setColor(mColorOther);
            				canvas.translate(x + wordWidth, 0);
            				mDivider.draw(canvas);
            				canvas.translate(-x - wordWidth, 0);
            	}
            }
            paint.setTypeface(Typeface.DEFAULT);
            x += wordWidth;
        }
        mTotalWidth = x;
        if (mTargetScrollX != getScrollX()) {
            scrollToTarget();
        }
    }
    
    private void scrollToTarget() {
        if (mTargetScrollX > getScrollX()) {
            scrollBy(SCROLL_PIXELS, 0);
            if (getScrollX() >= mTargetScrollX) {
                scrollTo(mTargetScrollX, getScrollY());
                requestLayout();
            }
        } else {
        	scrollBy(-SCROLL_PIXELS, 0);
            if (getScrollX() <= mTargetScrollX) {
                scrollTo(mTargetScrollX, getScrollY());
                requestLayout();
            }
        }
        invalidate();
    }
    
    public void setSuggestions(List<CharSequence> suggestions, boolean completions,
            boolean typedWordValid, boolean haveMinimalSuggestion) {
        clear();
        if (suggestions != null) {
            mSuggestions = new ArrayList<CharSequence>(suggestions);
        }
        mShowingCompletions = completions;
        mTypedWordValid = typedWordValid;
        scrollTo(0, getScrollY());
        mTargetScrollX = 0;
        mHaveMinimalSuggestion = haveMinimalSuggestion;
        // Compute the total width
        onDraw(null);
        invalidate();
        requestLayout();
    }
    
    public void nextSuggestion(boolean setMinimalSuggestion) {
    	if (mSuggestions.size() == 0) {
    		// Avoid a division by 0
    		return;
    	}
    	if (setMinimalSuggestion) {
    		mHaveMinimalSuggestion = true;
    	}
    	if (mCurrentIndex == -1 && !setMinimalSuggestion) {
    		mCurrentIndex = (mTypedWordValid || !mHaveMinimalSuggestion) ? 0 : 1;
    	}
    	mCurrentIndex = (mCurrentIndex + 1) % mSuggestions.size();
    	// Update scroll position
    	int index = Math.max(mCurrentIndex - 1, 0);
        int leftEdge = mWordX[index];
        updateScrollPosition(leftEdge);
    	invalidate();
    }
    
    public CharSequence getCurrentSuggestion() {
    	return mSuggestions.get(mCurrentIndex);
    }
    
    public int getCurrentIndex() {
    	return mCurrentIndex;
    }

    public void scrollPrev() {
        int i = 0;
        final int count = mSuggestions.size();
        int firstItem = 0; // Actually just before the first item, if at the boundary
        while (i < count) {
            if (mWordX[i] < getScrollX() 
                    && mWordX[i] + mWordWidth[i] >= getScrollX() - 1) {
                firstItem = i;
                break;
            }
            i++;
        }
        int leftEdge = mWordX[firstItem] + mWordWidth[firstItem] - getWidth();
        if (leftEdge < 0) leftEdge = 0;
        updateScrollPosition(leftEdge);
    }
    
    public void scrollNext() {
        int i = 0;
        
        int targetX = getScrollX();
        final int count = mSuggestions.size();
        int rightEdge = getScrollX() + getWidth();
        while (i < count) {
            if (mWordX[i] <= rightEdge &&
                    mWordX[i] + mWordWidth[i] >= rightEdge) {
                targetX = Math.min(mWordX[i], mTotalWidth - getWidth());
                break;
            }
            i++;
        }
        updateScrollPosition(targetX);
    }

    private void updateScrollPosition(int targetX) {
        if (targetX != getScrollX()) {
            // TODO: Animate
            mTargetScrollX = targetX;
            requestLayout();
            invalidate();
            mScrolled = true;
        }
    }
    
    public void clear() {
        mSuggestions = EMPTY_LIST;
        mTouchX = OUT_OF_BOUNDS;
        mSelectedString = null;
        mSelectedIndex = -1;
        mCurrentIndex = -1;
        invalidate();
        Arrays.fill(mWordWidth, 0);
        Arrays.fill(mWordX, 0);
        if (mPreviewPopup.isShowing()) {
            mPreviewPopup.dismiss();
        }
    }
    
    /* package */
    public List<CharSequence> getSuggestions() {
        return mSuggestions;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me) {

        if (mGestureDetector.onTouchEvent(me)) {
            return true;
        }

        int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        mTouchX = x;

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mScrolled = false;
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
            if (y <= 0) {
                // Fling up!?
                if (mSelectedString != null) {
                    if (!mShowingCompletions) {
                        TextEntryState.acceptedSuggestion(mSuggestions.get(0),
                                mSelectedString);
                    }
                    mService.pickSuggestionManually(mSelectedIndex, mSelectedString);
                    mSelectedString = null;
                    mSelectedIndex = -1;
                }
            }
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            if (!mScrolled) {
                if (mSelectedString != null) {
                    if (!mShowingCompletions) {
                        TextEntryState.acceptedSuggestion(mSuggestions.get(0),
                                mSelectedString);
                    }
                    mService.pickSuggestionManually(mSelectedIndex, mSelectedString);
                }
            }
            mSelectedString = null;
            mSelectedIndex = -1;
            removeHighlight();
            hidePreview();
            requestLayout();
            break;
        }
        return true;
    }
    
    /**
     * For flick through from keyboard, call this method with the x coordinate of the flick 
     * gesture.
     * @param x
     */
    public void takeSuggestionAt(float x) {
        mTouchX = (int) x;
        // To detect candidate
        onDraw(null);
        if (mSelectedString != null) {
            if (!mShowingCompletions) {
                TextEntryState.acceptedSuggestion(mSuggestions.get(0), mSelectedString);
            }
            mService.pickSuggestionManually(mSelectedIndex, mSelectedString);
        }
        invalidate();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_REMOVE_THROUGH_PREVIEW), 200);
    }

    private void hidePreview() {
        mCurrentWordIndex = OUT_OF_BOUNDS;
        if (mPreviewPopup.isShowing()) {
            mHandler.sendMessageDelayed(mHandler
                    .obtainMessage(MSG_REMOVE_PREVIEW), 60);
        }
    }

    private void showPreview(int wordIndex, String altText) {
        int oldWordIndex = mCurrentWordIndex;
        mCurrentWordIndex = wordIndex;
        // If index changed or changing text
        if (oldWordIndex != mCurrentWordIndex || altText != null) {
            if (wordIndex == OUT_OF_BOUNDS) {
                hidePreview();
            } else {
                CharSequence word = altText != null? altText : mSuggestions.get(wordIndex);
                mPreviewText.setText(word);
                mPreviewText.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int wordWidth = (int) (mPaint.measureText(word, 0, word.length()) + X_GAP * 2);
                final int popupWidth = wordWidth
                        + mPreviewText.getPaddingLeft() + mPreviewText.getPaddingRight();
                final int popupHeight = mPreviewText.getMeasuredHeight();
                //mPreviewText.setVisibility(INVISIBLE);
                mPopupPreviewX = mWordX[wordIndex] - mPreviewText.getPaddingLeft() - getScrollX();
                mPopupPreviewY = - popupHeight;
                mHandler.removeMessages(MSG_REMOVE_PREVIEW);
                int [] offsetInWindow = new int[2];
                getLocationInWindow(offsetInWindow);
                if (mPreviewPopup.isShowing()) {
                    mPreviewPopup.update(mPopupPreviewX, mPopupPreviewY + offsetInWindow[1],
                            popupWidth, popupHeight);
                } else {
                    mPreviewPopup.setWidth(popupWidth);
                    mPreviewPopup.setHeight(popupHeight);
                    mPreviewPopup.showAtLocation(this, Gravity.NO_GRAVITY, mPopupPreviewX,
                            mPopupPreviewY + offsetInWindow[1]);
                }
                mPreviewText.setVisibility(VISIBLE);
            }
        }
    }

    private void removeHighlight() {
        mTouchX = OUT_OF_BOUNDS;
        invalidate();
    }
    
    private void longPressFirstWord() {
        CharSequence word = mSuggestions.get(0);
        if (mService.addTypedWordToDictionary()) {
        	StringBuilder sb = new StringBuilder();
        	sb.append(word);
        	sb.append(" : ");
        	sb.append(getContext().getResources().getString(R.string.saved));
            showPreview(0, sb.toString());
        }
    }
    
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hidePreview();
    }
}
