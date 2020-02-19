//host목록에서 위치확인 클릭되었을경우 실행되는 엑티비티
package project.waiting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
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

public class AddressOnMap extends AppCompatActivity implements AutoPermissionsListener {

    SupportMapFragment mapFragment;
    GoogleMap map;
    TextView log;
    MarkerOptions marker;
    String Slatitude;
    String Slotitude;
    String hostname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_on_map);

        Intent intent = getIntent();

         Slatitude = intent.getStringExtra("latitude");
         Slotitude = intent.getStringExtra("lotitude");
         hostname = intent.getStringExtra("hostname");
         log = findViewById(R.id.log);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);//프레그 먼트에 map 띄우기
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                map = googleMap;
                viewOnMyPosition(Double.parseDouble(Slatitude),Double.parseDouble(Slotitude),hostname);//좌표를 지도에 보여준다 - 좌표 형변환 해서 보내준다
            }
        });
        try{
            MapsInitializer.initialize(this);
        }catch(Exception e){
            e.printStackTrace();
        }

        AutoPermissions.Companion.loadAllPermissions(this, 101);


    }

    public void viewOnMyPosition(Double latitude, Double lotitude,String hostname){
        log.setText("latitude = " + latitude + "logtitude = " + lotitude);
        LatLng cadd = new LatLng(latitude,lotitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cadd,15));

        addMarker(cadd,hostname);

    }

    private void addMarker(LatLng cadd,String hostname){
        if(marker == null){//마커 설정 안되어있을시
            marker = new MarkerOptions();
            marker.title(hostname);
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
