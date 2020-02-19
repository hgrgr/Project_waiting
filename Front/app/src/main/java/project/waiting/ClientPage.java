package project.waiting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ClientPage extends AppCompatActivity {
    public static final int LOGIN = 101;
    public static final int CHOICE = 102;
    public static final int HOST = 103;
    public static final int CLIENT = 104;
    public static final int DEL = 105;
    static RequestQueue requestQueue;
    RecyclerView searchRecyclerView;
    RecyclerView waitingRecyclerView;
    HostAdapter searchAdapter;
    HostAdapter waitAdapter;


    EditText inputHost;
    TextView err;
    final static String search = "http://10.0.2.2:8002/client/search";//등록한 호스트 정보들 요청
    final static String mywaits = "http://10.0.2.2:8002/client/mywaits";//등록한 호스트 정보들 요청
    final static String testing = "http://10.0.2.2:8002/client/testing";//등록한 호스트 정보들 요청

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_page);
        inputHost = findViewById(R.id.inputHost);
        err =findViewById(R.id.err);
        Button search = findViewById(R.id.search);





        //자신이 웨이팅 신청 한곳 리사이클러뷰로 보여줌
        waitingRecyclerView = findViewById(R.id.waitingRecyclerView);//리사이클러 뷰 설정 - 어탭터 연결
        LinearLayoutManager layoutManager= new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        waitingRecyclerView.setLayoutManager(layoutManager);
        waitAdapter = new HostAdapter();
        waitingRecyclerView.setAdapter(waitAdapter);
        waitAdapter.setOnItemClickListener(
                new HostAdapter.OnitemClickListener() {
                    @Override
                    public void onItemClick(View v, int pos, int bit, HostAdapter.Datas data) {
//                        log.setText("");
//                        log.setText(pos);
                        //담근 뷰에 정보 꺼내줌
                        if(bit == 0) {//뷰가 선택되었을경우//카드뷰 클릭시 hostingpage로 이동
                            Intent intent = new Intent(getApplicationContext(),waitingPage.class);
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

                            Intent intent = new Intent(getApplicationContext(),waitingPage.class);
                            intent.putExtra("latitude",data.latitude);
                            intent.putExtra("lotitude",data.lotitude);
                            intent.putExtra("hostname",data.hostname);
                            startActivity(intent);
                        }
                        else if(bit ==3){//waiting 선택 - host이름 넣어서 전달해줌 ->디비 조회해서 대기자 등등 정보 가져와야함

                            Intent intent = new Intent(getApplicationContext(),DeletePage2.class);
                            intent.putExtra("latitude",data.latitude);
                            intent.putExtra("lotitude",data.lotitude);
                            intent.putExtra("hostname",data.hostname);
                            startActivityForResult(intent,DEL);
                        }



                    }
                }
        );
        //검색결과 보여줄 어댑터와 리사이클러뷰 설정해줌
        searchRecyclerView = findViewById(R.id.searchRecyclerView);//리사이클러 뷰 설정 - 어탭터 연결
        LinearLayoutManager layoutManager2= new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        searchRecyclerView.setLayoutManager(layoutManager2);
        searchAdapter = new HostAdapter();
        searchRecyclerView.setAdapter(searchAdapter);
        searchAdapter.setOnItemClickListener(
                new HostAdapter.OnitemClickListener() {
                    @Override
                    public void onItemClick(View v, int pos, int bit, HostAdapter.Datas data) {
//                        log.setText("");
//                        log.setText(pos);
                        //담근 뷰에 정보 꺼내줌
                        if(bit == 0) {//뷰가 선택되었을경우//카드뷰 클릭시 hostingpage로 이동
                            Intent intent = new Intent(getApplicationContext(),waitingPage.class);
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

                            Intent intent = new Intent(getApplicationContext(),waitingPage.class);
                            intent.putExtra("latitude",data.latitude);
                            intent.putExtra("lotitude",data.lotitude);
                            intent.putExtra("hostname",data.hostname);
                            startActivity(intent);
                        }else if(bit ==3){//삭제선택 -search어댑터 초기화 해줌
                            searchAdapter.clear();
                            searchAdapter.notifyDataSetChanged();//변경 사항 적용
                        }




                    }
                }
        );
        ////////////////////////////////////////////////////////////////////

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//inputHost가 빈곳이면
                if(inputHost.getText().toString().equals("")){
                    inputHost.setHint("찾고 싶은 host이름 입력 하세요");
                }else
                {
                    hostSearch();
                }
            }
        });
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        reqHost();//내가 웨이팅한 호스트들 요청
    }


    public void reqHost(){//내가 웨이팅하고 있는 호스트들 요청
        StringRequest request = new StringRequest(Request.Method.POST, mywaits, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {//받아온 host json 객체들 어뎁터에 넣어준다
                waitAdapter.clear();
                Gson gson = new Gson();
                HostList hostList = gson.fromJson(response,HostList.class);

                for(int i=0;i<hostList.hostData.size();i++){
                    Host host = hostList.hostData.get(i);
                    waitAdapter.addItem(host);
                }
                waitAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
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
                params.put("userid",sp.getString("userid",""));
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    public void hostSearch(){
        final String hostname = inputHost.getText().toString();
        inputHost.setText("");
        inputHost.setHint("찾고싶은 host이름 입력");
        StringRequest request = new StringRequest(Request.Method.POST, search, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {//받은 호스트 정보 배열들 gson사용하여 자바 객체로 변경
           //   inputHost.setText(response);
                // 여기서 리사이클러뷰에 host 정보들 담아준다
                //     log.setText(response);//받은데이터 표기
                searchAdapter.clear();
                Gson gson = new Gson();
                HostList hostList = gson.fromJson(response, HostList.class);

                for(int i =0;i<hostList.hostData.size();i++){
                    Host host = hostList.hostData.get(i);
                    searchAdapter.addItem(host);
                }
                searchAdapter.notifyDataSetChanged();//변경 사항 적용

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
                params.put("hostname",hostname);
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DEL){
            if(resultCode == RESULT_OK){
                String state = data.getStringExtra("state");//상태 뽑아줌
                if(state.equals("OK")){//정상작동
                    reqHost();//내가 웨이팅한 호스트들 요청
                }else if(state.equals("NO")){//내차례여서 삭제 불가능
                    reqHost();//내가 웨이팅한 호스트들 요청
                    err.setText("자신의 차례에서는 웨이팅을 취소할수 없습니다");
                }else if(state.equals("ERR")){
                    err.setText("오류 발생");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        reqHost();//내가 웨이팅한 호스트들 요청
        searchAdapter.clear();
        searchAdapter.notifyDataSetChanged();//변경 사항 적용
        super.onResume();
    }
}
