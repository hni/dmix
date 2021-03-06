package com.namelessdev.mpdroid;

import java.util.ArrayList;
import java.util.Collection;

import org.a0z.mpd.Directory;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FSActivity extends BrowseActivity {
	private Directory currentDirectory = null;

	public FSActivity() {
		super(R.string.addDirectory, R.string.addedDirectoryToPlaylist, MPD.MPD_SEARCH_FILENAME);
		items = new ArrayList<String>();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.files);

		UpdateList();
		registerForContextMenu(getListView());
	}

	@Override
	protected void Add(String item) {
		try {
			MPDApplication app = (MPDApplication) getApplication();
			Directory ToAdd = currentDirectory.getDirectory(item);
			if (ToAdd != null) {
				// Valid directory
				app.oMPDAsyncHelper.oMPD.getPlaylist().add(ToAdd);
				MainMenuActivity.notifyUser(String.format(getResources().getString(R.string.addedDirectoryToPlaylist), item), FSActivity.this);
			} else {
				Music music = currentDirectory.getFileByTitle(item);
				if (music != null) {
					app.oMPDAsyncHelper.oMPD.getPlaylist().add(music);
					MainMenuActivity.notifyUser(getResources().getString(R.string.songAdded, item), FSActivity.this);
				}
			}
		} catch (MPDServerException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void asyncUpdate() {
		MPDApplication app = (MPDApplication) getApplication();
		if (this.getIntent().getStringExtra("directory") != null) {
			currentDirectory = app.oMPDAsyncHelper.oMPD.getRootDirectory()
					.makeDirectory((String) this.getIntent().getStringExtra("directory"));
			setTitle((String) getIntent().getStringExtra("directory"));
			findViewById(R.id.header).setVisibility(View.VISIBLE);
			TextView title = (TextView) findViewById(R.id.headerText);
			title.setText(this.getTitle());
			ImageView icon = (ImageView) findViewById(R.id.headerIcon);
			icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_playlists_selected));
		} else {
			currentDirectory = app.oMPDAsyncHelper.oMPD.getRootDirectory();
		}

		try {
			currentDirectory.refreshData();
		} catch (MPDServerException e) {
			e.printStackTrace();
			this.setTitle(e.getMessage());
		}

		Collection<Directory> directories = currentDirectory.getDirectories();
		for (Directory child : directories) {
			items.add(child.getName());
		}

		Collection<Music> musics = currentDirectory.getFiles();
		for (Music music : musics) {
			items.add(music.getTitle());
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// click on a file
		if (position > currentDirectory.getDirectories().size() - 1 || currentDirectory.getDirectories().size() == 0) {

			Music music = (Music) currentDirectory.getFiles().toArray()[position - currentDirectory.getDirectories().size()];

			try {
				MPDApplication app = (MPDApplication) getApplication();

				int songId = -1;
				app.oMPDAsyncHelper.oMPD.getPlaylist().add(music);
				if (songId > -1) {
					app.oMPDAsyncHelper.oMPD.skipTo(songId);
				}

			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// click on a directory
			// open the same sub activity, it would be better to reuse the
			// same instance

			Intent intent = new Intent(this, FSActivity.class);
			String dir;

			dir = ((Directory) currentDirectory.getDirectories().toArray()[position]).getFullpath();
			if (dir != null) {
				intent.putExtra("directory", dir);
				startActivityForResult(intent, -1);
			}
		}

	}

}
