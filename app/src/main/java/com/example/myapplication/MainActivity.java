package com.example.myapplication;
import static org.opencv.imgproc.Imgproc.boundingRect;

import android.Manifest;
import org.jetbrains.annotations.NotNull;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class MainActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    Mat rgb,hsv,mask;
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    rgb=new Mat();
    hsv=new Mat();
    mask=new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        rgb.release();
        hsv.release();
        mask.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        rgb = inputFrame.rgba();




        Scalar lowerColorBound = new Scalar(0, 50, 50); // REDD
        Scalar upperColorBound = new Scalar(10, 255,255);

// convert to HSV, necessary to use inRange()
        Imgproc.cvtColor(rgb,hsv, Imgproc.COLOR_RGB2HSV);

// keep only the pixels defined by lower and upper bound range
        Core.inRange(hsv,lowerColorBound,upperColorBound,mask);

        List<MatOfPoint> contours_red = new ArrayList<>();
        Mat tempR = new Mat();
        Imgproc.findContours(mask, contours_red, tempR, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);

        for(Mat cnt: contours_red)
        {
            double cont_area = Imgproc.contourArea(cnt);
            if(cont_area>1000)
            {
                Rect box = boundingRect(cnt);
                Imgproc.rectangle(rgb,box, new Scalar(255,0,0),4);
                int x=box.x;
                int y=box.y-5;
                Point p=new Point(x,y);
                Imgproc.putText(rgb,"RED",p,1,3,new Scalar(255,0,0),4);

            }
        }


        return rgb;
    }
}

