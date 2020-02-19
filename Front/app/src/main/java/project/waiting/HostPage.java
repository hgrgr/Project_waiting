package project.waiting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HostPage extends AppCompatActivity {
    public static final int LOGIN = 101;
    public static final int CHOICE = 102;
    public static final int HOST = 103;
    public static final int CLIENT = 104;
    public static final int MAP = 105;
    public static final int DEL = 106;
    static SharedPreferences.Editor edit = null;

    Double addone;
    Double addtwo;
    TextView address;
    TextView status;
    TextView log;
    EditText host_name;
    RecyclerView recyclerView;
    HostAdapter adapter;

    static RequestQueue requestQueue;
    final static String join = "http://10.0.2.2:8002/host/join"; //로그아웃 요청 서버 db에 status 바꿔줘야함
    final static String gethosts = "http://10.0.2.2:8002/host/gethosts";//등록한 호스트 정보들 요청

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_page);

        address = findViewById(R.id.address);
        host_name = findViewById(R.id.host_name);
        status = findViewById(R.id.status);
        log = findViewById(R.id.log);

        Button hosting = findViewById(R.id.hosting);
        Button search = findViewById(R.id.serach);
        Button retry = findViewById(R.id.retry);

        recyclerView = findViewById(R.id.recyclerView);//리사이클러 뷰 설정 - 어탭터 연결

        LinearLayoutManager layoutManager= new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HostAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(
                new HostAdapter.OnitemClickListener() {
                    @Override
                    public void onItemClick(View v, int pos, int bit, HostAdapter.Datas data) {
//                        log.setText("");
//                        log.setText(pos);
                        //담근 뷰에 정보 꺼내줌
                        if(bit == 0) {//뷰가 선택되었을경우//카드뷰 클릭시 hostingpage로 이동
                            Intent intent = new Intent(getApplicationContext(),HostingPage.class);
                            intent.putExtra("latitude",data.latitude);
                            intent.putExtra("lotitude",data.lotitude);
                            intent.putExtra("hostname",data.hostname);
                            startActivity(intent);

                        }else if(bit ==1){//map 선택 - 맵 좌표 넣어서 잔달해줌 -> 맵에 위치 찍어줘야함

                            Intent intent = new Intent(getApplicationContext(),AddressOnMap.class);
                            intent.putExtra("latitude",data.latitude);
                            intent.putExtra("lotitude",data.lotitude);
                            intent.putExtra("hostname",data.hostname);
                            startActivity(intent);

                        }else if(bit ==2){//waiting 선택 - host이름 넣어서 전달해줌 ->디비 조회해서 대기자 등등 정보 가져와야함

                            Intent intent = new Intent(getApplicationContext(),HostingPage.class);
                            intent.putExtra("latitude",data.latitude);
                            intent.putExtra("lotitude",data.lotitude);
                            intent.putExtra("hostname",data.hostname);
                            startActivity(intent);
                        }else if(bit ==3){//delete선택 - 대기자 없고 stop상태일경우 삭제해준다

                            Intent intent = new Intent(getApplicationContext(),DeletePage.class);
                            intent.putExtra("latitude",data.latitude);
                            intent.putExtra("lotitude",data.lotitude);
                            intent.putExtra("hostname",data.hostname);
                            startActivityForResult(intent,DEL);//다시 넘어올때 어댑터 설정 다시해준다
                        }




                    }
                }
        );
        hosting.setOnClickListener(new View.OnClickListener() {//서버로 호스트 등록
            @Override
            public void onClick(View v) {
                host_req();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {//맵 검색
            @Override
            public void onClick(View v) {
                search_add();
            }
        });
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHost();//등록한 호스트정보들 요청
            }
        });
        requestHost();//requestQueue보다 아래에 있어야할듯
    }
    public void requestHost(){//등록한 호스트정보들 요청
        StringRequest request = new StringRequest(Request.Method.POST, gethosts, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {//받은 호스트 정보 배열들 gson사용하여 자바 객체로 변경
                // 여기서 리사이클러뷰에 host 정보들 담아준다
           //     log.setText(response);//받은데이터 표기
                adapter.clear();
                Gson gson = new Gson();
                HostList hostList = gson.fromJson(response, HostList.class);

                for(int i =0;i<hostList.hostData.size();i++){
                    Host host = hostList.hostData.get(i);
                    adapter.addItem(host);
                }
                adapter.notifyDataSetChanged();//변경 사항 적용

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override //response를 UTF8로 변경해주는 소스코드-https://honeyinfo7.tistory.com/78
            protected Response<String> parseNetworkResponse(NetworkResponse response) {//데이터 받을때 한글깨짐 현상 해결 위해 오버라이딩
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    // log error
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    // log error
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                String userid = sp.getString("userid","");
                params.put("userid",userid);//유저 아이디 넣어서 보내줌
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void host_req(){//서버에 요청 보낸다 - 호스트 데이터 저장 - (보낼때 "userid" 보내야함 nick보내면 중복 때문에 곤란함)
        final String hname = host_name.getText().toString();
        final String add = address.getText().toString();

        if(hname.equals("") || add.equals("")) {//host 이름이나 addresss둘중 하나라도 null이면 못보냄
            host_name.setText("");
            host_name.setHint("host이름과 좌표를 설정해주세요");
        }else{//둘다 등록한 경우
            postHost(); // 서버에 등록 요청
        }

    }
    public void postHost(){//서버로 보내줘야할것 : 좌표 호스트 이름 , 등록하는 유저 이름
        final String hname = host_name.getText().toString();
        final String add = address.getText().toString();
        StringRequest request = new StringRequest(Request.Method.POST, join, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                status.setText("");
                status.setText(response);
                requestHost();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                SharedPreferences sp = getSharedPreferences("CHECK",MODE_PRIVATE);
                String userid = sp.getString("userid","");
                params.put("name",hname);
                params.put("addone",Double.toString(addone));
                params.put("addtwo",Double.toString(addtwo));
                params.put("userid",userid);
                return params;

            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
        host_name.setText("");
        address.setText("");
    }

    public void search_add(){//구글 api 가져와서 보여줌
        Intent intent = new Intent(getApplicationContext(), MyGoogleMap.class);
        startActivityForResult(intent,MAP);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//맵에서 선택한 좌표 받기
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MAP){

            if(resultCode == RESULT_OK){
                addone = data.getDoubleExtra("addone",0);
                addtwo = data.getDoubleExtra("addtwo",0);
                address.setText("");
                address.setText("좌표: " +addone + " " + addtwo);
            }
        }else if(requestCode == DEL){

            if(resultCode == RESULT_OK){
                String state = data.getStringExtra("state");//상태 뽑아줌
                if(state.equals("err")){//에러 발생시
                    status.setText("");
                    status.setText("삭제요청 실패");
                }else if(state.equals("fail")){//조건 불만족
                    status.setText("");
                    status.setText("stop상태+대기자가 없어야 삭제가 가능합니다");
                }else if(state.equals("success")){//성공시
                    status.setText("");
                    status.setText("삭제 성공");
                    requestHost();
                }else if(state.equals("no")){//그냥 돌아왔을시
                    status.setText("ㅁ");
                }
            }
        }
    }
}
