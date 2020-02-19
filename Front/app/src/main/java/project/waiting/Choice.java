package project.waiting;
//host or client choice Activity

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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

public class Choice extends AppCompatActivity {
    public static final int LOGIN = 101;
    public static final int CHOICE = 102;
    public static final int HOST = 103;
    public static final int CLIENT = 104;
    static SharedPreferences.Editor edit = null;

    private int Activitybit =0;

    TextView log;
    static RequestQueue requestQueue;
    final static String logout = "http://10.0.2.2:8002/auth/logout"; //로그아웃 요청 서버 db에 status 바꿔줘야함
    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        log= findViewById(R.id.log);
        Button host = findViewById(R.id.host);
        Button client = findViewById(R.id.client);
        Button logout = findViewById(R.id.logout);
        name = findViewById(R.id.name);
        Intent intent = getIntent();//인텐트 가져옴
        Bundle bundle = intent.getExtras();
       // name.setText("welcome " + bundle.getString("name") +" please chocie that you want" ); // 이방식으로 하면 아이디 한글일 경우 깨짐

        SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
        name.setText("환영합니다" + sp.getString("userid","") +"님 원하시는 기능을 선택해주세요" );

        host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),HostPage.class);
                startActivityForResult(intent,HOST);
            }
        });

        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ClientPage.class);
                startActivityForResult(intent,CLIENT);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {//로그 아웃 버튼 ->로그인 화면으로 가야한다
            @Override
            public void onClick(View v) {

                logoutRequest();

            }
        });

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    public void logoutRequest(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, logout, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(status.equals("success")){
                        // SharedPreferences 에 있던 로그인 정보 삭제
                        SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                        edit = sp.edit();
                        edit.clear();//저장된 로그인 정보 전부 삭제해준다.
                        edit.commit();
                        Activitybit = 1; //이 비트가 1이면 현재 엑티비티가 정지 상태에 들어갈시 삭제 아니면 유지
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class); //기존 회원가입 화면으로 넘어간다
                        startActivity(intent);
                    }
                    else{//로그아웃 실패시 - 뜨면 매우곤란 - 해결할 방법이 없음
                        println("logout fail");
                    }


                }catch(Exception e){
                    println("err->e");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override//파라미터 전달
            protected Map<String, String> getParams() throws AuthFailureError {
                    SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                    String userid = sp.getString("userid","");
                    Map<String,String> params =new HashMap<String,String>();
                    params.put("userid",userid);//아이디 파라미터에 넣어줌

                return params;

            }
        };
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
        println("요청 보냄");
    }
    @Override
    protected void onPause() {//정지상태로 들어가면 자동으로 엑티비티 삭제
        super.onPause();
        if(Activitybit == 1){//1 이면 삭제(로그아웃 된거임)
            finish();
        }

    }

    public void println(String data){
        log.append(data + "\n");
    }
}
