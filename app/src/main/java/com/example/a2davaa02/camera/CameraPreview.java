package com.example.a2davaa02.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by 2davaa02 on 19/04/2018.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    Camera camera;

    public CameraPreview(Context ctx)
    {
        super(ctx);
        try {
            camera = Camera.open();
            this.getHolder().addCallback(this);
        } catch(Exception e) {
            new AlertDialog.Builder(ctx).setMessage(e.toString()).
                    setPositiveButton("OK",null).show();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d("cameraApp",e.toString());
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(camera!=null)
        {
            boolean  isPortrait =  getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            Camera.Parameters p= camera.getParameters();
            Camera.Size s = this.getClosestSize(p,width,height);
            if (s!=null) {
                camera.stopPreview();
                p.setPreviewSize(s.width, s.height);
                camera.setParameters(p);
                camera.startPreview();


                try {
                    camera.setPreviewDisplay(this.getHolder());
                } catch (IOException e) {
                    Log.e("cameraApp", "Error setting preview display: " + e);
                }

                // Have to rotate if portrait
                if(isPortrait) {
                    camera.setDisplayOrientation(90);
                }
            }
        }


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            camera.stopPreview();
            camera.release();
            camera = null;
        } catch (Exception e) {
            Log.d("cameraApp",e.toString());
        }
    }
    private Camera.Size getClosestSize(Camera.Parameters p,int w, int h) {

        boolean isPortrait =  getResources().getConfiguration().orientation == 	Configuration.ORIENTATION_PORTRAIT;

        Camera.Size s = null;

        // Get a list of all the supported preview sizes
        List<Camera.Size> sizes = p.getSupportedPreviewSizes();

        int mindw=Integer.MAX_VALUE, dw;
        double curRatio, aspectRatio = (double)w/(double)h, dratio, 	minDiffRatio = Double.MAX_VALUE;

        // Loop through the supported sizes to select the best one
        for(int i=0; i<sizes.size(); i++) {

            // Get the difference between the width of the screen and the width 	// of this supported size
            dw = Math.abs(sizes.get(i).width-w);

            // Match in landscape mode - supported resolutions expressed in 	// landscape
            int realWidth = isPortrait ? sizes.get(i).height: sizes.get(i).width;//if isPortrait x: else y | int w=isPortrait?x:y;
            int realHeight = isPortrait ? sizes.get(i).width: sizes.get(i).height;

            // Get the aspect ratio of the current supported size
            curRatio = ((double)realWidth) / ((double)realHeight);

            // What is the difference between the aspect ratio of the screen 	// and the aspect ratio of the current supported size?
            dratio = Math.abs(curRatio-aspectRatio);

            // only consider this size if aspect ratio of this dimension is 	// closest match so far
            // 0.0001 for possible rounding errors in double numbers

            if(dratio-0.0001 <= minDiffRatio ) {
                minDiffRatio  = dratio;

                // as we're selecting our size on aspect ratio we only need to 	    // consider one dimension (width in this example)
                // if the difference in width between the current supported 	    // size and the screen width is the lowest so far,
                //  then select this size
                if(dw < mindw) {
                    mindw = dw;

                    s = sizes.get(i);
                }
            }
        }

        return s;
    }


}
