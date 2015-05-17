package com.chen.how_old;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {


    private static final int PICK_CODE = 0x110;
    private ImageView imageView;
    private Button getImageBt, selectImageBt;
    private String mCurrentPhotoStr;
    private Bitmap mPhotoImg;
    private Paint mPaint;
    private Drawable drawablemale;
    private Drawable drawablfemale;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageview);
        getImageBt = (Button) findViewById(R.id.get_image);
        getImageBt.setOnClickListener(this);
        selectImageBt = (Button) findViewById(R.id.select_image);
        selectImageBt.setOnClickListener(this);
        initViews();
    }

    private void initViews() {
        mPaint = new Paint();
        drawablemale= getResources().getDrawable(R.drawable.male);
        drawablfemale= getResources().getDrawable(R.drawable.female);
        /// 这一步必须要做,否则不会显示.
        drawablemale.setBounds(0, 0, drawablemale.getMinimumWidth(), drawablemale.getMinimumHeight());
        drawablfemale.setBounds(0, 0, drawablfemale.getMinimumWidth(), drawablfemale.getMinimumHeight());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_image:

                if(!TextUtils.isEmpty(mCurrentPhotoStr)){
                    resizePhoto();
                }else{
                    mPhotoImg=BitmapFactory.decodeResource(getResources(),R.drawable.t4);
                }
                FaceDetect.detect(mPhotoImg, new FaceDetect.CallBack() {
                    @Override
                    public void success(JSONObject result) {
                        mHandler.sendMessage(mHandler.obtainMessage(0x11, result));
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        mHandler.sendMessage(mHandler.obtainMessage(0x12, exception));
                    }
                });
                break;
            case R.id.select_image:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_CODE);
                break;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0x11:
                    JSONObject js = (JSONObject) msg.obj;
                    prepareRsBitmap(js);
                    imageView.setImageBitmap(mPhotoImg);
                    break;
                case 0x12:
                    String error = (String) msg.obj;
                    if (!TextUtils.isEmpty(error)) {
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void prepareRsBitmap(JSONObject jsonObject) {

        Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(), mPhotoImg.getHeight(), mPhotoImg.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mPhotoImg, 0, 0, null);
        try {
            JSONArray faces = jsonObject.getJSONArray("face");
            int faceCount = faces.length();

            for (int i = 0; i < faceCount; i++) {
                JSONObject faceObject = faces.getJSONObject(i);
                JSONObject posObj = faceObject.getJSONObject("position");

                float x = (float) posObj.getJSONObject("center").getDouble("x");
                float y = (float) posObj.getJSONObject("center").getDouble("y");

                float w = (float) posObj.getDouble("width");
                float h = (float) posObj.getDouble("height");

                x = x / 100 * bitmap.getWidth();
                y = y / 100 * bitmap.getHeight();

                w = w / 100 * bitmap.getHeight();
                h = h / 100 * bitmap.getHeight();

                mPaint.setColor(0xFFFFFFFF);
                mPaint.setStrokeWidth(3);
                //box
                canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y - h / 2, x + w / 2, y - h / 2, mPaint);
                canvas.drawLine(x + w / 2, y - h / 2, x + w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y + h / 2, x + w / 2, y + h / 2, mPaint);

                //get age and gender

                int age = faceObject.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender = faceObject.getJSONObject("attribute").getJSONObject("gender").getString("value");

                Bitmap ageBitmap = buildAgeBitmap(age, "Male".equals(gender));
                int ageWidth = ageBitmap.getWidth();
                int ageHeight = ageBitmap.getHeight();

                if (bitmap.getWidth() < mPhotoImg.getWidth() && bitmap.getHeight() < mPhotoImg.getHeight()) {

                    float ratio = Math.max(bitmap.getWidth() * 1.0f / mPhotoImg.getWidth(), bitmap.getHeight() * 1.0f / mPhotoImg.getHeight());
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap, (int) (ageWidth * ratio), (int) (ageHeight * ratio), false);
                }
                canvas.drawBitmap(ageBitmap,x-ageBitmap.getWidth()/2,y-h/2-ageBitmap.getHeight(),null);
                mPhotoImg = bitmap;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap buildAgeBitmap(int age, boolean isMale) {

        TextView textView = (TextView) findViewById(R.id.age_and_gender);
        textView.setText(age + "");
        textView.setTextColor(Color.YELLOW);
        if (isMale) {
            textView.setCompoundDrawables(drawablemale, null, null, null);
            // textView.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.male, null), null, null, null);
        } else {
            textView.setCompoundDrawables(drawablfemale, null, null, null);
            // textView.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(R.drawable.female, null), null, null, null);
        }
        textView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(textView.getDrawingCache());
        textView.destroyDrawingCache();

        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_CODE) {
            if (data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurrentPhotoStr = cursor.getString(idx);
                cursor.close();
                resizePhoto();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 图片压缩
     */
    private void resizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoStr, options);
        double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);
        options.inSampleSize = (int) Math.ceil(ratio);
        options.inJustDecodeBounds = false;
        mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
        imageView.setImageBitmap(mPhotoImg);

    }
}
