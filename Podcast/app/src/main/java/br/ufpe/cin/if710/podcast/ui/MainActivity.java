package br.ufpe.cin.if710.podcast.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_TITLE;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.columns;

public class MainActivity extends Activity{
    public static boolean status = false;   //bool pra checar se está ou nao em primeiro plano

    //ao fazer envio da resolucao, use este link no seu codigo!
    public static final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";

    //TODO teste com outros links de podcast

    private ListView items;

    public void checkPermission() {         //checa se tem a permissao necessária para o download
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    public boolean conexao(){       //checa se existe conexao
        ConnectivityManager conect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conect.getActiveNetworkInfo() != null && conect.getActiveNetworkInfo().isAvailable()
                && conect.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                                                        //checa as permissoes e att status
        items = (ListView) findViewById(R.id.items);
        checkPermission();
        status = true;
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();                            //atualiza a lista de itens (se houver conexao) e status
        Log.d("Log", "On Start vai começar task");
        new DownloadXmlTask().execute(RSS_FEED);
        status = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
        status = false;
    }

    public void atualizaFeed(List<ItemFeed> itemList, String... params){
                                            //Se houver conexao, atualiza feed e coloca no bd, é chamado pela async task
        try {
            itemList = XmlFeedParser.parse(getRssFeed(params[0]));
            for (ItemFeed item : itemList) {
                ContentValues cv = new ContentValues();

                String query = EPISODE_TITLE + " =?" ;
                String[] selectionArgs = new String[]{item.getTitle()};

                Cursor cursor = getContentResolver().query(PodcastProviderContract.EPISODE_LIST_URI, columns, query, selectionArgs, null);
                if(cursor != null && (cursor.getCount() == 0)) {
                    Log.d("Log", "Pode add, nao tem ngn igual" );
                    cv.put(PodcastDBHelper.EPISODE_DATE, item.getPubDate());
                    cv.put(PodcastDBHelper.EPISODE_DESC, item.getDescription());
                    cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
                    cv.put(PodcastDBHelper.EPISODE_LINK, item.getLink());
                    cv.put(EPISODE_TITLE, item.getTitle());

                    //   cv.put(PodcastDBHelper.EPISODE_FILE_URI, "");
                    getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, cv);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }


    public class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {


        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();
            if (conexao()) {            //se tiver conexão, pega os dados diretamente da fonte e add no bd
                Log.d("Log", "Baixando XML");
                atualizaFeed(itemList, params[0]);
            }
                // Se não, utiliza o que já estiver no bd
                Log.d("Log", "BD msm");
                Cursor cursor = getContentResolver().query(PodcastProviderContract.EPISODE_LIST_URI, null, "", null, null);

                while (cursor.moveToNext()) {
                    String itemTitle = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.TITLE));
                    String itemLink = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_LINK));
                    String itemDate = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.DATE));
                    String itemDescription = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.DESCRIPTION));
                    String itemDownloadLink = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.DOWNLOAD_LINK));
                    String itemDownloadUri = cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_URI));
                    itemList.add(new ItemFeed(itemTitle, itemLink, itemDate, itemDescription, itemDownloadLink, itemDownloadUri));
                }
                cursor.close();


            return itemList;
         }
        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            //Adapter Personalizado
            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            //atualizar o list view
            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);

                                            //Acrescentado focusable no xml para que o click no item funcione
                                            //Ao clicar na tela irá mostrar todos os detalhes do podcast
            items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    XmlFeedAdapter adapter = (XmlFeedAdapter) parent.getAdapter();
                    ItemFeed item = adapter.getItem(position);

                    Intent episodeDetails = new Intent(getApplicationContext(), EpisodeDetailActivity.class);
                    episodeDetails.putExtra("title", item.getTitle());
                    episodeDetails.putExtra("description", item.getDescription());
                    episodeDetails.putExtra("pubDate", item.getPubDate());
                    episodeDetails.putExtra("link", item.getLink());
                    episodeDetails.putExtra("downloadLink", item.getDownloadLink());
                    startActivity(episodeDetails);

                }
            });

        }
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    public String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }
}
