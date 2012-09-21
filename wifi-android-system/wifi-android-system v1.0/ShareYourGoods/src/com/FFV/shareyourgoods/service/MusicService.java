package com.FFV.shareyourgoods.service;

import java.text.NumberFormat;

import com.FFV.shareyourgoods.R;
import com.FFV.shareyourgoods.activity.MusicActivity;
import com.FFV.shareyourgoods.util.Music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class MusicService extends Service implements OnCompletionListener {

	public static MediaPlayer mplayer;
	public static int playing_id;// 正在播放歌曲id
	public static boolean isPlaying;// 是否正在播放

	// 当前播放模式,0为列表循环，1为随机播放，2为单曲循环，3为顺序播放
	public int currentPlayMode = 0;
	public String[] modeString = { "列表循环", "随机播放", "单曲循环", "顺序播放" };
	public int[] modeImage = { R.drawable.mode_list_loop,
			R.drawable.mode_random, R.drawable.mode_single_loop,
			R.drawable.mode_order };

	Handler mHandler = new Handler();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		initMediaPlayer(0);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

		String type = intent.getExtras().getString("control");

		if (type.equals("play")) {
			play();
		} else if (type.equals("previous")) {
			previous();
		} else if (type.equals("next")) {
			next();
		} else if (type.equals("listClick")) {
			playing_id = intent.getExtras().getInt("musicId");
			listClick();
		} else if (type.equals("playMode")) {
			dealPlayMode();
		} else if (type.equals("longPlayMode")) {
			Toast.makeText(this, modeString[currentPlayMode],
					Toast.LENGTH_SHORT).show();
		}
	}

	// 初始化MediaPlayer
	public void initMediaPlayer(int id) {
		playing_id = id;
		
		if (MusicActivity.mAdapter.musicList != null
				&& MusicActivity.mAdapter.musicList.size() > 1) {
			// 得到歌曲id
			String path = MusicActivity.mAdapter.musicList.get(playing_id)
					.getMusicPath();
			Uri mp3Path = Uri.parse(path);

			// 不为空先释放原有资源
			if (mplayer != null) {
				mplayer.stop();
				mplayer.reset();
				mplayer = null;
			}

			mplayer = MediaPlayer.create(this, mp3Path);
			
			mplayer.setOnCompletionListener(this);
		}
	}

	// 设置播放歌曲的信息，当点击播放歌曲时在右上部显示播放信息，时间歌曲名等
	public void setMusicInfo() {
		Music m = MusicActivity.mAdapter.musicList.get(playing_id);

		// 设置显示时间
		MusicActivity.songTime = m.getMusicTime();
		// 设置进度条
		MusicActivity.seekBar.setMax(MusicActivity.songTime);
		// 设置歌曲名称
		MusicActivity.mName.setText(MusicActivity.mAdapter.toMp3(m.getMusicName()));
		MusicActivity.mName2.setText("文件名：" + MusicActivity.mAdapter.toMp3(m.getMusicName()));
		MusicActivity.mAlbum3.setText("专辑名：" + m.getMusicAlubm());
		MusicActivity.mSinger.setText("歌手名：" + m.getMusicSinger());
		NumberFormat f = NumberFormat.getInstance();
		f.setMaximumFractionDigits(2);
		MusicActivity.mSize.setText("文件大小：" + f.format((float) m.getMusicSize() / (1024 * 1024)) + "MB");
		MusicActivity.mPath.setText("文件路径：" + m.getMusicPath());
		// 设置专辑图片如果有的话
		String albumPicturePath = MusicActivity.mAdapter.getAlbumPicture(m
				.getMusicId());
		
		if (albumPicturePath != null) {
			MusicActivity.mAlbum.setImageURI(Uri.parse(albumPicturePath));
			MusicActivity.mAlbum2.setImageURI(Uri.parse(albumPicturePath));
		} else {
			MusicActivity.mAlbum.setImageResource(R.drawable.album);
			MusicActivity.mAlbum2.setImageResource(R.drawable.album);
		}
	}

	// 播放
	public void play() {

		if (mplayer != null) {
			// 如果正在播放则变为暂停
			if (mplayer.isPlaying()) {
				MusicActivity.playBtn
						.setImageResource(R.drawable.player_play);
				mplayer.pause();
			} else {
				setMusicInfo();
				MusicActivity.playBtn
						.setImageResource(R.drawable.player_pause);
				mplayer.start();
			}

			isPlaying = true;
			Thread thread = new Thread() {
				@Override
				public void run() {
					while (isPlaying) {
						// 通过线程设置进度条显示时间，睡眠一秒更新
						int curTime = mplayer.getCurrentPosition();
						MusicActivity.seekBar.setProgress(curTime);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			thread.start();
		}

	}

	// 上一首
	public void previous() {
		// 随机播放的话
		isPlaying = false;
		if (currentPlayMode == 1) {
			// 随机生成播放ID
			int size = MusicActivity.mAdapter.musicList.size();
			playing_id = (int) (size * Math.random());
			this.initMediaPlayer(playing_id);
		} else {
			if (playing_id == 0) {
				playing_id = MusicActivity.mAdapter.musicList.size() - 1;
				this.initMediaPlayer(playing_id);
			} else {
				this.initMediaPlayer(--playing_id);
			}
		}

		play();
	}

	// 下一首
	public void next() {
		isPlaying = false;
		if (currentPlayMode == 1) {
			// 随机生成播放ID
			int size = MusicActivity.mAdapter.musicList.size();
			playing_id = (int) (size * Math.random());
			this.initMediaPlayer(playing_id);
		} else {
			if (playing_id == MusicActivity.mAdapter.musicList.size() - 1) {
				playing_id = 0;
				this.initMediaPlayer(playing_id);
			} else {
				this.initMediaPlayer(++playing_id);
			}
		}

		play();
	}

	// 点击歌曲
	public void listClick() {
		isPlaying = false;
		this.initMediaPlayer(playing_id);
		play();
	}

	// 点击播放模式
	public void dealPlayMode() {
		currentPlayMode++;
		currentPlayMode %= 4;
		Toast.makeText(this, modeString[currentPlayMode], Toast.LENGTH_SHORT)
				.show();
		MusicActivity.playMode.setImageResource(modeImage[currentPlayMode]);
	}

	// 播放完后的处理事件
	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub

		// 播放完根据播放模式决定下一首
		if (currentPlayMode == 0) {// 列表循环
			if (MusicActivity.mAdapter.musicList != null
					&& MusicActivity.mAdapter.musicList.size() > 0) {
				next();
			}
		} else if (currentPlayMode == 1) {// 随机播放
			// 随机生成播放ID
			if (MusicActivity.mAdapter.musicList != null
					&& MusicActivity.mAdapter.musicList.size() > 0) {
				isPlaying = false;
				int size = MusicActivity.mAdapter.musicList.size();
				playing_id = (int) (size * Math.random());
				this.initMediaPlayer(playing_id);
				play();
			}
		} else if (currentPlayMode == 2) {// 单曲循环
			if (MusicActivity.mAdapter.musicList != null
					&& MusicActivity.mAdapter.musicList.size() > 0) {
				isPlaying = false;

				this.initMediaPlayer(playing_id);
				play();
			}
		} else if (currentPlayMode == 3) { // 顺序播放
			if (MusicActivity.mAdapter.musicList != null
					&& MusicActivity.mAdapter.musicList.size() > 0) {
				isPlaying = false;

				if (playing_id != MusicActivity.mAdapter.musicList.size() - 1) {
					next();
				} else {
					// 设置显示时间
					MusicActivity.songTime = 0;
					// 设置进度条
					MusicActivity.seekBar.setMax(0);
					// 设置歌曲名称
					MusicActivity.mName.setText("音乐你的移动生活！");
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}