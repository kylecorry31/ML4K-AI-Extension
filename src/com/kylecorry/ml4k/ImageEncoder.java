package com.kylecorry.ml4k;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class ImageEncoder {

    /**
     * Encode an image in base64 format
     * @param image The image
     * @return the encode image (base64)
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException if there is an IO error
     */
    public static String encode(File image) throws FileNotFoundException, IOException {
        byte[] byteArray = readAllBytes(new FileInputStream(image));
        return Base64Encoder.encode(byteArray);
    }
    
    /**
     * Reads all bytes from a file.
     * @param is The input stream.
     * @return The file contents as bytes.
     * @throws IOException upon error reading the input file.
     */
    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int read;
        while ((read = is.read(buff)) != -1) {
            byteArrayOutputStream.write(buff, 0, read);
        }

        byte[] out = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        is.close();

        return out;
    }

}