package com.laithlab.core.activity;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.laithlab.core.R;
import com.laithlab.core.adapter.SongListAdapter;
import com.laithlab.core.converter.DTOConverter;
import com.laithlab.core.db.Playlist;
import com.laithlab.core.db.Song;
import com.laithlab.core.dto.MusicContent;
import com.laithlab.core.dto.SongDTO;
import com.laithlab.core.fragment.PlaylistCallback;
import com.laithlab.core.fragment.PlaylistSelectDialog;
import com.laithlab.core.utils.MusicDataUtility;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class PlaylistActivity extends AppCompatActivity implements SongListAdapter.ClickListener, PlaylistCallback {

    private static String ALBUM_ID_PARAM = "albumId";

    private DrawerLayout drawerLayout;
    private SongListAdapter songListAdapter;

    private MusicContent musicContent;
    private List<SongDTO> songs;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

    public static Intent getIntent(Context context, String id) {
        Intent artistActivity = new Intent(context, PlaylistActivity.class);
        artistActivity.putExtra(ALBUM_ID_PARAM, id);
        return artistActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Bundle extras = getIntent().getExtras();
        musicContent = extras.getParcelable("musicContent");
//        if (currentPlaylist == null) {
//            currentPlaylist = DTOConverter.getAlbumDTO(MusicDataUtility.getAlbumById(extras.getString(ALBUM_ID_PARAM), this));
//        }

        songs = DTOConverter.getSongList(MusicDataUtility.getSongsFromList(musicContent, this));

        TextView albumTitle = (TextView) findViewById(R.id.txt_album);
        albumTitle.setText(musicContent.getPlaylistName());

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary));
        View tiltedView = findViewById(R.id.tilted_view);
        tiltedView.setPivotX(0f);
        tiltedView.setPivotY(0f);
        tiltedView.setRotation(-5f);

        RecyclerView songList = (RecyclerView) findViewById(R.id.rv_songs_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        songList.setLayoutManager(layoutManager);
        songListAdapter = new SongListAdapter(songs, this);
        songList.setAdapter(songListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if (i == R.id.search_menu_item) {
            startActivity(SearchActivity.getIntent(this));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {
            Intent playerActivity = new Intent(this, SwipePlayerActivity.class);
            playerActivity.putParcelableArrayListExtra("songs", (ArrayList<? extends Parcelable>) songs);
            playerActivity.putExtra("songPosition", position);
            startActivity(playerActivity);
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
        songListAdapter.toggleSelection(position);
        int count = songListAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public void playlistChosen(int position) {
        List<Playlist> playlists = MusicDataUtility.getPlayists(this);
        Playlist playlistSelected = playlists.get(position);

        List<Song> songs = MusicDataUtility.getSongsFromList(musicContent, this);
        if(songs !=null){
            Realm realm = Realm.getInstance(PlaylistActivity.this);
            realm.beginTransaction();
            List<Integer> selectedSongs = songListAdapter.getSelectedItems();
            for (int j = 0; j < selectedSongs.size(); j++) {
                playlistSelected.getSongs().add(songs.get(selectedSongs.get(j)));
            }
            realm.commitTransaction();
        }
        songListAdapter.clearSelection();
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.song_selection_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.add_to_playlist_menu_item) {
                showDialog();
                mode.finish();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }

    public void showDialog() {
        List<Playlist> playlists = MusicDataUtility.getPlayists(this);

        String[] playlistNames = new String[playlists.size()];
        for (int i = 0; i < playlistNames.length; i++) {
            playlistNames[i] = playlists.get(i).getPlaylistName();
        }
        FragmentManager manager = getSupportFragmentManager();
        PlaylistSelectDialog dialog = new PlaylistSelectDialog();
        Bundle args = new Bundle();
        args.putStringArray("playlistNames", playlistNames);
        dialog.setArguments(args);
        dialog.show(manager, "dialog");
    }

}