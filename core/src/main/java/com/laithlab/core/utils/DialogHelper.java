package com.laithlab.core.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.laithlab.core.R;
import com.laithlab.core.db.Playlist;
import com.laithlab.core.fragment.PlaylistSelectDialog;

import java.util.List;

public class DialogHelper {
    public static void showAddPlaylistDialog(final Context context){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.RhythmAlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_add_playlist, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.playlist_input);

        dialogBuilder.setTitle("Add a Playlist");
        dialogBuilder.setMessage("Playlist Name:");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                MusicDataUtility.createPlaylist(edt.getText().toString(), context);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                dialog.cancel();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public static void aboutDialog(final Context context){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.RhythmAlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View dialogView = inflater.inflate(R.layout.dialog_about_rhythm, null);
        dialogView.findViewById(R.id.github_project).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/laithnurie/rhythm"));
                context.startActivity(i);
            }
        });
        dialogView.findViewById(R.id.design_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://dribbble.com/shots/2183234-Music-app-research"));
                context.startActivity(i);
            }
        });
        dialogBuilder.setView(dialogView);

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public static void resetMusicDataAlert(final Activity activity) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("Reset Music Data");
        alertDialogBuilder
                .setMessage("Including Most Played, Last Played and your Personal Playlists ?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MusicDataUtility.resetMusicStats(activity);
                SharedPreferences sharedPreferences = activity
                        .getSharedPreferences("com.laithlab.rhythm", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(activity.getString(R.string.first_time_pref_key), true);
                editor.apply();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void addSongToPlaylist(AppCompatActivity activity){
        List<Playlist> playlists = MusicDataUtility.getPlayists(activity);

        if(playlists.size() == 0){
            showAddPlaylistDialog(activity);
        } else {
            String[] playlistNames = new String[playlists.size()];
            for (int i = 0; i < playlistNames.length; i++) {
                playlistNames[i] = playlists.get(i).getPlaylistName();
            }
            FragmentManager manager = activity.getSupportFragmentManager();
            PlaylistSelectDialog dialog = new PlaylistSelectDialog();
            Bundle args = new Bundle();
            args.putStringArray("playlistNames", playlistNames);
            dialog.setArguments(args);
            dialog.show(manager, "dialog");
        }
    }
}
