package br.ufpe.cin.if710.podcast.ui;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;

import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_TITLE;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.columns;

public class AtualizarList extends IntentService {
    MyService jobService;

    public AtualizarList() {
        super("AtualizarList");
    }
                //Metódo que baixa o feed e atualiza o nosso bd, usado no jobScheduling
    protected void onHandleIntent(Intent intent) {
        MainActivity main = new MainActivity();
        List<ItemFeed> itemList = null;
        try {
            itemList = XmlFeedParser.parse(main.getRssFeed(MainActivity.RSS_FEED));

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ItemFeed item : itemList) {
            ContentValues cv = new ContentValues();

            String query = EPISODE_TITLE + " =?";
            String[] selectionArgs = new String[]{item.getTitle()};
    //        Log.d("Log do Job", "Tamo no loop");
            Cursor cursor = getContentResolver().query(PodcastProviderContract.EPISODE_LIST_URI, columns, query, selectionArgs, null);
            if (cursor != null && (cursor.getCount() == 0)) {
                Log.d("Log do JobSchedule", "Pode add, nao tem ngn igual");
                cv.put(PodcastDBHelper.EPISODE_DATE, item.getPubDate());
                cv.put(PodcastDBHelper.EPISODE_DESC, item.getDescription());
                cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
                cv.put(PodcastDBHelper.EPISODE_LINK, item.getLink());
                cv.put(EPISODE_TITLE, item.getTitle());

                //   cv.put(PodcastDBHelper.EPISODE_FILE_URI, "");
                getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, cv);
            }
        }     //Envia broadcast para o static receiver que trata enviando notificação, avisando da atualizaçao da lista de eps
        sendBroadcast(new Intent("br.ufpe.cin.if710.broadcasts.jobschedule"));
        Log.d("Log", "Opa atualizamos a lista de boas");
     //   jobService.jobFinished();
    }
}