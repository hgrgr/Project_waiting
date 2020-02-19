package project.waiting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.sql.DriverManager.println;

public class MainActivity extends AppCompatActivity {
    public static final int LOGIN = 101;
    public static final int CHOICE = 102;
    public static final int HOST = 103;
    public static final int CLIENT = 104;
    public static final String CHECK="LOG_CHECK";
    static SharedPreferences.Editor edit = null;

    EditText editText;
    EditText editText2;
    EditText editText3;
    TextView textView;
    final static String join = "http://10.0.2.2:8002/auth/join";
    final static String check = "http://10.0.2.2:8002/auth/check";

    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
        String temp = sp.getString("status","");
        edit =sp.edit();

        if(temp.equals("login")){//로그인 상태일 경우 실행
            String name = sp.getString("name","");
            Intent intent = new Intent(getApplicationContext(),Choice.class);
            intent.putExtra("name",name);//닉네임 넣어서 전달해줌
            startActivityForResult(intent, CHOICE);
        }else {
            setContentView(R.layout.activity_main);

            //   Intent intent = new Intent(getApplicationContext(), MyServiceCheck.class);
            //  startService(intent);
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {//앱 실행시 받아옴
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String newToken =instanceIdResult.getToken();
                    SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                    edit=sp.edit();
                    edit.putString("token",newToken);
                    edit.commit();
                  //  println("token= " + sp.getString("token",""));
                    //println("token = " +  newToken);
                }
            });

            editText = findViewById(R.id.editText);
            editText2 = findViewById(R.id.editText2);
            editText3 = findViewById(R.id.editText3);
            textView = findViewById(R.id.textView3);
            //println("this => " + temp);
            Button button = findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {//회원 가입
                @Override
                public void onClick(View v) {

                    makeRequest();
                }
            });
            Button login = findViewById(R.id.login);
            login.setOnClickListener(new View.OnClickListener() {//로그인 화면으로 이동
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                }
            });
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
        }

    }


    public void makeRequest() {
        final String userid = editText.getText().toString();
        final String password = editText2.getText().toString();
        final String name = editText3.getText().toString();

        StringRequest request = new StringRequest(Request.Method.POST, join,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("No")){
                            textView.setText("정보를 입력해주세요");
                        }else if(response.equals("already")){
                            textView.setText("해당 아이디가 이미 존재합니다");
                        }else if(response.equals("OK")){
                            textView.setText("회원가입 완료");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //println("에러-> " + error.getMessage());
                    }
                }
                ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("userid",userid);
                params.put("password",password);
                params.put("name",name);

                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
        //println("요청 보냄");
        editText.setText("");
        editText2.setText("");
        editText3.setText("");
    }

    public void println(String data){
        textView.append(data);
    }
}
