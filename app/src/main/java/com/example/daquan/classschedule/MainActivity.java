package com.example.daquan.classschedule;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.widget.AutoSizeableTextView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String cookie;
    private ImageView imageView;//验证码
    private List<String> classIds;
    private List<String> classNames;
    private int classNamesListIndexes = 0;
    private Spinner spinner;
    private ImageView studentClassImageView;
    private Button button;
    private EditText editText;
    private ProgressBar progressBar;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();//去掉标题栏
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        spinner = findViewById(R.id.auto);
        studentClassImageView = findViewById(R.id.studentClass);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        Rect outRect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        //打印标题栏的高度
        Log.d("height = " , String.valueOf(outRect.top));
        //打印标题栏到底部的高度
        Log.d("width = " + outRect.width()," height = " + outRect.height());


        init();
        imageView.setOnClickListener(new View.OnClickListener() {//验证码监听
            @Override
            public void onClick(View v) {
                getImg();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {//查询监听
            @Override
            public void onClick(View v) {
                studentClassImageView.setVisibility(View.GONE);//隐藏控件
                progressBar.setVisibility(View.VISIBLE);
                progressBar.bringToFront();
                textView.setVisibility(View.VISIBLE);//显示控件
                textView.bringToFront();//控件最上层
                getStudentClass();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("位置", String.valueOf(position));
                classNamesListIndexes = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                    getList();
                    getImg();
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
                    connection.addRequestProperty("Referer", "http://59.79.112.9/ZNPK/KBFB_ClassSel.aspx");
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
		    con2.setRequestProperty("Referer", "http://59.79.112.9/ZNPK/TeacherKBFB.aspx");
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

            int p1 = 0;
            int p2 = 0;
            p1=s.indexOf("<option value=", 0);
            //<option value=2015011601>15保险G1</option>
            //option value='20171'>2017-2018学年第二学期</option>//错误信息
            //遍历循环处理所有的id和name
            while(p1!=-1) {
                //假定id的长度为固定10位字符
                if(s.substring(p1+14,p1+15).equals("'")) {
                    p2 = s.indexOf("</option>", p1);
                    p1 = s.indexOf("<option value=", p2);
                    continue;
                }
                String id = s.substring(p1+14,p1+24);
                p2 = s.indexOf("</option>", p1);
                String name = s.substring(p1+25,p2);
                p1 = s.indexOf("<option value=", p2);
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
                    spinner.setAdapter(arr_adapter);
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
    //学生课表
    private void getStudentClass(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://59.79.112.9/ZNPK/KBFB_ClassSel_rpt.aspx");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Referer", "http://59.79.112.9/ZNPK/KBFB_ClassSel.aspx");
                    conn.setRequestProperty("Cookie",cookie);

                    String id = classIds.get(classNamesListIndexes);
                    String authCode = editText.getText().toString();
                    String postString = "Sel_XNXQ="+getSchoolYearTerm()+"&Sel_XZBJ="+id+"&type=1&txt_yzm=" + authCode;
                    Log.d("报文", postString);
                    conn.getOutputStream().write(postString.getBytes());
                    InputStream is = conn.getInputStream();
                    BufferedReader bf = new BufferedReader(new InputStreamReader(is, "GB2312"));
                    String line = null;
                    StringBuilder stringBuilder = new StringBuilder();
                    //响应数据
                    while ((line = bf.readLine()) != null) {
                        stringBuilder.append(line);
//                        Log.d("报文", line);
                    }

                    String responseMessage = stringBuilder.toString();
                    if(responseMessage.contains("验证码错误")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"验证码错误",Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                textView.setVisibility(View.GONE);
                            }
                        });
                    }else{
                        int imgLocation = responseMessage.indexOf("img");
                        if(imgLocation==-1){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"没有课",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    textView.setVisibility(View.GONE);
                                    studentClassImageView.setVisibility(View.VISIBLE);
                                    studentClassImageView.bringToFront();
                                }
                            });
                        }else{
                            Element img = Jsoup.parse(responseMessage).select("img").get(0);
                            String src = img.attr("src");
                            getClassImg(src);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void getClassImg(String src) {
        try {
            URL url = new URL("http://59.79.112.9/ZNPK/"+src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.addRequestProperty("Referer", "http://59.79.112.9/ZNPK/KBFB_ClassSel_rpt.aspx");
            connection.addRequestProperty("Cookie",cookie);

            InputStream is = connection.getInputStream();
            final Bitmap bitmap = BitmapFactory.decodeStream(is);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    studentClassImageView.setVisibility(View.VISIBLE);
                    studentClassImageView.setImageBitmap(bitmap);
                    progressBar.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            });
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getSchoolYearTerm(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy:MM");//设置日期格式
        String yearAndMonth = df.format(new Date());
        String as[] = yearAndMonth.split(":");
        int year=Integer.parseInt(as[0]);
        int month = Integer.parseInt(as[1]);
        int m = 0;
        if(month<9){
            year--;
            if (month>=3){
                m = 1;
            }else{
                m = 0;
            }
        }else{
            if(month>=9){
                m = 0;
            }
        }
        return String.valueOf(year)+String.valueOf(m);
//        System.out.println("学年："+"学期："+String.valueOf(year)+String.valueOf(m));
    }
}
