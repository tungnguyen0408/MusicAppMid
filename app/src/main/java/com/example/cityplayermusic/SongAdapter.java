package com.example.cityplayermusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // Members

    Context context;
    List<Song> songs;
    ExoPlayer player;

    // Constructor


    public SongAdapter(Context context, List<Song> songs, ExoPlayer player) {
        this.context = context;
        this.songs = songs;
        this.player = player;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       // inflate song row item layout

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
// bài hát hiện tại và màn hình hiện tại
        Song song = songs.get(position);
        SongViewHolder viewHoler = (SongViewHolder) holder;

        // cài đặt giá trị cho các views
        viewHoler.titleHolder.setText(song.getTitle());
        viewHoler.durationHolder.setText(getDuration(song.getDuration()));
        viewHoler.sizeHolder.setText(getSize(song.getSize()));

        // artworks

        Uri artworkUri = song.getArtwork();
        if (artworkUri != null){
            viewHoler.artworkHolder.setImageURI(artworkUri);

            // đảm bảo rằng uri là một artwork
            if(viewHoler.artworkHolder.getDrawable() ==  null){
                viewHoler.artworkHolder.setImageResource(R.drawable.default_bg);
            }
        }

        //chơi nhạc khi ấn vào item
        viewHoler.itemView.setOnClickListener(view -> {
            // playing the song
            if(!player.isPlaying()){
                player.setMediaItems(getMediaItems(), position,0);
            }
            else {
                player.pause();
                player.seekTo(position,0);
            }
            //prepare and play
            player.prepare();
            player.play();
            Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show();
        }  );
    }

    private List<MediaItem> getMediaItems() {
        // define a list of media items
        List<MediaItem>mediaItems = new ArrayList<>();
        for(Song song : songs){
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetadata(song))
                    .build();
            // add the media item to media items list
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getArtwork())
                .build();
    }


    // View Holder
    public static class SongViewHolder extends RecyclerView.ViewHolder {
        // Members
        ImageView artworkHolder;
        TextView titleHolder, durationHolder, sizeHolder;

        public SongViewHolder(@NonNull View itemView){
            super(itemView);

            artworkHolder = itemView.findViewById(R.id.artworkview);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById(R.id.durationView);
            sizeHolder = itemView.findViewById(R.id.sizeView);
        }

    }


    @Override
    public int getItemCount() {
        return songs.size();
    }


    // lọc
    @SuppressLint("NotifyDataSetChanged")
    public void filterSongs (List<Song> filteredList){
        songs = filteredList;
        notifyDataSetChanged();
    }


    private String getDuration (int totalDuration){
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = ((totalDuration%(1000*60*60))/(1000*60));
        int secs = (((totalDuration%(1000*60*60))%(1000*60))%(1000*60))/1000;

        if (hrs < 1){
            totalDurationText = String.format("%02d:%02d", min, secs);

        }else {
            totalDurationText = String.format("%1d:%02d:%02d", hrs, min, secs);

        }
        return totalDurationText;
    }

    // size

    private String getSize (long bytes){
        String hrSize;
        double k = bytes/1024.0;
        double m = ((bytes/1024.0)/1024.0);
        double g = (((bytes/1024.0)/1024.0)/1024.0);
        double t = ((((bytes/1024.0)/1024.0)/1024.0)/1024.0);

        // Định dạng
        DecimalFormat dec = new DecimalFormat("0.00");

        if (t>1){
            hrSize = dec.format(t).concat(" TB");

        }else if (g > 1){
            hrSize = dec.format(g).concat(" GB");
        }else if (m > 1){
            hrSize = dec.format(m).concat(" MB");
        }else if (k > 1){
            hrSize = dec.format(k).concat(" KB");
        }else {
            hrSize = dec.format(g).concat(" Bytes");
        }
        return hrSize;

    }


}
