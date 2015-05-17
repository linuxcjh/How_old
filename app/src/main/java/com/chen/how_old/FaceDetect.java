package com.chen.how_old;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2015/5/13.
 */
public class FaceDetect {


    public interface CallBack {
        void success(JSONObject result);

        void error(FaceppParseException exception);
    }

    public static void detect(final Bitmap bitmap, final CallBack callBack) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpRequests requests = new HttpRequests(HoConstant.API_KEY, HoConstant.SECRET_KEY, true, true);
                    Bitmap bmSmall = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] arrays = stream.toByteArray();

                    PostParameters parameters = new PostParameters();
                    parameters.setImg(arrays);
                    JSONObject jsonObject = requests.detectionDetect(parameters);

                    Log.v("TAG", jsonObject.toString());
                    if(callBack!=null){
                        callBack.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if(callBack!=null){
                        callBack.error(e);
                    }
                }


            }
        }).start();
    }
}
