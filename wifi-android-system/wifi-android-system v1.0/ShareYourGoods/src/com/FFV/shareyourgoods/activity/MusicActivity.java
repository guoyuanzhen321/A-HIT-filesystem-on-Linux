package com.FFV.shareyourgoods.activity;

import com.FFV.shareyourgoods.R;
import com.FFV.shareyourgoods.adapter.MusicAdapter;
import com.FFV.shareyourgoods.service.MusicService;
import com.FFV.shareyourgoods.util.MsgConfig;
import com.FFV.shareyourgoods.util.Music;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicActivity extends BaseActivity {
	public static ImageButton preBtn, playBtn, nextBtn;
	public static ListView mListView;// 我的音乐选项卡View

	public static int songTime = 0;
	public static SeekBar seekBar;
	public static TextView mName;
	public static TextView mName2;
	public static ImageView mAlbum;
	public static ImageView mAlbum2;
	public static TextView mAlbum3;
	public static TextView mSinger;
	public static TextView mSize;
	public static TextView mPath;
	public static ImageView playMode;
	public static TextView playTime;
	public static MusicAdapter mAdapter;
	public static TabHost mTabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_play);		

		init();
		initTab();
 
		// 设置音乐列表MusicAdapter
		mAdapter = new MusicAdapter(this);
		mListView.setAdapter(mAdapter);// 加入ListView

		initEvent();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			String path = bundle.getString("path");
			for (int i = mAdapter.musicList.size() - 1; i >= 0; i--) {
				Music m = mAdapter.musicList.get(i);
				if (m.getMusicPath().equals(path)) {
					Intent play = new Intent(MusicActivity.this,
							MusicService.class);
					play.putExtra("control", "listClick");
					play.putExtra("musicId", i);
					startService(play);
					break;
				}
			}
		}
	}

	public void init() {
		// 得到我的歌曲列表 ,以及三个播放按钮
		mListView = (ListView) findViewById(R.id.mlistView);
		preBtn = (ImageButton) findViewById(R.id.pre_button);
		playBtn = (ImageButton) findViewById(R.id.play_button);
		nextBtn = (ImageButton) findViewById(R.id.next_button);

		// 进度条
		seekBar = (SeekBar) findViewById(R.id.mseekBar);
		// 时间TextView
		playTime = (TextView) findViewById(R.id.play_time);
		// 歌手名和专辑图像
		mName = (TextView) findViewById(R.id.mText);
		mName2 = (TextView) findViewById(R.id.musicname);
		mAlbum = (ImageView) findViewById(R.id.album_imageView);
		mAlbum2 = (ImageView) findViewById(R.id.imageView1);
		mAlbum3 = (TextView) findViewById(R.id.musicalbum);
		mSinger = (TextView) findViewById(R.id.musicsinger);
		mSize = (TextView) findViewById(R.id.musicsize);
		mPath = (TextView) findViewById(R.id.musicpath);
		// 播放模式
		playMode = (ImageView) findViewById(R.id.playMode);

	}

	public void initTab() {
		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();

		TextView mMusic = new TextView(this);
		TextView mDetails = new TextView(this);

		mMusic.setText("我的音乐");
		mMusic.setTextColor(getResources().getColor(R.color.yellow));
		mMusic.setTextSize(20);
		mDetails.setText("歌曲信息");
		mDetails.setTextColor(getResources().getColor(R.color.yellow));
		mDetails.setTextSize(20);
		mMusic.setGravity(Gravity.CENTER);
		mDetails.setGravity(Gravity.CENTER);

		mTabHost.addTab(mTabHost.newTabSpec("mListView").setIndicator(mMusic)
				.setContent(R.id.musicTab));

		mTabHost.addTab(mTabHost.newTabSpec("mDetailsView")
				.setIndicator(mDetails).setContent(R.id.lrcTab));

	}

	public void initEvent() {
		preBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mAdapter.musicList != null && mAdapter.musicList.size() > 0) {
					Intent pre = new Intent(MusicActivity.this,
							MusicService.class);
					pre.putExtra("control", "previous");
					startService(pre);
				}
			}
		});

		playBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mAdapter.musicList != null && mAdapter.musicList.size() > 0) {
					Intent play = new Intent(MusicActivity.this,
							MusicService.class);
					play.putExtra("control", "play");
					startService(play);
				}
			}
		});

		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mAdapter.musicList != null && mAdapter.musicList.size() > 0) {
					Intent next = new Intent(MusicActivity.this,
							MusicService.class);
					next.putExtra("control", "next");
					startService(next);
				}
			}
		});

		// 播放模式事件
		playMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent mode = new Intent(MusicActivity.this, MusicService.class);
				mode.putExtra("control", "playMode");
				startService(mode);
			}
		});
		// 当长时间点击播放模式时候显示当前播放模式
		playMode.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Intent longMode = new Intent(MusicActivity.this,
						MusicService.class);
				longMode.putExtra("control", "longPlayMode");
				startService(longMode);
				return false;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (mAdapter.musicList != null && mAdapter.musicList.size() > 0) {
					Intent play = new Intent(MusicActivity.this,
							MusicService.class);
					play.putExtra("control", "listClick");
					play.putExtra("musicId", arg2);
					startService(play);
				}
			}
		});

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (MusicService.mplayer != null) {
					if (fromUser) {
						MusicService.mplayer.seekTo(progress);
					}
					playTime.setText(mAdapter.toTime(progress));
				} else {
					seekBar.setMax(0);
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case 0:
			openOptionsDialog();
			break;
		case 1:
			ifexitOptionsDialog();
			break;
		}
		return true;
	}

	private void openOptionsDialog() {
		new AlertDialog.Builder(this)
				.setTitle("About")
				.setMessage(
						"Written by FFV team.\nAny questions, please email:\nsteve_guo@163.com")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).show();
	}

	private void ifexitOptionsDialog() {
		new AlertDialog.Builder(this).setTitle("Exit")
				.setMessage("Do you really want to exit?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).show();
	}

	@Override
	public void sendFile() {
		String[] path = { mAdapter.musicList.get(MusicService.playing_id)
				.getMusicPath() };
		netConnHelper.sendFileTransIRQ(connector, path);
	}

	public Runnable update = new Runnable() {
		@Override
		public void run() {
			mAdapter.notifyDataSetChanged();
		}
	};
	
	@Override
	public void processMsg(Message msg) {
		// TODO 自动生成的方法存根
		switch (msg.what) {
		case MsgConfig.MSG_FILE_MP3_RCV:
			String path = (String) msg.obj;		
			mAdapter.refreshMusicList();	
			Handler handler = new Handler();
			handler.post(update);
			
			for (int i = mAdapter.musicList.size() - 1; i >= 0; i--) {
				Music m = mAdapter.musicList.get(i);
				if (m.getMusicPath().equals(path)) {
					Intent play = new Intent(MusicActivity.this,
							MusicService.class);
					play.putExtra("control", "listClick");
					play.putExtra("musicId", i);
					startService(play);
					break;
				}
			}
			break;

		case MsgConfig.MSG_FILE_IMG_RCV:
			Intent intent2 = new Intent();
			intent2.setClass(this, ImgActivity.class);
			startActivity(intent2);
			this.finish();
			break;
			
		case MsgConfig.MSG_FILE_DIY_RCV:
			Intent intent3 = new Intent();
			intent3.setClass(this, FileActivity.class);		
			startActivity(intent3);
			this.finish();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		// 关于
		if (item.getItemId() == Menu.FIRST + 3) {
			Intent intent2 = new Intent();
			intent2.setClass(this, ImgActivity.class);
			startActivity(intent2);
			this.finish();
		} else if (item.getItemId() == Menu.FIRST + 5) {
			Intent intent2 = new Intent();
			intent2.setClass(this, FileActivity.class);
			startActivity(intent2);
			this.finish();
		} else if (item.getItemId() == Menu.FIRST + 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(R.drawable.dialog_information).setTitle("关于")
					.setMessage(R.string.about)// string.xml中定义的about
					.setPositiveButton("确定", null).show();
		} else if (item.getItemId() == Menu.FIRST + 2) {// 退出
			this.onClickExit();
		}
		return true;
	}
}
