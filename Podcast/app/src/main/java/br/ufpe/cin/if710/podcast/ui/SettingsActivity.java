package br.ufpe.cin.if710.podcast.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import br.ufpe.cin.if710.podcast.R;

public class SettingsActivity extends Activity {
    public static final String FEED_LINK = "feedlink";
    JobScheduler jobScheduler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class FeedPreferenceFragment extends PreferenceFragment {

        protected static final String TAG = "FeedPreferenceFragment";
        private SharedPreferences.OnSharedPreferenceChangeListener mListener;
        private Preference feedLinkPref;
        private Preference jobSchedPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // carrega preferences de um recurso XML em /res/xml
            addPreferencesFromResource(R.xml.preferences);

            // pega o valor atual de FeedLink
            feedLinkPref = (Preference) getPreferenceManager().findPreference(FEED_LINK);
            jobSchedPref = (Preference)getPreferenceManager().findPreference("JobScheduling");
            // cria listener para atualizar summary ao modificar link do feed
            mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    feedLinkPref.setSummary(sharedPreferences.getString(FEED_LINK, getActivity().getResources().getString(R.string.feed_link)));
       //             feedLinkPref.setSummary(sharedPreferences.getBoolean("JobScheduling", Boolean.parseBoolean("false")));
                    SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
                    Boolean JobStatus = prefs.getBoolean("JobScheduling", false);
                    Log.d("Log Retornado Pref", String.valueOf(JobStatus));
                    if(JobStatus == true){
                        ((SettingsActivity)getActivity()).agendarJob();
                    }

                }
            };          /* Verifica se o usuário ativou ou nao o jobscheduling nas settings do app,
                        caso ele tenha, prepara o agendamento para 1 segundos. Porém por conta da plataforma,
                        o tempo mínimo é automaticamente ajustado para 15 minutos.*/

            // pega objeto SharedPreferences gerenciado pelo PreferenceManager deste fragmento
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            Boolean JobStatus = prefs.getBoolean("JobScheduling", false);
            Log.d("Log Retornado Pref", String.valueOf(JobStatus));
            if(JobStatus){
                ((SettingsActivity)getActivity()).agendarJob();
            }

            // registra o listener no objeto SharedPreferences
            prefs.registerOnSharedPreferenceChangeListener(mListener);

            // força chamada ao metodo de callback para exibir link atual
            mListener.onSharedPreferenceChanged(prefs, FEED_LINK);

        }
    }
    //Tempo está hardcoded no momento, apesar de qualquer valor menor que 15 minutos está sendo automaticamente setado para 15 min
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void agendarJob(){
        JobInfo.Builder job = new JobInfo.Builder(1, new ComponentName(this, MyService.class));
        PersistableBundle bundle = new PersistableBundle();
        if(job != null) {
            Log.d("Log", "Job nao ta null");
            bundle.putBoolean("chave", true);
            job.setExtras(bundle);
            job.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            job.setPeriodic(1000);
            job.setRequiresCharging(false);
            job.setPersisted(false); // por enquanto
            job.setRequiresDeviceIdle(false);

            jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(job.build());
            Log.d("Log", "Opa buildou o job");
        }
    }
                                            //Faz o cancelamento do job
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void cancelJob(){
        Log.d("Log", "Cancelei o job");
        jobScheduler.cancel(1);

    }

}