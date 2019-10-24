package com.example.multitread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private TextView tvMsg;
    private Button Thr, Task, Handler, AsyncTask, Other;
    private ImageView imageWeb;

    //线程变量
    MyTask myTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        progressBar = findViewById(R.id.progress_bar_h);
        tvMsg = findViewById(R.id.tv_Msg);
        Thr = findViewById(R.id.bt_thread);
        Task = findViewById(R.id.bt_task);
        Handler = findViewById(R.id.bt_Handler);
        AsyncTask = findViewById(R.id.bt_AsyncTask);
        Other = findViewById(R.id.bt_other);
        imageWeb = findViewById(R.id.image_web);

        Thr.setOnClickListener(this);
        Task.setOnClickListener(this);
        Handler.setOnClickListener(this);
        AsyncTask.setOnClickListener(this);
        Other.setOnClickListener(this);
        myTask=new MyTask();
    }

    private CalculateThread calculateThread;
    private static final String DOWNLOAD_URL = "https://desk-fd.zol-img.com.cn/t_s1920x1080c5/g5/M00/07/07/ChMkJlXw8QmIO6kEABYKy-RYbJ4AACddwM0pT0AFgrj303.jpg\n";
    private static final String DOWNLOAD_IMG="https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3608155120,1630233151&fm=26&gp=0.jpg\n";


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_Msg:
                break;
            case R.id.bt_thread:
                calculateThread = new CalculateThread();
                calculateThread.start();
                break;
            case R.id.bt_task:
                //手动调用异步线程,myTask只能起一次
                myTask.execute();
                break;
            case R.id.bt_Handler:
                new Thread(new DownloadImageFetcher(DOWNLOAD_URL)).start();
                break;
            case R.id.bt_AsyncTask:
                new MyAsyncTask().execute(DOWNLOAD_IMG);
                break;
            case R.id.bt_other:
                break;
            case R.id.image_web:
                break;
        }
    }

    //进度条常量，Message,what类型
    private static final int START_NUM = 1;
    private static final int ADDING_NUM = 2;
    private static final int ENDING_NUM = 3;
    private static final int CANCEL_NUM = 4;

    private MyHandler myHandler = new MyHandler(this);

    static class MyHandler extends Handler {
        private WeakReference<Activity> ref;

        public MyHandler(Activity activity) {
            this.ref = new WeakReference<>(activity);
        }
        //


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = (MainActivity) ref.get();
            if (activity == null) {
                return;
            }
            //2.
            switch (msg.what) {
                case START_NUM:
                    activity.progressBar.setVisibility(View.VISIBLE);
                    break;
                case ADDING_NUM:
                    activity.progressBar.setProgress(msg.arg1);
                    activity.tvMsg.setText("计算已完成" + msg.arg1 + "%");
                    break;
                case ENDING_NUM:
                    activity.progressBar.setVisibility(View.GONE);
                    activity.tvMsg.setText("计算已完成，结果为:" + msg.arg1);
                    activity.myHandler.removeCallbacks(activity.calculateThread);
                    break;
                case CANCEL_NUM:
                    activity.progressBar.setProgress(0);
                    activity.progressBar.setVisibility(View.GONE);
                    activity.tvMsg.setText("计算已取消");
                    break;
            }
        }
        //下载图片的线程
    }

    class CalculateThread extends Thread {
        @Override
        public void run() {
            int result = 0;
            boolean isCancel = false;

            //1.开始的发送一个空消息
            myHandler.sendEmptyMessage(START_NUM);

            //2.计算过程
            for (int i = 0; i <= 100; i++) {
                try {
                    Thread.sleep(100);
                    result += i;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isCancel = true;
                    break;
                }
                if (i % 5 == 0) {
                    //获取消息对象
                    Message msg = Message.obtain();
                    //
                    msg.what = ADDING_NUM;
                    msg.arg1 = i;
                    //发送消息
                    myHandler.sendMessage(msg);
                }

            }
            if (!isCancel) {
                Message msg = myHandler.obtainMessage();
                msg.what = ENDING_NUM;
                msg.arg1 = result;
                myHandler.sendMessage(msg);
            }
        }
    }


    private static final int MSG_SHOW_PROGRESS = 11;
    private static final int MSG_SHOW_IMAGE = 12;
    private MyUIHandler uiHandler = new MyUIHandler(this);

    static class MyUIHandler extends Handler {
        //定义弱应用对象
            private WeakReference<Activity> ref;
        //在构造方法中创建此对象
            public MyUIHandler(Activity activity) {
                this.ref = new WeakReference<>(activity);
            }
            //重写handler方法

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                //1.获取弱应用指向的Activity对象
                MainActivity activity = (MainActivity) ref.get();
                if (activity == null) {
                    return;
                }
                //2.根据Message的what属性值处理消息
                switch (msg.what){
                    case MSG_SHOW_PROGRESS:
                        activity.progressBar.setVisibility(View.VISIBLE);
                        break;
                    case MSG_SHOW_IMAGE:
                        activity.progressBar.setVisibility(View.GONE);
                        activity.imageWeb.setImageBitmap((Bitmap) msg.obj);
                        break;
                }
            }
        }
        //下载图片的线程
    private class DownloadImageFetcher implements Runnable{
        private String imgUrl;

        public DownloadImageFetcher(String strUrl){
            this.imgUrl=strUrl;
        }

        @Override
        public void run() {
            InputStream in=null;
            //发一个空消息到handleMessage()去处理，显示进度条
            uiHandler.obtainMessage(MSG_SHOW_PROGRESS).sendToTarget();
            try {
                //1.将url字符串转为URL对象
                URL url=new URL(imgUrl);
                //2.打开url对象的http连接
                HttpURLConnection connection=(HttpURLConnection) url.openConnection();
                //获取这个连接的输入流
                in=connection.getInputStream();
                //4.将输入流解码为Bitmap图片
                Bitmap bitmap= BitmapFactory.decodeStream(in);
                //5.通过handler发送消息
                Message msg=uiHandler.obtainMessage();
                msg.what=MSG_SHOW_IMAGE;
                msg.obj=bitmap;
                uiHandler.sendMessage(msg);

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(in!=null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private class MyTask extends android.os.AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                int count = 0;
                int length = 1;
                while (count < 99) {
                    count += length;
                    publishProgress(count);
                    Thread.sleep(50);
                }
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }

            return null;
        }

        //在主线程 显示线程任务执行的进度
        @Override
        protected void onProgressUpdate(Integer... progresse) {
            progressBar.setProgress(progresse[0]);
            tvMsg.setText("loadign..."+progresse[0]+"%");

        }
        //方法4onPostExecute()
        //作用:接收线程任务执行结果，将执行结果显示到ui组件中

        @Override
        protected void onPostExecute(String s) {
           tvMsg.setText("加载完毕");
        }
        //作用:将异步任务设置为:取消状态

        @Override
        protected void onCancelled() {
           tvMsg.setText("已取消");
           progressBar.setProgress(0);
        }
    }
    private class MyAsyncTask extends android.os.AsyncTask<String,Void,Bitmap> {


        //方法4onPostExecute()
        //作用:接收线程任务执行结果，将执行结果显示到ui组件中

        @Override
        protected void onPostExecute(Bitmap result) {
            Log.d("","onPostExecute");
            updateImageView(result);
        }
        //作用:将异步任务设置为:取消状态



        @Override
        protected void onPreExecute() {
           Log.d("","onPreExecute");
        }

        @Override
        protected Bitmap doInBackground(String... args) {
            String website=args[0];
            HttpURLConnection conn=null;
            InputStream ins=null;
            try {
                URL url=new URL(website);
                conn= (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                if(conn.getResponseCode()==200){
                    Log.d("","get image is ok");
                    ins=conn.getInputStream();
                    return  BitmapFactory.decodeStream(ins);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (ins!=null){
                    try {
                        ins.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        public void updateImageView(Bitmap result){
            if(result!=null){
                ImageView img=findViewById(R.id.image_web);
                img.setImageBitmap(result);
            }
        }
    }
}
