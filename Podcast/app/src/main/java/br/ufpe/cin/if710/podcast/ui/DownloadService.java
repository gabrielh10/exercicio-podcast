package br.ufpe.cin.if710.podcast.ui;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {
      public static final String DOWNLOAD_COMPLETE = "br.ufpe.cin.if710.services.action.DOWNLOAD_COMPLETE";


    public DownloadService() {
        super("DownloadService");
    }

    @Override           //Faz o download do podcast e envia um broadcast avisando que acabou
    public void onHandleIntent(Intent i) {
        try {
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
            root.mkdirs();
            File output = new File(root, i.getData().getLastPathSegment());
            if (output.exists()) {
                output.delete();
            }
            Log.d("file path", i.getData().toString());
            URL url = new URL(i.getData().toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            FileOutputStream fos = new FileOutputStream(output.getPath());
            BufferedOutputStream out = new BufferedOutputStream(fos);
            try {
                InputStream in = c.getInputStream();
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
                Log.d("Log", output.getPath());
            }
            finally {
                Log.d("cabo", "");
                fos.getFD().sync();
                out.close();
                c.disconnect();
            }
            Log.d("Broadcast", "Send Acabei");

            sendBroadcast(new Intent("br.ufpe.cin.if710.broadcasts.exemplo"));
        } catch (IOException e2) {
            Log.e(getClass().getName(), "Exception durante download", e2);
        }
    }
}