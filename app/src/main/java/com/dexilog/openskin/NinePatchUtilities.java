/*
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

package com.dexilog.openskin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

public class NinePatchUtilities {

	private static boolean DEBUG = false;


	@SuppressWarnings("serial")
	public static class NinePatchException extends RuntimeException {

		public final int x;
		public final int y;
		public final int pixel;


		public NinePatchException(int x, int y, int pixel) {
			super(String.format("invalid pixel found at x=%d,y=%d: %08X", x, y, pixel));

			this.x = x;
			this.y = y;
			this.pixel = pixel;
		}

	}


	public static NinePatchDrawable decodeNinePatchDrawable(InputStream inputStream) throws IOException, NinePatchException {
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

		int width = bitmap.getWidth();
		int innerWidth = width - 2;

		if (innerWidth < 1)
			throw new IllegalArgumentException("width must be >= 3");

		int height = bitmap.getHeight();
		int innerHeight = height - 2;

		if (innerHeight < 1)
			throw new IllegalArgumentException("height must be >= 3");

		int[] row = new int[innerWidth]; 
		bitmap.getPixels(row, 0, innerWidth, 1, 0, innerWidth, 1);

		DivsResult divsResult = getDivs(row);

		if (!divsResult.ok)
			throw new NinePatchException(
					divsResult.badIndex + 1, 
					0, 
					row[divsResult.badIndex]);

		List<Integer> xDivs = divsResult.divs;
		List<Integer> xStretchableDivs = divsResult.stretchableDivs;

		if (DEBUG) {
			System.err.println("xDivs=" + xDivs);
			System.err.println("xStretchableDivs=" + xStretchableDivs);
		}

		int[] column = new int[innerHeight];
		bitmap.getPixels(column, 0, 1, 0, 1, 1, innerHeight);

		divsResult = getDivs(column);

		if (!divsResult.ok)
			throw new NinePatchException(
					0, 
					divsResult.badIndex + 1, 
					row[divsResult.badIndex]);

		List<Integer> yDivs = divsResult.divs;
		List<Integer> yStretchableDivs = divsResult.stretchableDivs;

		if (DEBUG) {
			System.err.println("yDivs=" + yDivs);
			System.err.println("yStretchableDivs=" + yStretchableDivs);
		}

		int y = height - 1;
		bitmap.getPixels(row, 0, innerWidth, 1, y, innerWidth, 1);

		PaddingResult paddingResult = getNinePatchPadding(row);

		if (!paddingResult.ok)
			throw new NinePatchException(
					paddingResult.badIndex + 1, 
					y, 
					row[paddingResult.badIndex]);

		int paddingLeft = paddingResult.padding0;
		int paddingRight = paddingResult.padding1;

		if (DEBUG)
			System.err.printf("xPadding=%d,%d\n", paddingLeft, paddingRight);

		int x = width - 1;
		bitmap.getPixels(column, 0, 1, x, 1, 1, innerHeight);

		paddingResult = getNinePatchPadding(column);

		if (!paddingResult.ok)
			throw new NinePatchException(
					x, 
					paddingResult.badIndex + 1, 
					column[paddingResult.badIndex]);

		int paddingTop = paddingResult.padding0;
		int paddingBottom = paddingResult.padding1;

		if (DEBUG)
			System.err.printf("yPadding=%d,%d\n", paddingTop, paddingBottom);

		int columnCount = xDivs.size() - 1;
		int rowCount = yDivs.size() - 1;
		int colorCount = columnCount * rowCount;

		int xDivCount = xStretchableDivs.size();
		int yDivCount = yStretchableDivs.size();

		byte[] chunk = new byte[
		                        4 // int8_t wasDeserialized, numXDivs, numYDivs, numColors
		                        + 4 // int32_t* xDivs
		                        + 4 // int32_t* yDivs
		                        + 8 // int32_t paddingLeft, paddingRight
		                        + 8 // int32_t paddingTop, paddingBottom
		                        + 4 // uint32_t* colors
		                        + 4 * xDivCount
		                        + 4 * yDivCount
		                        + 4 * colorCount
		                        ];

		ByteBuffer buffer = ByteBuffer.wrap(chunk);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		buffer.put((byte)1);
		buffer.put((byte)xDivCount);
		buffer.put((byte)yDivCount);
		buffer.put((byte)colorCount);
		buffer.putInt(0); // int32_t* xDivs
		buffer.putInt(0); // int32_t* yDivs
		buffer.putInt(paddingLeft);
		buffer.putInt(paddingRight);
		buffer.putInt(paddingTop);
		buffer.putInt(paddingBottom);
		buffer.putInt(0); // uint32_t* colors

		for (Integer xDiv : xStretchableDivs)
			buffer.putInt(xDiv);

		for (Integer yDiv : yStretchableDivs)
			buffer.putInt(yDiv);

		for (int i = 0; i < colorCount; i++)
			buffer.putInt(1);

		bitmap = Bitmap.createBitmap(bitmap, 1, 1, innerWidth, innerHeight);

		return new NinePatchDrawable(
				bitmap,
				chunk, 
				new Rect(paddingLeft, paddingTop, paddingRight, paddingBottom),
		"src");
	}


	private static class DivsResult {

		boolean ok;
		List<Integer> divs = new LinkedList<Integer>();
		List<Integer> stretchableDivs = new LinkedList<Integer>();
		int badIndex;

	}

	private static DivsResult getDivs(int[] pixels) {
		DivsResult result = new DivsResult();
		result.ok = true;

		boolean inBlack = false;

		result.divs.add(0);

		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];

			//System.err.printf("%d=%08X\n", i, pixel);

			if (inBlack) {
				if (pixel == Color.TRANSPARENT) {
					inBlack = false;
					result.divs.add(i);
					result.stretchableDivs.add(i);
				}
				else if (pixel != Color.BLACK) {
					result.ok = false;
					result.badIndex = i;
					return result;
				}
			}
			else {
				if (pixel == Color.BLACK) {
					inBlack = true;
					result.divs.add(i);
					result.stretchableDivs.add(i);
				}
				else if (pixel != Color.TRANSPARENT) {
					result.ok = false;
					result.badIndex = i;
					return result;
				}
			}
		}

		result.divs.add(pixels.length);

		if (inBlack)
			result.stretchableDivs.add(pixels.length);

		return result;
	}

	private static class PaddingResult {

		boolean ok;
		int padding0;
		int padding1;
		int badIndex;

	}

	private static PaddingResult getNinePatchPadding(int[] pixels) {
		PaddingResult result = new PaddingResult();
		result.ok = true;

		boolean inBlack = false;

		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];

			//System.err.printf("%d=%08X\n", i, pixel);

			if (inBlack) {
				if (pixel == Color.TRANSPARENT) {
					result.padding1 = pixels.length - i;
					break;
				}
				else if (pixel != Color.BLACK) {
					result.ok = false;
					result.badIndex = i;
					return result;
				}
			}
			else {
				if (pixel == Color.BLACK) {
					inBlack = true;
					result.padding0 = i;
				}
				else if (pixel != Color.TRANSPARENT) {
					result.ok = false;
					result.badIndex = i;
					return result;
				}
			}
		}

		return result;
	}

}
