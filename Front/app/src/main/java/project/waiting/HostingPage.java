package project.waiting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HostingPage extends AppCompatActivity {

    TextView name;
    TextView waitingnum;
    TextView number;
    TextView alarmnum;
    TextView status;
    TextView setnum;
    TextView err;
    String hostname;
    int nowWaiting;//현재 웨이팅 현황
    int bit=0;


    static RequestQueue requestQueue;
    final static String gethost = "http://10.0.2.2:8002/host/gethost";//등록한 호스트 정보 요청
    final static String state = "http://10.0.2.2:8002/host/state";//등록한 호스트 정보 요청
    final static String passreq = "http://10.0.2.2:8002/host/pass";//대기자 번호 변경 요청
    final static String alarm = "http://10.0.2.2:8002/host/alarm";//알람 수변경 요청
    final static String resetq = "http://10.0.2.2:8002/host/reset";//정보 리셋

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosting_page);

        name = findViewById(R.id.name);
        err = findViewById(R.id.err);
        waitingnum = findViewById(R.id.waitingnum);
        number = findViewById(R.id.number);
        alarmnum = findViewById(R.id.alarmnum);
        status = findViewById(R.id.status);
        setnum = findViewById(R.id.setnum);

        Button start = findViewById(R.id.start);
        Button stop = findViewById(R.id.stop);
        Button set = findViewById(R.id.set);
        Button pass = findViewById(R.id.pass);
        Button retry = findViewById(R.id.retry);
        Button reset = findViewById(R.id.reset);

        Intent intent = getIntent();
        hostname = intent.getStringExtra("hostname");
        name.setText(hostname);

        retry.setOnClickListener(new View.OnClickListener() {//새로고침 역할 (대기자수) 갱신된것 보여준다
            @Override
            public void onClick(View v) {
                getDatas();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {//start 누르면 클라이언트들 웨이팅 받을수 있게 한다.
            @Override
            public void onClick(View v) {
                bit = 0;
                setState();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bit =1;
                setState();
            }
        });
        pass.setOnClickListener(new View.OnClickListener() {//클라이언트 완성하고 해야함
            @Override
            public void onClick(View v) {
            //    if(nowWaiting ==0) {//대기자 없을경우

             //   }else{//대기자 1명이라도 있을경우
                    passing();
            //    }
            }
        });
        set.setOnClickListener(new View.OnClickListener() {//알림 보낼수 셋팅
            @Override
            public void onClick(View v) {
                sendalarm();
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {//데이터 초기화함 - 상태 stop, 대기자 없을때만 가능
            @Override
            public void onClick(View v) {
                resetreq();
            }
        });
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        getDatas();//디비에서 데이터 가져옴

    }
    /////////////////////////////////////////////////
    public void resetreq(){//데이터 초기화함 - 상태 stop, 대기자 없을때만 가능
        StringRequest request= new StringRequest(Request.Method.POST, resetq, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("err")){
                    err.setText("에러");
                }
                else if(response.equals("fail")){//실패 했을경우 알려줘야함
                    err.setText("대기자가 남아 있거나 상태가 start인경우 초기화 할수 없습니다");
                }
                else if(response.equals("ok")){//성공한경우
                    getDatas();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("hostname",hostname);
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    public void sendalarm(){//알람받을 수 요청 보냄
        String temp = setnum.getText().toString();
        if(temp.equals("")){//
            temp ="5";
        }
        final String Salarm = temp;//아무것도 입력안한경우 디폴트값 5 넣어줌 입력값 있는경우 입력값 넣어줌

        setnum.setText("");
        StringRequest request = new StringRequest(Request.Method.POST, alarm, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                alarmnum.setText("보낼 알람 수 : "+ response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("hostname",hostname);
                params.put("alarm",Salarm);
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    public void passing(){//번호표 넘겼을경우 - 디비 갱신해주고 데이터 받아온다
        StringRequest request = new StringRequest(Request.Method.POST, passreq, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.equals("NO waiter")){//대기자 없을경우
                    err.setText("대기자 없음");
                }
                else if(response.equals("err")){
                    err.setText("err");
                }
                else{//정상 작동시 웨이팅 넘버 갱신해줌
                    number.setText(response);//
                    getDatas();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {//호스트 네임 보내줘야함
                Map<String,String> params = new HashMap<String,String>();
                params.put("hostname",hostname);
                return params;
            }
        };
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    public void setState(){//상태 변경 요청
        StringRequest request = new StringRequest(Request.Method.POST, state, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                status.setText("상태: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("hostname",hostname);
                if(bit == 0){
                    params.put("state","start");
                }else if(bit == 1){
                    params.put("state","stop");
                }
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    public void getDatas(){
        StringRequest request = new StringRequest(Request.Method.POST, gethost, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String statuse = jsonObject.getString("statuse");
                    String waitnumber = jsonObject.getString("waitnumber");
                    String currentnumber = jsonObject.getString("currentnumber");
                    String alarmnumber = jsonObject.getString("alarmnumber");
                    nowWaiting = Integer.parseInt(waitnumber) - Integer.parseInt(currentnumber);

                    status.setText("");
                    status.setText("상태: "+statuse);
                    waitingnum.setText("");
                    waitingnum.setText("대기자 수: " + (Integer.parseInt(waitnumber) - Integer.parseInt(currentnumber)));
                    alarmnum.setText("");
                    alarmnum.setText("보낼 알람 수 : " + alarmnumber);
                    number.setText("");
                    number.setText(currentnumber);

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("hostname",hostname);
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }


}
