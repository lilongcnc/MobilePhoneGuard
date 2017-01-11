package com.example.lauren.mobilephoneguard.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
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

    /**
     * 可取消的任务
     */
    private Callback.Cancelable cancelable;
    /**
     * 进度条对话框
     */
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        x.view().inject(this);

        //初始化UI
        initUI();

        //初始化数据
        initData();


    }

    private void initUI() {

        tv_version_name = (TextView) findViewById(R.id.tv_version_name);

        initProgressDialog();
    }


    private void initData() {
        mAppVersionCode = getVersionCode();
        tv_version_name.setText("版本名称: " + mAppVersionCode);

        //获取服务器保存的版本信息
        getNewVersionInfoByRequest();
    }


    private Handler handle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
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

    /*初始化对话框*/
    private void initProgressDialog() {
        //创建进度条对话框
        progressDialog = new ProgressDialog(this);
        //设置标题
        progressDialog.setTitle("下载文件");
        //设置信息
        progressDialog.setMessage("玩命下载中...");
        //设置显示的格式
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //设置按钮
        progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "暂停", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //点击取消正在下载的操作
                cancelable.cancel();
            }
        });
    }

    //下载软件
    private void downLoadNewAPK() {

        String url_str = "http://lilongcnc.cc/lauren_service/mobilephoneguard3.apk";
        //设置请求参数
        RequestParams params = new RequestParams(url_str);
        params.setAutoResume(true);//设置是否在下载是自动断点续传
        params.setAutoRename(false);//设置是否根据头信息自动命名文件
        params.setSaveFilePath("/sdcard/xutils/mobilephoneguard3.apk");
        params.setExecutor(new PriorityExecutor(2, true));//自定义线程池,有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.
        params.setCancelFast(true);//是否可以被立即停止.
        //下面的回调都是在主线程中运行的,这里设置的带进度的回调
        cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onCancelled(CancelledException arg0) {
                Log.i("tag", "取消" + Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                Log.i("tag", "onError: 失败" + Thread.currentThread().getName());
                progressDialog.dismiss();
            }

            @Override
            public void onFinished() {
                Log.i("tag", "完成,每次取消下载也会执行该方法" + Thread.currentThread().getName());
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(File arg0) {
                Log.i("tag", "下载成功的时候执行" + Thread.currentThread().getName());
                //下载完成之后,安装应用程序
                installNewVersionApp(arg0);

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (isDownloading) {
                    progressDialog.setProgress((int) (current * 100 / total));
                    Log.i("tag", "下载中,会不断的进行回调:" + Thread.currentThread().getName());
                }
            }

            @Override
            public void onStarted() {
                Log.i("tag", "开始下载的时候执行" + Thread.currentThread().getName());
                progressDialog.show();
            }

            @Override
            public void onWaiting() {
                Log.i("tag", "等待,在onStarted方法之前执行" + Thread.currentThread().getName());
            }

        });

    }

    //安装新版本 APP
    private void installNewVersionApp(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
//        intent.setData(Uri.fromFile(file));// 文件作为数据源
//        intent.setType("application/vnd.android.package-archive"); //设置安装的类型

        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        startActivityForResult(intent,0);
    }

    //安装完成
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enterHome();
        super.onActivityResult(requestCode, resultCode, data);
    }

    //跳转到首页
    private void enterHome() {

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    //网络下载
    private void getNewVersionInfoByRequest() {
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
                    Log.e("getNewVersionInfoByRequest", "-- 开始请求网络 --");

                    int code = connection.getResponseCode();
                    Log.e("getNewVersionInfoByRequest", "" + code);

                    if (code == 200) {
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
                        Log.e("getNewVersionInfoByRequest", myVersionInfo.versionCode);


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

                } catch (IOException e) {

                    message.what = IO_ERROR;
                    e.printStackTrace();

                } catch (JSONException e) {

                    message.what = JSON_ERROR;
                    e.printStackTrace();


                } finally {
                    long endRequestTime = System.currentTimeMillis();
                    if (endRequestTime - beginRequestTime < 4000)
                        try {
                            Thread.sleep(4000 - (endRequestTime - beginRequestTime));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    handle.sendMessage(message);
                }
            }
        }).start();
    }


    private int getVersionCode() {
        PackageManager packageManager = getPackageManager();
        try {
            //获取指定的基本信息(版本名称,版本号)
            PackageInfo pkInfo = packageManager.getPackageInfo(getPackageName(), 0); //0代表获取基本信息
            return pkInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
