package com.paragyte.ocrproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.FrameLayout;
import com.paragyte.ocrproject.classes.CameraPreview;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.paragyte.ocrproject.Constants.FILE_URI;

public class CaptureImageActivity extends AppCompatActivity {
    @BindView(R.id.activity_cature_image_preview_frame) FrameLayout mFramePreview;
    @BindView(R.id.activity_capture_image_btn_capture)
    Button mBtnCapture;

    private CameraPreview mCameraPreview;
    private Camera mCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        checkDeviceCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDeviceCamera();
        mCameraPreview = new CameraPreview(this, mCamera);
        mFramePreview.addView(mCameraPreview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mCamera) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @OnClick(R.id.activity_capture_image_btn_capture)
    public void onCaptureClicked(){
        mCamera.takePicture(null,null, pictureCallback);
    }

    private Camera checkDeviceCamera(){
        try {
            mCamera = Camera.open();
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mCamera.stopPreview();
            File pictureFile = getOutputMediaFile(Constants.MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(Constants.TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Intent intent = new Intent(CaptureImageActivity.this, ResultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(FILE_URI, getOutputMediaFileUri(Constants.MEDIA_TYPE_IMAGE));
                intent.putExtras(bundle);
                startActivity(intent);
            }catch (FileNotFoundException e) {
                Log.d(Constants.TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(Constants.TAG, "Error accessing file: " + e.getMessage());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OcrProject");
        if(!mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(Constants.TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type == Constants.MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath()+ File.separator+"IMG_"+timeStamp+".jpg");
        }else {
            return null;
        }
        return mediaFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            mCamera.stopPreview();
            mCamera.release();
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
