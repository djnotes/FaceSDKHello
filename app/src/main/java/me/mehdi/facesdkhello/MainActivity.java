package me.mehdi.facesdkhello;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.luxand.FSDK;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE = 100;
    protected FSDK.HImage oldpicture;
    private static int RESULT_LOAD_IMAGE = 1;
    protected boolean processing;


    // Subclass for async processing of FaceSDK functions.
    // If long-run task runs in foreground - Android kills the process.
    private class DetectFaceInBackground extends AsyncTask<String, Void, String> {
        protected FSDK.FSDK_Features features;
        protected FSDK.TFacePosition faceCoords;
        protected String picturePath;
        protected FSDK.HImage picture;
        protected int result;

        @Override
        protected String doInBackground(String... params) {
            String log = new String();
            picturePath = params[0];
            faceCoords = new FSDK.TFacePosition();
            faceCoords.w = 0;
            picture = new FSDK.HImage();
            result = FSDK.LoadImageFromFile(picture, picturePath);
            if (result == FSDK.FSDKE_OK) {
                result = FSDK.DetectFace(picture, faceCoords);
                features = new FSDK.FSDK_Features();
                if (result == FSDK.FSDKE_OK) {
                    //DEBUG
                    //FSDK.SetFaceDetectionThreshold(1);
                    //FSDK.SetFaceDetectionParameters(false, false, 70);
                    //long t0 = System.currentTimeMillis();
                    //for (int i=0; i<10; ++i)
                    //result = FSDK.DetectFacialFeatures(picture, features);
                    result = FSDK.DetectFacialFeaturesInRegion(picture, faceCoords, features);
                    //Log.d("TT", "TIME: " + ((System.currentTimeMillis()-t0)/10.0f));
                }
            }
            processing = false; //long-running code is complete, now user may push the button
            return log;
        }

        @Override
        protected void onPostExecute(String resultstring) {
            TextView tv = (TextView) findViewById(R.id.textView1);

            if (result != FSDK.FSDKE_OK)
                return;

            ImageView imageView =  findViewById(R.id.imageView1);

            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            tv.setText(resultstring);

//            imageView.detectedFace = faceCoords;


            if (features.features[0] != null); // if detected
//                imageView.facial_features = features;

            int [] realWidth = new int[1];
            FSDK.GetImageWidth(picture, realWidth);
//            imageView.faceImageWidthOrig = realWidth[0];
            imageView.invalidate(); // redraw, marking up faces and features

            if (oldpicture != null)
                FSDK.FreeImage(oldpicture);
            oldpicture = picture;
        }

        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
    //end of DetectFaceInBackground class



    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processing = true; //prevent user from pushing the button while initializing

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //using res/layout/activity_main.xml

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);

        TextView tv = (TextView) findViewById(R.id.textView1);

        try {
            int res = FSDK.ActivateLibrary(getString(R.string.luxand_license));
            FSDK.Initialize();
            FSDK.SetFaceDetectionParameters(false, false, 100);
            FSDK.SetFaceDetectionThreshold(5);

            if (res == FSDK.FSDKE_OK) {
                tv.setText(R.string.license_ok_welcome);
            } else {
                tv.setText(R.string.license_invalid_quitting);
            }
        }
        catch (Exception e) {
            tv.setText("exception " + e.getMessage());
        }

        // Adding button
        Button buttonLoadImage1 = (Button) findViewById(R.id.buttonLoadImage);
        buttonLoadImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg) {
                if (!processing) {
                    processing = true;
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            }
        });

        processing = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            TextView tv = (TextView) findViewById(R.id.textView1);
            tv.setText("processing...");
            new DetectFaceInBackground().execute(picturePath);
        } else {
            processing = false;
        }
    }
}
