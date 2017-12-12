package com.paragyte.ocrproject;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.paragyte.ocrproject.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.paragyte.ocrproject.Constants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE;
import static com.paragyte.ocrproject.Constants.FILE_URI;
import static com.paragyte.ocrproject.Constants.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btnTakePicture) Button mBtnTakePicture;
    @BindView(R.id.btnChoosePicture) Button mBtnChoosePicture;

    private Uri fileUri; // file url to store image/video
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        utility = new Utility();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @OnClick(R.id.btnTakePicture)
    public void onTakePictureClicked(){
        Log.d(Constants.TAG, "onTakePictureClicked: ");
        captureImage();
//        Intent intent = new Intent(this, CaptureImageActivity.class);
//        startActivity(intent);
    }

    private void captureImage() {
        Log.d(Constants.TAG, "captureImage: ");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = utility.getOutputMediaFileUri(MEDIA_TYPE_IMAGE, this);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FILE_URI, fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable(FILE_URI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Constants.TAG, "onActivityResult: ");
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ResultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(FILE_URI, fileUri);
                intent.putExtras(bundle);
                startActivity(intent);
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void requestPermissions(){

    }
}
