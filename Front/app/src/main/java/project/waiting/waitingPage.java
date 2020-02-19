package project.waiting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class waitingPage extends AppCompatActivity {

    TextView title;
    TextView waitnum;
    TextView mynum;
    String hostname;
    String latitude;
    String lotitude;
    TextView state;
    TextView log;

    int nowWaiting;
    String savestate;

    static SharedPreferences.Editor edit = null;
    static RequestQueue requestQueue;
    final static String getdata = "http://10.0.2.2:8002/client/getdata";//검색한 호스트 정보들 요청
    final static String waiting = "http://10.0.2.2:8002/client/waiting";//호스트에 웨이팅 요청
    final static String setwait = "http://10.0.2.2:8002/client/mywait";//웨이팅 정보 요청

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_page);

        title = findViewById(R.id.title);
        waitnum = findViewById(R.id.waitnum);
        mynum = findViewById(R.id.mynum);
        state = findViewById(R.id.state);
        log = findViewById(R.id.log);

        Button onoff = findViewById(R.id.onoff);
        Button retry = findViewById(R.id.retry);

        Intent intent = getIntent();
        hostname = intent.getStringExtra("hostname");
        latitude = intent.getStringExtra("latitude");
        lotitude = intent.getStringExtra("lotitude");
        title.setText(hostname);

        onoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waitreq();
            }
        });
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData();
            }
        });
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        setData();//정보들 셋팅
       // setWait();//웨이팅 정보 셋팅
    }
    public void setWait(){
        StringRequest request = new StringRequest(Request.Method.POST, setwait, new Response.Listener<String>() {//나의 웨이팅 정보 요청
            @Override
            public void onResponse(String response) {
                if(response.equals("NO")){//내가 웨이팅 중인지 체크
                    mynum.setText("X");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(response);//데이터 받아와서 나의 대기번호와 남은 인수 보여준다
                        String waitnumber = jsonObject.getString("waitnumber");
                        String currentnumber = jsonObject.getString("currentnumber");
                        mynum.setText(waitnumber);
                        waitnum.setText("");
                        waitnum.setText("남은 대기자 : " + (Integer.parseInt(waitnumber)-Integer.parseInt(currentnumber)));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {//유저 아이디와 호스트이름 전송해줌
                Map<String,String> params = new HashMap<String,String>();
                SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                edit = sp.edit();
                params.put("userid",sp.getString("userid",""));
                params.put("hostname",hostname);
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    public void waitreq(){
        StringRequest request = new StringRequest(Request.Method.POST, waiting, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("NO")){//서버 저장중 오류가 생긴경우
                    log.setText("");
                    log.setText("server error please retry");
                }else if(response.equals("already")){//이미 웨이팅 중일경우
                    log.setText("");
                    log.setText("이미 웨이팅 신청하였습니다");
                }else {//정상적 웨이팅 신청
                    mynum.setText(response);//받은 번호 설정해준다
                    setData();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {//유저 아이디와 호스트이름 전송해줌
                Map<String,String> params = new HashMap<String,String>();
                SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                edit = sp.edit();
                params.put("userid",sp.getString("userid",""));
                params.put("hostname",hostname);
                params.put("latitude",latitude);
                params.put("lotitude",lotitude);

                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    public void setData(){

        StringRequest request = new StringRequest(Request.Method.POST, getdata, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
           //  waitnum.setText(response);
                try{

                    JSONObject jsonObject = new JSONObject(response);
                    String statuse = jsonObject.getString("statuse");
                    String waitnumber = jsonObject.getString("waitnumber");
                    String currentnumber = jsonObject.getString("currentnumber");
                    savestate = statuse;//현재 사태 저장해둔다
                    nowWaiting = Integer.parseInt(waitnumber) - Integer.parseInt(currentnumber);

                    if(statuse.equals("stop")){//웨이팅 안받는 경우
                        state.setText("웨이팅 불가능");
                        waitnum.setText("대기자 수 : "+ nowWaiting);
                    }else {//웨이팅 받는 경우
                        state.setText("웨이팅 가능");
                        waitnum.setText("대기자 수 : "+ nowWaiting);
                    }
                    setWait();//웨이팅 정보 셋팅

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
