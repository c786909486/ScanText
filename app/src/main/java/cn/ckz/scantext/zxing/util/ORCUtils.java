package cn.ckz.scantext.zxing.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.google.zxing.Result;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.ckz.scantext.R;

import static android.content.ContentValues.TAG;
import static com.google.zxing.BarcodeFormat.RSS_14;

/**
 * Created by CKZ on 2017/9/14.
 */

public class ORCUtils {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    public static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tesseract";
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static final String DEFAULT_LANGUAGE = "ch";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;



    /**
     * 将裁剪的图片保存到文件夹里
     *
     * @param bitmap 要识别的图片
     */
    public static void saveBitmap(Bitmap bitmap) {
        Log.e(TAG, "保存图片");
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "img", "Card_number");
        if (f.exists()) {
            f.delete();
        } else {
            f.mkdirs();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * 对要识别的图像进行识别
     *
     * @param bitmap 要识别的bitmap
     * @return
     */
    public static String getResult(Bitmap bitmap) {
        String data;

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATAPATH, "ch");
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        baseApi.setImage(bitmap);
//        baseApi.setVariable("tessedit_char_whitelist", "0123456789"); //暂时只识别银行卡卡号()
        data = baseApi.getUTF8Text();
//        data = data.replaceAll("\\s*", "");
        if (data.equals("")) {
            data = null;
        }
        baseApi.end();
        return data;

    }


    /**
     * 获取语言包处理
     *
     * @param activity
     */
    public static void copyToSD(Activity activity) {
        Log.i(TAG, "copyToSD:LanguagePath " + LANGUAGE_PATH);
        Log.i(TAG, "copyToSD:LanguageName " + DEFAULT_LANGUAGE_NAME);
        Log.i(TAG, "copyToSD:DataPath " + DATAPATH);
        Log.i(TAG, "copyToSD:tessData " + tessdata);
        //如果存在就删掉
        File f = new File(tessdata);
        if (!f.exists()) {
            f.mkdirs();
        }
//        if (!f.exists()) {
//            File p = new File(f.getParent());
//            if (!p.exists()) {
//                p.mkdirs();
//            }
//            try {
//                f.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = activity.getAssets().open(DEFAULT_LANGUAGE_NAME);
            File file = new File(LANGUAGE_PATH);
            os = new FileOutputStream(file);
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

