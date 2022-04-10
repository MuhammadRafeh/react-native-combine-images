package com.reactnativecombineimages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.module.annotations.ReactModule;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@ReactModule(name = CombineImagesModule.NAME)
public class CombineImagesModule extends ReactContextBaseJavaModule {
    public static final String NAME = "CombineImages";
    ReactApplicationContext contexts;

    public CombineImagesModule(ReactApplicationContext reactContext) {
      super(reactContext);
      contexts = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void combineImages(ReadableArray imagesPath, String direction, Double imagesWidth, Double imagesHeight, Boolean saveToGallery,  Promise promise) {
        try {
            ArrayList<Bitmap> imagesBitmaps = new ArrayList<Bitmap>();

            int height = 0;
            int width = 0;

            for (int index = 0; index<imagesPath.size(); index++){
                File imageFile = new File(imagesPath.getString(index));
                Bitmap originalBitmap = BitmapFactory.decodeFile(String.valueOf(imageFile));
//                if (imagesWidth != -1){
//                  originalBitmap = resize(originalBitmap,  imagesWidth, imagesHeight);
//                }
                if (direction == "v"){
                  height += originalBitmap.getHeight();
                  if (originalBitmap.getWidth() > width){
                    width = originalBitmap.getWidth();
                  }
                } else { // h
                  width += originalBitmap.getWidth();
                  if (originalBitmap.getHeight() > height){
                    height = originalBitmap.getHeight();
                  }
                }
                imagesBitmaps.add(originalBitmap);
            }

            Bitmap resultBitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(resultBitmap);
            int doneArea = 0;

            for (int bit = 0; bit<imagesBitmaps.size(); bit++){
                Bitmap arrBitmap = imagesBitmaps.get(bit);
                if (direction == "v"){
                  canvas.drawBitmap(arrBitmap, 0f, doneArea, null);
                  doneArea+=arrBitmap.getHeight();
                } else {
                  canvas.drawBitmap(arrBitmap, doneArea, 0f, null);
                  doneArea+=arrBitmap.getWidth();
                }
            }

            File file = new File(contexts.getCacheDir(), getFileName("jpeg"));
            if(file.exists()) {
                file.delete();
            }

            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();

            if (saveToGallery){
              MediaScannerConnection.scanFile(contexts, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {}
              });
            }

            promise.resolve("file://"+file.getAbsolutePath());
        } catch(Exception e) {
            promise.reject("failed", e);
        }
    }
    public String getFileName(String ext) {
      String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
      return "/react-native-combine-images" + timeStamp + "_." + ext;
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
      if (maxHeight > 0 && maxWidth > 0) {
        int width = image.getWidth();
        int height = image.getHeight();
        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
          finalWidth = (int) ((float)maxHeight * ratioBitmap);
        } else {
          finalHeight = (int) ((float)maxWidth / ratioBitmap);
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
        return image;
      } else {
        return image;
      }
    }
}
