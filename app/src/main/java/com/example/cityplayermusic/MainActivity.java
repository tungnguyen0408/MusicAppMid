package com.example.cityplayermusic;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainActivity extends AppCompatActivity {
    // members

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    // Hôm nay
    ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // tạo thanh công cụ và tiêu đề ứng dụng
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        // recycleview
        recyclerView = findViewById(R.id.recyclerview);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if(granted){
                fetchSongs();
            }else {
                userResponses(); 
            }
        });

        // khởi động cấp phép bộ nhớ để tạo

        storagePermissionLauncher.launch(permission);

        player = new ExoPlayer.Builder(this).build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // release the player
        if(player.isPlaying()){
            player.stop();
        }
        player.release();
    }

    private void userResponses() {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
            fetchSongs();
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (shouldShowRequestPermissionRationale(permission)){
                // hiển thị UI hướng dẫn tới người dùng và tại sao cần sự cấp quyền này
                // hiển thị dialog
                new AlertDialog.Builder(this)
                        .setTitle("Đang yêu cầu cấp phép")
                        .setMessage("Cho phép chúng tôi truy cập âm thanh trên thiết bị")
                        .setPositiveButton("Cho phép", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "Bạn đã từ chối chúng tôi hiển thị bài hát", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        }
        else {
            Toast.makeText(this, "Bạn đã thoát để hiển thị danh sách bài hát", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchSongs() {
        // định nghĩa danh sách
        List<Song> songs = new ArrayList<>();
        Uri mediaStorageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaStorageUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }else {
            mediaStorageUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        // sắp xếp
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED;
        // lấy ra danh sách
        try (Cursor cursor = getContentResolver().query(mediaStorageUri, projection, null, null, sortOrder)){
        int idColumn = Objects.requireNonNull(cursor).getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        int nameColumn =  cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
        int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
        int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);


        while (cursor.moveToNext()){
            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);
            int duration = cursor.getInt(durationColumn);
            int size = cursor.getInt(sizeColumn);
            long albumId = cursor.getLong(albumColumn);

            // uri bài hát

            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            // uri bộ sưu tập artwork
            Uri albumArtWorkUri = ContentUris.withAppendedId(Uri.parse("drawable/default_bg.jpg"), albumId);

            // bỏ đuôi .mp3 từ tên của bài hát

            name = name.substring(0, name.lastIndexOf("."));

            // bài hát
            Song song = new Song(name, uri, albumArtWorkUri, size, duration);

            // thêm bài hát vao danh sách bài hát
            songs.add(song);
        }
        // hiển thị bài hát 
            showSongs(songs); 
        
        }
    }
    private void showSongs(List<Song> songs) {
        if (songs.isEmpty()){
            Toast.makeText(this, "Không có bài hát nào", Toast.LENGTH_SHORT).show();
            return;
        }

        // lưu các bài hát
        allSongs.clear();
        allSongs.addAll(songs);

        // cập nhật tiêu đề của thanh công cụ
        String title = getResources().getString(R.string.app_name) + " - " + songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        // quản lý giao diện
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // chuyển đổi các bai hát
        songAdapter  = new SongAdapter(this, songs, player);
         // tạo bộ chuyển đổi đến recycleview

        //recyclerView.setAdapter(songAdapter);

        // lựa chọn recycle
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(songAdapter);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        scaleInAnimationAdapter.setFirstOnly(false);
        recyclerView.setAdapter(scaleInAnimationAdapter);


            }
// cài đặt menu và phím tìm kiếm
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn, menu);
        // search
        MenuItem menuItem = menu.findItem(R.id.search_button);
        SearchView searchView = (SearchView) menuItem.getActionView();
        // phương thức tìm bài hát

        SearchSong(searchView) ;

        return super.onCreateOptionsMenu(menu);
    }

    private void SearchSong(SearchView searchView) {
   searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

       @Override
       public boolean onQueryTextSubmit(String query) {
           return false;
       }
       @Override
       public boolean onQueryTextChange(String newText) {
           // những bài hát lọc 
           filterSongs(newText.toLowerCase());
           return true;
       }
   });
    }

    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();
        if (allSongs.size() > 0){
            for (Song song : allSongs){
                if (song.getTitle().toLowerCase().contains(query)){
                    filteredList.add(song);
                }
            }
            if (songAdapter != null ){
                songAdapter.filterSongs(filteredList);
            }
        }
    }
}