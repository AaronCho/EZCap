package com.example.suro.recordscreen;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import java.text.SimpleDateFormat;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_CODE = 1;
    private int mScreenDensity;
    private DisplayMetrics mDisplayMetrics;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MediaRecorder mMediaRecorder;
    
    TextView textView; 
    private int clo = 0;
    public ImageButton Video, Camera, Record, Gallery, Share, Help, Set;
    static final int REQUEST_VIDEO_CAPTURE = 1, REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    ImageView view;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Video=(ImageButton)findViewById(R.id.vi_video);
        Video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               Intent intent = new Intent();
               intent.setAction("android.media.action.VIDEO_CAPTURE");
               intent.addCategory("android.intent.category.DEFAULT");

               File file = new File(Environment.getExternalStorageDirectory() + "/" + new SimpleDateFormat("HH-mm-ss").format(new Date()) + "000.3gp");
               Uri uri = Uri.fromFile(file);  
               intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
               startActivity(intent);
            }
        });
        
        //------------------------------Camera Fuction--------------------------------------------
        Camera = (ImageButton)findViewById(R.id.camera);
        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent();
               intent.setAction("android.media.action.IMAGE_CAPTURE");
               intent.addCategory("android.intent.category.DEFAULT");
   
               File file = new File(Environment.getExternalStorageDirectory() + "/" + new SimpleDateFormat("HH-mm-ss").format(new Date()) + "000.jpg");
   
               Uri uri = Uri.fromFile(file);
   
               intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
   
               startActivity(intent); 
            }
       }
   );



   Help = (ImageButton)findViewById(R.id.help);
   Help.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
         startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.csun.edu/~ny607216/EZCapHelpButton.html")));

      }
   });

   DisplayMetrics metrics = new DisplayMetrics();
   getWindowManager().getDefaultDisplay().getMetrics(metrics);
   mScreenDensity = metrics.densityDpi;

   mMediaRecorder = new MediaRecorder();
   initRecorder();
   prepareRecorder();

   mProjectionManager = (MediaProjectionManager) getSystemService
           (Context.MEDIA_PROJECTION_SERVICE);

   mToggleButton = (ToggleButton) findViewById(R.id.toggle);
   mToggleButton.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
           onToggleScreenShare(v);
       }
   });

   mMediaProjectionCallback = new MediaProjectionCallback();
    

   @Override
   public void onDestroy() {
       super.onDestroy();
       if (mMediaProjection != null) {
           mMediaProjection.stop();
           mMediaProjection = null;
       }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode != PERMISSION_CODE) {
           Log.e(TAG, "Unknown request code: " + requestCode);
           return;
       }
       if (resultCode != RESULT_OK) {
           Toast.makeText(this,
                   "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
           mToggleButton.setChecked(false);
           return;
       }
       mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
       mMediaProjection.registerCallback(mMediaProjectionCallback, null);
       mVirtualDisplay = createVirtualDisplay();
       mMediaRecorder.start();
    }

   public void onToggleScreenShare(View view) {
       if (((ToggleButton) view).isChecked()) {
           shareScreen();
       } else {
           mMediaRecorder.stop();
           mMediaRecorder.reset();
           Log.v(TAG, "Recording Stopped");
           stopScreenSharing();
           initRecorder();
           prepareRecorder();
       }
   }

   private void shareScreen() {
       if (mMediaProjection == null) {
           startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
           return;
       }
       mVirtualDisplay = createVirtualDisplay();
       mMediaRecorder.start();
   }

   private void stopScreenSharing() {
       if (mVirtualDisplay == null) {
           return;
       }
       mVirtualDisplay.release();
       //mMediaRecorder.release();
   }

   private VirtualDisplay createVirtualDisplay() {
       DisplayMetrics metrics = new DisplayMetrics();
       getWindowManager().getDefaultDisplay().getMetrics(metrics);
       int width = metrics.widthPixels;
       int height = metrics.heightPixels;
       return mMediaProjection.createVirtualDisplay("MainActivity",
               width, height, mScreenDensity,
               DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
               mMediaRecorder.getSurface(), null /*Callbacks*/, null /*Handler*/);
   }

   private class MediaProjectionCallback extends MediaProjection.Callback {
       @Override
       public void onStop() {
           if (mToggleButton.isChecked()) {
               mToggleButton.setChecked(false);
               mMediaRecorder.stop();
               mMediaRecorder.reset();
               Log.v(TAG, "Recording Stopped");
               initRecorder();
               prepareRecorder();
           }
           mMediaProjection = null;
           stopScreenSharing();
           Log.i(TAG, "MediaProjection Stopped");
       }
   }
   
   public String getCurSysDate() {
       return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
   }

   private void prepareRecorder() {
       try {
           mMediaRecorder.prepare();
       } catch (IllegalStateException | IOException e) {
           e.printStackTrace();
           finish();
       }

   }

   private void initRecorder() {
       DisplayMetrics metrics = new DisplayMetrics();
       getWindowManager().getDefaultDisplay().getMetrics(metrics);
       int width = metrics.widthPixels;
       int height = metrics.heightPixels;

       final String directory = Environment.getExternalStorageDirectory() + File.separator + "Recordings";
       if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
           Toast.makeText(this, "Failed to get External Storage", Toast.LENGTH_SHORT).show();
           return;
       }
       final File folder = new File(directory);
       boolean success = true;
       if (!folder.exists()) {
           success = folder.mkdir();
       }
       String filePath;
       if (success) {
           String videoName = ("capture_" + getCurSysDate() + ".mp4");
           filePath = directory + File.separator + videoName;
       } else {
           Toast.makeText(this, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show();
           return;
       }

       mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
       mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
       mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
       mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
       mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
       mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
       mMediaRecorder.setVideoFrameRate(30);
       mMediaRecorder.setVideoSize(width,height);
       mMediaRecorder.setOutputFile(filePath);
   }
}