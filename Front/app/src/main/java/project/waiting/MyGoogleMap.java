package project.waiting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.util.List;

public class MyGoogleMap extends AppCompatActivity implements AutoPermissionsListener {
    public static final int LOGIN = 101;
    public static final int CHOICE = 102;
    public static final int HOST = 103;
    public static final int CLIENT = 104;
    public static final int MAP = 105;

    MarkerOptions marker;
    MarkerOptions marker2;
    MarkerOptions marker3;
    SupportMapFragment mapFragment;
    GoogleMap map;
    Geocoder geocoder;
    EditText add;
    TextView addview;
    int bit = 0;
    Double addone=null;//latitude 저장용
    Double addtwo=null;//longtitude 저장용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_google_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);//프레그 먼트에 map 띄우기
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                map = googleMap;
                geocoder = new Geocoder(getApplicationContext());//여기 조금 그럼

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {//맵 클릭시 클릭한곳 마크
                    @Override
                    public void onMapClick(LatLng latLng) {
                        marker2 = new MarkerOptions();//표기할 마크
                        marker2.title("선택한 지점");
                        Double latitude = latLng.latitude;
                        Double lotitude = latLng.longitude;
                        addview.setText("");//기존꺼 비워주고 좌표 보여줌
                        addview.setText("현재 좌표: " + latitude + " " + lotitude);
                        addone = latitude;//다른 엑티비티 전달용
                        addtwo = lotitude;
                        marker2.snippet(latitude.toString() + " , " + lotitude.toString());//좌표 표시
                        marker2.position(latLng);
                        marker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark2));
                        map.addMarker(marker2);
                    }
                });
            }
        });
        try{
            MapsInitializer.initialize(this);
        }catch(Exception e){
            e.printStackTrace();
        }


        add= findViewById(R.id.add);
        addview= findViewById(R.id.addview);

        Button msearch = findViewById(R.id.msearch);
        Button myposition = findViewById(R.id.myposition);
        Button set = findViewById(R.id.set);

        set.setOnClickListener(new View.OnClickListener() {//찾은 좌표 보내기
            @Override
            public void onClick(View v) {
                sendaddress();
            }
        });

        msearch.setOnClickListener(new View.OnClickListener() {//입력한 값 지도에서 찾기
            @Override
            public void onClick(View v) {
                String test = add.getText().toString();
                if(test.equals("")){//비어 있는경우
                    add.setHint("찾으시는 장소를 입력해주세요");
                }else {//안 비어 있는경우
                    findPosition();
                }
            }
        });

        myposition.setOnClickListener(new View.OnClickListener() {//내위치 지도에서 보기
            @Override
            public void onClick(View v) {
                Mylocation();
            }
        });
        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    @Override
    protected void onPause() {//엑티비티 정지상태에 들어가면 삭제
        super.onPause();
        if(bit == 1) {
            finish();
        }
    }

    public void sendaddress(){
        if(addone == null || addtwo == null){//좌표 입력 안되어 있을경우
            addview.setText("");
            addview.setText("좌표를 선택해 주세요");
        }
        else{//좌표 입력 됐을경우
            Intent intent = new Intent(getApplicationContext(),HostPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("addone",addone);
            intent.putExtra("addtwo",addtwo);
            bit = 1;
            setResult(RESULT_OK,intent);
            finish();
        }
    }
    public void findPosition(){
        final String wantPosition = add.getText().toString();//검색한곳 가져오기
        List<Address> addresses = null;//검색된 주소 담을곳들 설정

        try{
            addresses = geocoder.getFromLocationName(wantPosition,10);//주소 검색해서 장소가져옴 최대 20개

        }catch(Exception e){
            e.printStackTrace();
        }
        // 여기서 파싱 해줘야 함
        int i=0;
        int size =  addresses.size();
        if(size==0) {//검색 결과 없을경우는 검색결과 없다고 표시
            add.setText("검색결과 없음");
        }else{
            System.out.println("size = " + size);
            while (true) {//전부 표시해줌
                if (size-- == 0) {
                    break;//탈출
                }
                String[] parsing = addresses.get(i).toString().split(",");//검색 결과 하나 담긴다 -- 내부적으로 많은 정보 담겨져 있음
                String name = parsing[0].substring(parsing[0].indexOf("\"") + 1, parsing[0].length() - 2);//정보 잘라서 가져옴
                String latitude = parsing[10].substring(parsing[10].indexOf("=") + 1);
                String lotitude = parsing[12].substring(parsing[12].indexOf("=") + 1);
            //    System.out.println("latitude = " + latitude);
             //   System.out.println("lotitude = " + lotitude);
                LatLng address = new LatLng(Double.parseDouble(latitude), Double.parseDouble(lotitude));//형변환
                addview.setText("");//기존꺼 비워주고 좌표 보여줌
                addview.setText("현재 좌표: " + Double.parseDouble(latitude) + " " + Double.parseDouble(lotitude));
                addone = Double.parseDouble(latitude);
                addtwo = Double.parseDouble(lotitude);
                marker3 = new MarkerOptions();
                marker3.title("찾으시는 장소");
                marker3.snippet(name);
                marker3.position(address);
                marker3.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark3));
                if (i == 0) {//처음 표기될곳만 이동해서 보여준다
                    map.addMarker(marker3);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(address, 15));
                }
            }
        }
    }
    public void Mylocation(){//내위치 마크 찍기
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);//시스템 서비스 사용

        try{
            Location lo = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 권한 허가 때문에 밑줄 생김
            if( lo != null){
                double latitude = lo.getLatitude();
                double lotitude = lo.getLongitude();
            }
            GListener gListener = new GListener();
            long minTime = 20000;
            float minDis = 0;

            locationManager.requestLocationUpdates(//위치 변경시 (설정 + 리스너)
                    LocationManager.GPS_PROVIDER,
                    minTime,minDis,gListener);


        }catch(Exception e){

        }
    }
    class GListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double lotitude = location.getLongitude();

            showLccation(latitude,lotitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    private void showLccation(Double latitude, Double lotitude){
        LatLng cadd = new LatLng(latitude,lotitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cadd,14));

        addMarker(cadd);
    }
    private void addMarker(LatLng cadd){
        if(marker == null){//마커 설정 안되어있을시
            marker = new MarkerOptions();
            marker.title("현재 좌표");
            marker.position(cadd);

            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark));
            map.addMarker(marker);
        }else{
            marker.position(cadd);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, String[] permissions) {
        Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, String[] permissions) {
        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }

}
