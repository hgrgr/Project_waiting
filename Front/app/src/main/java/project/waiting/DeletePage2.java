package project.waiting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class DeletePage2 extends AppCompatActivity {
    static RequestQueue requestQueue;
    final static String delete = "http://10.0.2.2:8002/client/delete";//등록한 호스트 정보들 요청
    Intent intent = new Intent();
    String hostname;
    String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_page2);

        Intent intent2 = getIntent();
        hostname = intent2.getStringExtra("hostname");

        SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
        userid = sp.getString("userid","");//유저 아이디 가져와줌

        Button del = findViewById(R.id.del);
        Button back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {//그냥 돌아가는 버튼
            @Override
            public void onClick(View v) {
                intent.putExtra("state","back");//그냥 돌아감
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        del.setOnClickListener(new View.OnClickListener() {//삭제 요청 - 디비에서 host 현재번호와 waiting client 들 번호 수정 + 알림 해줘야함
            @Override
            public void onClick(View v) {
                call_del();
            }
        });
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    public void call_del(){
        StringRequest request = new StringRequest(Request.Method.POST, delete, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("err")){
                    intent.putExtra("state","ERR");
                }else if(response.equals("myturn")) {//삭제 할수없음 - 지금 내차례일경우
                    intent.putExtra("state","NO");
                }else if(response.equals("NO")){//업데이트 할 애들 없을 경우 - 정상작동과 동일?
                    intent.putExtra("state","OK");
                }else if(response.equals("OK")){//정상작동 했을경우
                    intent.putExtra("state","OK");
                }

                setResult(RESULT_OK,intent);
                finish();
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
                params.put("userid",userid);
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }
}
