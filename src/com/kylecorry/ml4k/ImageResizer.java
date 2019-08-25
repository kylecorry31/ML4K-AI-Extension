package com.kylecorry.ml4k;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.appinventor.components.runtime.util.FileUtil;

class ImageResizer {

    private ImageResizer(){}
    
    public static File resize(File image, int width, int height) throws ML4KException {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);  
            byte[] bytes = out.toByteArray();
            out.close();
            File file = File.createTempFile("AI_Media_", null);
            file.deleteOnExit();
            FileUtil.writeFile(bytes, file.getAbsolutePath());
            return file;
        } catch (IOException e){
            throw new ML4KException("Unable to resize image");
        }
    }

}