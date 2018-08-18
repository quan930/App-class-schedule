package com.example.daquan.classschedule;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.AutoSizeableTextView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String cookie;
    private ImageView imageView;
    private List<String> classIds;
    private List<String> classNames;
    private AutoCompleteTextView autoCompleteTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        autoCompleteTextView = findViewById(R.id.auto);
        init();
    }
    private void init () {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://59.79.112.9/ZNPK/KBFB_ClassSel.aspx");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    String headerField = connection.getHeaderField("set-cookie");
                    //        Log.d("asd", headerField.toString());
                    cookie = headerField.split(";")[0];
                    Log.d("无敌", cookie);
                    getImg();
                    getList();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //获得验证码
    private void getImg() {//验证码
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://59.79.112.9/sys/ValidateCode.aspx");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.addRequestProperty("Referer", "http://59.79.112.9/sys/ValidateCode.aspx");//http://59.79.112.9/ZNPK/TeacherKBFB.aspx
                    connection.addRequestProperty("Cookie",cookie);

                    InputStream is = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(is);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //获取班级列表，id
    private void getList(){
        ArrayAdapter<String> arrayAdapter;
        final ArrayAdapter<String> arr_adapter;
        classIds = new ArrayList<>();
        classNames = new ArrayList<>();
        URL u2 = null;
        try {
            u2 = new URL("http://59.79.112.9/ZNPK/KBFB_ClassSel.aspx");
            HttpURLConnection con2 = (HttpURLConnection) u2.openConnection();
            con2.setRequestProperty("Cookie", cookie);
//		con2.setRequestProperty("Referer", "http://59.79.112.9/ZNPK/TeacherKBFB.aspx");

            con2.connect();

            //获取全部内容
            BufferedReader br2 = new BufferedReader(new InputStreamReader(con2.getInputStream(), "GB2312"));
            String s2 = null;
            StringBuffer sb2 = new StringBuffer();
            while ((s2 = br2.readLine()) != null) {
                sb2.append(s2);
            }
            String s;
            s = sb2.toString();
//		System.out.println(s);

            int p1 = 0;
            int p2 = 0;
            p1=s.indexOf("<option value=", 0);
            //<option value=2015011601>15保险G1</option>
            //option value='20171'>2017-2018学年第二学期</option>//错误信息
            //遍历循环处理所有的id和name
            while(p1!=-1) {
                //假定id的长度为固定10位字符
                if(s.substring(p1+14,p1+15).equals("'")) {
                    System.out.println("错误");
                    p2 = s.indexOf("</option>", p1);
                    p1 = s.indexOf("<option value=", p2);
                    continue;
                }
                String id = s.substring(p1+14,p1+24);
                p2 = s.indexOf("</option>", p1);
                String name = s.substring(p1+25,p2);
                p1 = s.indexOf("<option value=", p2);
                System.out.println(id);
                System.out.println(name);

                classIds.add(id);
                classNames.add(name);
            }

            Log.d("OK了", "getList: ");
            //适配器
            arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, classNames);
            //设置样式
            arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //加载适配器
                    autoCompleteTextView.setAdapter(arr_adapter);
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
