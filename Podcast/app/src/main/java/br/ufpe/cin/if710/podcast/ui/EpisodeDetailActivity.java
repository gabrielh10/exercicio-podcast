package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;

public class EpisodeDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);
                                                            //pega os detalhes do episodio e os exibe
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String pubDate = getIntent().getStringExtra("pubDate");
        String link = getIntent().getStringExtra("link");
        String downloadLink = getIntent().getStringExtra("downloadLink");
      //  Log.d("item title", title);

        TextView itemTitle = findViewById(R.id.itemTitle);
        TextView itemDescription = findViewById(R.id.itemDescription);
        TextView itemPubDate = findViewById(R.id.itemPubDate);
        TextView itemLink = findViewById(R.id.itemLink);
        TextView itemDownloadLink = findViewById(R.id.itemDownloadLink);

        itemTitle.setText(title);
        itemDescription.setText(description);
        itemPubDate.setText(pubDate);
        itemLink.setText(link);
        itemDownloadLink.setText(downloadLink);
        //TODO preencher com informações do episódio clicado na lista...
    }
}
