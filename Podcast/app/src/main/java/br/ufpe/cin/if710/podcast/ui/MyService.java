package br.ufpe.cin.if710.podcast.ui;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MyService extends JobService {

    @Override       //Dispara o service responsável por atualizar a lista de episodios quando o jobSchedule está ativado
                    //nas settings do app
    public boolean onStartJob(JobParameters jobParameters) {

        PersistableBundle bundle = jobParameters.getExtras();
        Intent intent = new Intent(getApplicationContext(), AtualizarList.class);
    //    intent.setData()
        getApplicationContext().startService(intent);
        Log.d("Log", "Executei 1");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Intent downloadService = new Intent (getApplicationContext(),DownloadService.class);
        getApplicationContext().stopService(downloadService);
        return true;
    }


}
