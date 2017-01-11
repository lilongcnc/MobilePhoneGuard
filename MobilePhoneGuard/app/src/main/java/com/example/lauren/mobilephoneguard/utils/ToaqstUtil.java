package com.example.lauren.mobilephoneguard.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Lauren on 17/1/11.
 */

public class ToaqstUtil {

    public static void show(Context ctx,String msg){
        Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show();
    }
}
