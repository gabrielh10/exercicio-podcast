package br.ufpe.cin.if710.podcast.ui.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.ui.DownloadService;

public class XmlFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;

    public XmlFeedAdapter(Context context, int resource, List<ItemFeed> objects) {
        super(context, resource, objects);
        linkResource = resource;
    }

    /**
     * public abstract View getView (int position, View convertView, ViewGroup parent)
     * <p>
     * Added in API level 1
     * Get a View that displays the data at the specified position in the data set. You can either create a View manually or inflate it from an XML layout file. When the View is inflated, the parent View (GridView, ListView...) will apply default layout parameters unless you use inflate(int, android.view.ViewGroup, boolean) to specify a root view and to prevent attachment to the root.
     * <p>
     * Parameters
     * position	The position of the item within the adapter's data set of the item whose view we want.
     * convertView	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view. Heterogeneous lists can specify their number of view types, so that this View is always of the right type (see getViewTypeCount() and getItemViewType(int)).
     * parent	The parent that this view will eventually be attached to
     * Returns
     * A View corresponding to the data at the specified position.
     */


	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.itemlista, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.item_title);
		textView.setText(items.get(position).getTitle());
	    return rowView;
	}
	/**/

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button baixarPoad;
        MediaPlayer mp;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ItemFeed item = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new ViewHolder();
            holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
            holder.item_title.setTextColor(Color.RED);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            holder.item_date.setTextColor(Color.RED);
            convertView.setTag(holder);

            holder.baixarPoad = convertView.findViewById(R.id.item_action);
            holder.baixarPoad.setTextColor(Color.WHITE);
            Log.d("UriAntesdeEntrar", getItem(position).getDownloadUri());

            if (item.getDownloadUri().equals("Nulo")){  //verifica se item ja foi baixado ou nao através da uri
                holder.baixarPoad.setBackgroundColor(Color.RED);
            } else {
                Log.d("log", "Opa a uri ta att");
                holder.baixarPoad.setBackgroundColor(Color.BLUE);
                holder.baixarPoad.setText("Tocar");

            }
            holder.baixarPoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("click", "click");
                    String base = "/storage/emulated/0/Podcasts";       //obtém a possível uri e já a seta.
                    if (item.getDownloadUri().equals("Nulo")) {
                        getItem(position).setDownloadUri(base+getItem(position).getDownloadLink().substring(getItem(position).getDownloadLink().lastIndexOf('/')
                                ,getItem(position).getDownloadLink().length()));

                        ContentValues cv = new ContentValues();         //atualiza no bd
                        cv.put(PodcastDBHelper.EPISODE_FILE_URI, getItem(position).getDownloadUri());
                        String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
                        String[] selection_args = new String[]{getItem(position).getLink()};
                        getContext().getContentResolver().update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selection_args);
                        Log.d("Uri", getItem(position).getDownloadUri());


                        //Prepara o download e chama o service pra baixar e troca o texto do botao
                        String link = getItem(position).getDownloadLink();
                        Intent downloadPoad = new Intent(getContext(), DownloadService.class);
                        downloadPoad.setData(Uri.parse(link));
                        getContext().startService(downloadPoad);
                        holder.baixarPoad.setBackgroundColor(Color.RED);
                        holder.baixarPoad.setEnabled(false);
                        holder.baixarPoad.setText("Baixando...");

                    } else {    //Bom... Se eu ja tiver adicionado a URI no arquivo, eu posso tocá-lo
                        Log.d("UriElse", "Vai tocar o somzinho");
                        Uri uri = Uri.parse(item.getDownloadUri());
                        if(holder.baixarPoad.getText().equals("Tocar")) {
                            if (holder.mp == null) {            //Cria o media player e começa a tocar
                                holder.mp = MediaPlayer.create(getContext(), uri);
                                if (holder.mp != null) {
                                    holder.mp.start();
                                    holder.baixarPoad.setText("Parar");
                                    Log.d("Log", String.valueOf(holder.baixarPoad.getText()));
                                }
                            }else{
                                holder.mp.start();
                            }
                                                      //Para de tocar o podcast
                        }else if(holder.baixarPoad.getText().equals("Parar")){
                            Log.d("log", "Cliquei em parar");
                            holder.mp.pause();
                            holder.baixarPoad.setText("Cont");
                                                    //Continua da posição de onde foi parado
                        } else if(holder.baixarPoad.getText().equals("Cont")){
                            int duracao = holder.mp.getCurrentPosition();
                            holder.mp.seekTo(duracao);
                            holder.mp.start();
                            holder.baixarPoad.setText("Parar");
                        }
                        holder.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                     @Override                  //Quando acabar de tocar, deleta o arquivo
                                     public void onCompletion(MediaPlayer mediaPlayer) {
                                         File file = new File(item.getDownloadUri());
                                         file.delete();
                                         item.setDownloadUri("Nulo");
                                         Log.d("Log", "Opa deletei");
                                     }
                                 });

                    }
                }
            });

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.item_title.setText(getItem(position).getTitle());
        holder.item_date.setText(getItem(position).getPubDate());
        return convertView;

    }
}