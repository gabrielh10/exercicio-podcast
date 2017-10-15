package br.ufpe.cin.if710.podcast.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.NOTIFICATION_SERVICE;

public class StaticReceiver extends BroadcastReceiver {
    private final String TAG = "StaticReceiver";

    @Override           //se app estiver em segundo plano, enviar uma notificação de download terminado
    public void onReceive(Context context, Intent intent) {
        if(MainActivity.status == false) {      //se aplicativo nao está em primeiro plano notifica.
            Log.i(TAG, "intent chegou");
            final Intent notificationIntent = new Intent(context, MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            final Notification notification = new Notification.Builder(context)
                    .setContentTitle("Opa, Acabou de baixar... Volte para a lista de ep.")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentIntent(pendingIntent).build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
        }else{                                       //se está em primeiro plano, atualiza a lista de items
            Intent Tintent = new Intent(context, MainActivity.class);
            context.startActivity(Tintent);

            Log.d("Download", "Acabou mlk");
            Toast.makeText(context,"Download Acabou", Toast.LENGTH_LONG).show();
        }
    }
}