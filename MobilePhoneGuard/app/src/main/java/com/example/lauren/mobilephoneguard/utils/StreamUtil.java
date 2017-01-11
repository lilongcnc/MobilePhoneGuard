package com.example.lauren.mobilephoneguard.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Lauren on 17/1/11.
 */

public class StreamUtil {

    public static String streamToString(InputStream inputStream) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] byteBuffer = new byte[1024];
        int temp = -1;

        try {
            while ((temp = inputStream.read(byteBuffer)) != -1) {
                bos.write(byteBuffer, 0, temp);
            }

            return bos.toString();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                bos.close();
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return null;
    }
}