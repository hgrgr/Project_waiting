package project.waiting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class Hostview extends LinearLayout {
    ImageView profile;
    TextView hostname;
    TextView address;
    TextView address2;

    public Hostview(Context context) {//생성자들
        super(context);
        init(context);
    }

    public Hostview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.hostview, this, true);//인플레티어로 메모리에 올리고 나서는 객체들 사용가능

        profile = findViewById(R.id.profile);
        hostname = findViewById(R.id.hostname);
        address = findViewById(R.id.address);
        address2 = findViewById(R.id.address2);
        Button map = findViewById(R.id.map);
        Button waiting= findViewById(R.id.waiting);
        Button delete = findViewById(R.id.delete);


    }
}
