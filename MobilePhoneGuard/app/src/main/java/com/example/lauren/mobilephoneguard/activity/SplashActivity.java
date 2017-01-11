package com.example.lauren.mobilephoneguard.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.example.lauren.mobilephoneguard.model.VersionInfo;
import com.example.lauren.mobilephoneguard.R;
import com.example.lauren.mobilephoneguard.utils.StreamUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class SplashActivity extends Activity {

    private TextView tv_version_name;
    private VersionInfo myVersionInfo;
    private int mAppVersionCode;
    //static final
    private static final int UPDATE_VERSION = 100;
    private static final int JSON_ERROR = 101;
    private static final int IO_ERROR = 102;
    private static final int URL_ERROR = 103;
    private static final int DONT_UPDATE_VERSION = 104;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //初始化UI
        initUI();

        //初始化数据
        initData();
    }

    private void initUI(){

        tv_version_name = (TextView) findViewById(R.id.tv_version_name);


    }


    private void initData(){
        mAppVersionCode = getVersionCode();
        tv_version_name.setText("版本名称: "+mAppVersionCode);

        //获取服务器保存的版本信息
        getNewVersionInfoByRequest();
    }


    private Handler handle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case UPDATE_VERSION:
                    showUpdateView();
                    break;
                case IO_ERROR:

                    break;
                case URL_ERROR:

                    break;
                case DONT_UPDATE_VERSION:

                    break;
                default:
                    break;
            }


            return false;
        }
    });


    private void showUpdateView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("版本更新");
        builder.setMessage(myVersionInfo.versionDes);

        //立即更新
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //下载apk,apk连接地址,downLoadURL
                downLoadNewAPK();
            }
        });

        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //取消对话框,进入首页
                enterHome();
            }
        });
        builder.show();
    }



    private void downLoadNewAPK(){

//        XUtils.

    }

    private void enterHome() {
        //跳转到首页
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }


    private void getNewVersionInfoByRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                long beginRequestTime = System.currentTimeMillis();
                try {
                    URL url = new URL("http://www.lilongcnc.cc/lauren_service/getNewVersion.json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(2000);
                    connection.setReadTimeout(2000);
                    Log.e("getNewVersionInfoByRequest","-- 开始请求网络 --");

                    int code = connection.getResponseCode();
                       Log.e("getNewVersionInfoByRequest",""+code);

                    if ( code == 200){
                        InputStream inputStream = connection.getInputStream();
                        //将流转换成字符串
                        String json = StreamUtil.streamToString(inputStream);

                        JSONObject jsonObject = new JSONObject(json);

                        VersionInfo versionInfo = new VersionInfo();
                        versionInfo.versionName = jsonObject.optString("versionName");
                        versionInfo.versionDes = jsonObject.optString("versionDes");
                        versionInfo.versionCode = jsonObject.optString("versionCode");
                        versionInfo.downloadURL = jsonObject.optString("downloadURL");
                        myVersionInfo = versionInfo;
                        Log.e("getNewVersionInfoByRequest",myVersionInfo.versionCode);


                        //判断是否需要更新
                        if (mAppVersionCode < Integer.parseInt(myVersionInfo.versionCode))
                            message.what = UPDATE_VERSION;
                        else
                            message.what = DONT_UPDATE_VERSION;

                        message.obj = versionInfo;
                    }

                } catch (MalformedURLException e) {
                    message.what = URL_ERROR;
                    e.printStackTrace();

                }catch (IOException e) {

                    message.what = IO_ERROR;
                    e.printStackTrace();

                } catch (JSONException e) {

                    message.what = JSON_ERROR;
                    e.printStackTrace();



                }finally {
                    long endRequestTime = System.currentTimeMillis();
                    if (endRequestTime-beginRequestTime < 4000)
                        try {
                            Thread.sleep(4000-(endRequestTime-beginRequestTime));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    handle.sendMessage(message);
                }
            }
        }).start();
    }



    private int getVersionCode(){
        PackageManager packageManager = getPackageManager();
        try {
            //获取指定的基本信息(版本名称,版本号)
            PackageInfo pkInfo = packageManager.getPackageInfo(getPackageName(),0); //0代表获取基本信息
            return pkInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
