//여기서 FCM에서 보내주는 메시지 받아서 처리할수 있음
package project.waiting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    NotificationManager manager;
    private static String ALARM_CH_ID ="ch1";
    private static String ALARM_CH_NAME ="ch1";

    static SharedPreferences.Editor edit = null;
    public MyFirebaseMessagingService() {
    }

//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//
//
//    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {//firebase에서 알림 올때 호출
        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        System.out.println("title = " + title);
        String body = data.get("body");
        System.out.println("body = " + body);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = null;
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){//버전체크 버전마다 다름
            if(manager.getNotificationChannel(ALARM_CH_ID)==null){
             manager.createNotificationChannel(new NotificationChannel(ALARM_CH_ID,ALARM_CH_NAME,NotificationManager.IMPORTANCE_DEFAULT));
            }
            builder = new NotificationCompat.Builder(this,ALARM_CH_ID);
        }else{
            builder = new NotificationCompat.Builder(this);
        }

        Intent intent = new Intent(this,ClientPage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,101,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(title);
        builder.setContentText( body);
        builder.setSmallIcon(android.R.drawable.ic_menu_view);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        Notification noti = builder.build();
        manager.notify(1,noti);
    }

    @Override
    public void onNewToken(String s) {//토큰 발급시 호출 - 서버호출해서 토큰 저장해야함
        super.onNewToken(s);

    }

}
