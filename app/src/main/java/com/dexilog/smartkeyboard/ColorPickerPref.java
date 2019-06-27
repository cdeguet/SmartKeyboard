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


import android.preference.DialogPreference;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class ColorPickerPref extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	
    private SeekBar mRedBar;
    private SeekBar mGreenBar;
    private SeekBar mBlueBar;
    private int mCurColor;
    
    private ColorPickerView mPicker;
    
    public ColorPickerPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    } 
    
    @Override
    protected View onCreateDialogView() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setBackgroundColor(0xFFFFFFFF);
        layout.setOrientation(LinearLayout.VERTICAL);
       
    	mCurColor = getPersistedInt(0xFFE35900);
        
        final float scale = getContext().getResources().getDisplayMetrics().density;
        final int padding = (int)(5f * scale);
    	
        mRedBar = new SeekBar(getContext());
        mRedBar.setBackgroundColor(0xFFFF0000);
        mRedBar.setMax(255);
        mRedBar.setProgress(Color.red(mCurColor));
        mRedBar.setPadding(padding, padding, padding, padding);
        layout.addView(mRedBar);
        
        mGreenBar = new SeekBar(getContext());
        mGreenBar.setBackgroundColor(0xFF00FF00);
        mGreenBar.setMax(255);
        mGreenBar.setProgress(Color.green(mCurColor));
        mGreenBar.setPadding(padding, padding, padding, padding);
        layout.addView(mGreenBar);
        
        mBlueBar = new SeekBar(getContext());
        mBlueBar.setBackgroundColor(0xFF0000FF);
        mBlueBar.setMax(255);
        mBlueBar.setProgress(Color.blue(mCurColor));
        mBlueBar.setPadding(padding, padding, padding, padding);
        layout.addView(mBlueBar);
        
        mRedBar.setOnSeekBarChangeListener(this);
        mGreenBar.setOnSeekBarChangeListener(this);
        mBlueBar.setOnSeekBarChangeListener(this);
        
        mPicker = new ColorPickerView(getContext());
    	layout.addView(mPicker);
    	
    	return layout;
    }
        
    protected void onDialogClosed(boolean positiveResult) { 
        if (positiveResult) {
        	persistInt(mCurColor);
        } 
        super.onDialogClosed(positiveResult);
    }

    private class ColorPickerView extends View {
        private Paint mPaint;
        private int mSize;
        StringBuilder mColorString = new StringBuilder();

        ColorPickerView(Context c) {
            super(c);
 
            final float scale = getContext().getResources().getDisplayMetrics().density;
            mSize = (int)(50f * scale);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setTextSize(20f * scale);
        }

        @Override 
        protected void onDraw(Canvas canvas) {           
            mPaint.setColor(mCurColor);
            canvas.translate((int)(mSize * 0.2), (int)(mSize * 0.2));
            canvas.drawRect(0, 0, mSize, mSize, mPaint);
            
            mPaint.setColor(0xFF000000);
            canvas.translate((int)(mSize * 1.3), (int)(mSize * 0.5));
            String text = Integer.toHexString(mCurColor);
            mColorString.setLength(0);
            mColorString.append('#');
            mColorString.append(text);
            canvas.drawText(mColorString, 0, mColorString.length(), 0, 0, mPaint);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension((int)(mSize * 4), (int)(mSize * 1.5));
        }
    }
    

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
    		boolean fromUser) {
    	mCurColor = Color.rgb(mRedBar.getProgress(), mGreenBar.getProgress(), mBlueBar.getProgress());
    	mPicker.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {				
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {				
    }

}