package project.waiting;
//로그인 하는 Activity
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Login extends AppCompatActivity {
    public static final int LOGIN = 101;
    public static final int CHOICE = 102;
    public static final int HOST = 103;
    public static final int CLIENT = 104;
    static SharedPreferences.Editor edit = null;

    EditText userid;
    EditText password;
    TextView log;
    static RequestQueue requestQueue;
    final static String login = "http://10.0.2.2:8002/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userid = findViewById(R.id.userid);
        password = findViewById(R.id.password);
        log = findViewById(R.id.log);

        Button button = findViewById(R.id.login);
        Button join = findViewById(R.id.join);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();//로그인 요청
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    public void makeRequest() {//로그인 요청
        SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
        edit = sp.edit();
        final String send_userid = userid.getText().toString();
        final String send_password = password.getText().toString();
        final String token = sp.getString("token","");

        StringRequest request = new StringRequest(Request.Method.POST, login,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {//
                            //println("json" + response);
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String name = jsonObject.getString("name");
                            //println("응답 -> " + status +  name);

                            if(status.equals("success")){//로그인 성공시 -
                                SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                                edit = sp.edit();
                                edit.putString("status","login");//로그인 되어 있다는 정보 저장
                                edit.putString("userid",send_userid);
                                edit.putString("name",name);
                                edit.commit();
                                String test = sp.getString("status","");
                               // println("이거 보여줘 :" + test.equals("login"));
                                Intent intent = new Intent(getApplicationContext(),Choice.class);
                                intent.putExtra("name",name);//닉네임 넣어서 전달해줌
                                intent.putExtra("userid",send_userid);//아이디도 넣어서 전달해줌
                          //      startActivityForResult(intent, CHOICE);
                                startActivity(intent);//Choic 화면 실행후 이 엑티비티는 종료
                            }else if(status.equals("already")){
                                println("login fail(이미 다른곳에서 로그인 중입니다)");
                            }
                            else{//로그인 실패시
                                println(" login fail (id or password is wrong)");
                            }
                        }
                        catch(Exception e){
                         //   println("err->"+e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                     //   println("에러-> " + error.getMessage());
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();


                params.put("token",token);//토큰 정보도 보내줌
                params.put("userid",send_userid);
                params.put("password",send_password);

                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
   //     println("요청 보냄");
        userid.setText("");
        password.setText("");
    }

    public void println(String data){
        log.append(data);
    }

}
