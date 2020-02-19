package project.waiting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class DeletePage extends AppCompatActivity {
    String hostname;
    Intent intent = new Intent();
    static RequestQueue requestQueue;
    final static String delreq = "http://10.0.2.2:8002/host/delete"; //호스트 삭제요청
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_page);

        Button del =findViewById(R.id.del);
        Button back = findViewById(R.id.back);
        Intent hintent  = getIntent();
        hostname=hintent.getStringExtra("hostname");

        del.setOnClickListener(new View.OnClickListener() {//호스트 삭제하기
            @Override
            public void onClick(View v) {
                deleteHost();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {//그냥 돌아가기
            @Override
            public void onClick(View v) {
                back_noChange();
            }
        });
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    public void back_noChange(){//그냥 원래 화면으로 돌아간다 - 변함 없는 상태 넣어줌
        intent.putExtra("state","no");
        setResult(RESULT_OK,intent);
        finish();
    }

    public void deleteHost(){

        StringRequest request = new StringRequest(Request.Method.POST, delreq, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.equals("err")){//에러 발생시
                    intent.putExtra("state","err");
                }else if(response.equals("fail")){//조건 불만족
                    intent.putExtra("state","fail");
                }else if(response.equals("success")){//조건 만족 삭제 성공
                    intent.putExtra("state","success");
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
                 return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();//pause 상태에 들어가면 종료해줌
    }
}
