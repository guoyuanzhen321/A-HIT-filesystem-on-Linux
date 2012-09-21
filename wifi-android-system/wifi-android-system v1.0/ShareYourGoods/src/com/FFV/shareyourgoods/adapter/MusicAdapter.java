package com.FFV.shareyourgoods.adapter;

import java.util.ArrayList;
import java.util.List;

import com.FFV.shareyourgoods.R;
import com.FFV.shareyourgoods.util.Music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter {

	// 用来获得ContentProvider(共享数据库)
	public ContentResolver cr;
	// 用来装查询到的音乐文件数据
	public Cursor cur;
	// 歌曲信息列表
	public List<Music> musicList;
	// 歌曲详细信息属性类
	public Music music;
	public Context context;

	public MusicAdapter(Context context) {
		this.context = context;
		musicList = new ArrayList<Music>();
		cr = context.getContentResolver();
		refreshMusicList();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return musicList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return musicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		/*
		 * LayoutInflater的作用类似于 findViewById(),不同点是LayoutInflater是用来
		 * 找layout文件夹下的xml布局文件，并且实例化！而 findViewById()是找具体 某一个xml下的具体 widget控件
		 */
		
		LayoutInflater mlistLayout = LayoutInflater.from(context);
		View mlistView = mlistLayout.inflate(R.layout.music_list, null);
		TextView songNum = (TextView) mlistView.findViewById(R.id.songNum);
		TextView songName = (TextView) mlistView.findViewById(R.id.songName);
		TextView songTime = (TextView) mlistView.findViewById(R.id.songTime);
		int mNUm = position + 1;
		songNum.setText(mNUm + ".");
		songName.setText(toMp3(musicList.get(position).getMusicName()));
		songTime.setText(toTime(musicList.get(position).getMusicTime()));
		return mlistView;
	}

	// 处理歌曲名字，即去掉后缀
	public String toMp3(String name) {
		int search = name.indexOf(".mp3");
		String newName = name.substring(0, search);
		return newName;

	}

	// 处理歌曲时间，以xx:xx格式显示
	public String toTime(int time) {
		// Log.e("time", time+"");
		// 时间是毫秒的格式，例如251432，在/1000以后得到一共多少秒
		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	// 专辑图片显示,如果有歌曲图片，才会返回，回类型是String 类型的图片地址uri，否则为null
	// http://topic.csdn.net/u/20120522/20/39907d0b-a742-4d34-b1c8-6fc28ba33e7e.html网址上有讲
	public String getAlbumPicture(int trackId) {
		String mUriTrack = "content://media/external/audio/media/#";
		String[] projection = new String[] { "album_id" };
		String selection = "_id = ?";
		String[] selectionArgs = new String[] { Integer.toString(trackId) };

		Cursor cur = context.getContentResolver().query(Uri.parse(mUriTrack),
				projection, selection, selectionArgs, null);
		int album_id = 0;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_id = cur.getInt(0);
		}
		cur.close();
		cur = null;

		if (album_id < 0) {
			return null;
		}
		String mUriAlbums = "content://media/external/audio/albums";
		projection = new String[] { "album_art" };
		cur = context.getContentResolver().query(
				Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
				projection, null, null, null);

		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;

		return album_art;
	}
	
	public void refreshMusicList(){
		musicList.clear();
		
		String[] projection = new String[] {
				MediaColumns.DISPLAY_NAME,// 歌曲名
				AudioColumns.ALBUM, // 专辑名
				AudioColumns.ARTIST, // 歌手名
				AudioColumns.DURATION, // 时间
				MediaColumns.SIZE, // 歌曲大小
				BaseColumns._ID, // ID
				MediaColumns.DATA // 音乐文件路径
		};

		// 查询得到所有音乐信息
		cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
				null, null, null);

		if (cur != null) {
			// 移动游标到第一个
			cur.moveToFirst();

			int musicCount = cur.getCount();

			for (int i = 0; i < musicCount; i++) {
				if (cur.getString(0).endsWith(".mp3")) {
					music = new Music();
					music.setMusicName(cur.getString(0));
					music.setMusicAlubm(cur.getString(1));
					music.setMusicSinger(cur.getString(2));
					music.setMusicTime(cur.getInt(3));
					music.setMusicSize(cur.getInt(4));
					music.setMusicId(cur.getInt(5));
					music.setMusicPath(cur.getString(6));
					// 添加到列表
					musicList.add(music);
				}
				cur.moveToNext();
			}
		}
	}

}

