/*
 * Copyright (C) 2010 ZXing authors
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

package cn.ckz.scantext.zxing.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

import cn.ckz.scantext.R;
import cn.ckz.scantext.zxing.app.CaptureActivity;
import cn.ckz.scantext.zxing.camera.CameraManager;
import cn.ckz.scantext.zxing.util.ORCUtils;

final class DecodeHandler extends Handler
{

	private static final String TAG = DecodeHandler.class.getSimpleName();

	private final CaptureActivity activity;
	private final MultiFormatReader multiFormatReader;
    private String scanType;

	DecodeHandler(CaptureActivity activity, Hashtable<DecodeHintType, Object> hints,String scanType)
	{
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
		this.scanType = scanType;
	}

	@Override
	public void handleMessage(Message message)
	{
		if (message.what == R.id.decode)
		{
			// Log.d(TAG, "Got decode message");
			decode((byte[]) message.obj, message.arg1, message.arg2);
		}
		else if (message.what == R.id.quit)
		{
			Looper.myLooper().quit();
	}
	}

	/**
	 * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
	 * reuse the same reader objects from one decode to the next.
	 *
	 * @param data   The YUV preview frame.
	 * @param width  The width of the preview frame.
	 * @param height The height of the preview frame.
	 */
	private void decode(byte[] data, int width, int height)
	{
		long start = System.currentTimeMillis();
		Result rawResult = null;
		String result = null;
		/***********************�޸�Ϊ������ʼ******************************/
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}
		int tmp = width; // Here we are swapping, that's the difference to #11
		width = height;
		height = tmp;
		data = rotatedData;
		/***********************�޸�Ϊ��������******************************/
		PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height,scanType);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		try
		{
		    if (scanType.equals(CaptureActivity.SCAN_TYPE_QRCODESCAN)) {
                rawResult = multiFormatReader.decodeWithState(bitmap);
                Log.d(TAG,"QRCode");
            }else {
		        result = ORCUtils.getResult(source.renderCroppedGreyscaleBitmap());
				Log.d(TAG,"ORC");
            }

		}
		catch (ReaderException re)
		{
			// continue
		}
		finally
		{
			multiFormatReader.reset();
		}

		if (rawResult != null || result !=null)
		{
			long end = System.currentTimeMillis();

            Bundle bundle = new Bundle();
            Log.d(TAG,scanType);
			if (scanType.equals(CaptureActivity.SCAN_TYPE_QRCODESCAN)){
                Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
                bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
                message.setData(bundle);
                // Log.d(TAG, "Sending decode succeeded message...");
				Log.d(TAG,"QRCodeResult");
				Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
                message.sendToTarget();
            }else if (scanType.equals(CaptureActivity.SCAN_TYPE_BANK_CARD)){
                Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, result);
                bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
                message.setData(bundle);
                // Log.d(TAG, "Sending decode succeeded message...");
				Log.d(TAG,"ORCResult");
                message.sendToTarget();
            }
		}
		else
		{
			Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
			message.sendToTarget();
		}
	}

}
