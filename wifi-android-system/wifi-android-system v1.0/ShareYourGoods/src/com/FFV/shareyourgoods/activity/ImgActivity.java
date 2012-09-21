package com.FFV.shareyourgoods.activity;

import java.util.HashMap;

import com.FFV.shareyourgoods.R;
import com.FFV.shareyourgoods.adapter.ImageAdapter;
import com.FFV.shareyourgoods.util.MsgConfig;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;

public class ImgActivity extends BaseActivity implements
		AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {

	private HashMap<Integer, Bitmap> mDataCache;
	private ImageSwitcher mSwitcher;
	private Gallery mGallery;
	private ImageAdapter mImageAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image);
		
		mDataCache = new HashMap<Integer, Bitmap>();

		mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
		mSwitcher.setFactory(this);
		mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.slide_in_left));
		mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.slide_out_right));

		mGallery = (Gallery) findViewById(R.id.mygallery);
		mImageAdapter = new ImageAdapter(this, mDataCache);
		mGallery.setAdapter(mImageAdapter);
		mGallery.setOnItemSelectedListener(this);
		mGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
			}
		});

	}

	final Runnable update = new Runnable() {
		@Override
		public void run() {
			mImageAdapter.notifyDataSetChanged(); // 不能直接在AsyncTask中调用,因为不是线程安全的
		}
	};
	
	@Override
	public void processMsg(Message msg) {
		// TODO 自动生成的方法存根
		switch (msg.what) {
		case MsgConfig.MSG_FILE_IMG_RCV:
			//String path = (String) msg.obj;
			mImageAdapter.refreshImageList();
			freeBitmapFromIndex(0);
			Handler handler = new Handler();
			handler.post(update);
			//Toast.makeText(this, "请划动相册以更新图片", Toast.LENGTH_LONG).show();
			break;
			
		case MsgConfig.MSG_FILE_MP3_RCV:
			String path1 = (String) msg.obj;
			Intent intent2 = new Intent();
			intent2.setClass(this, MusicActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("path", path1);
			intent2.putExtras(bundle);
			
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
	public View makeView() {
		// TODO 自动生成的方法存根
		ImageView i = new ImageView(this);
		i.setBackgroundColor(0xFF000000);
		i.setScaleType(ImageView.ScaleType.FIT_XY);
		i.setLayoutParams(new ImageSwitcher.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT));
		return i;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO 自动生成的方法存根
		String photoURL = mImageAdapter.mImgList.get(position);
		Log.i("A", String.valueOf(position));
		TextView TxtView = (TextView) findViewById(R.id.filepath);
		TxtView.setText(photoURL);
		mSwitcher.setImageURI(Uri.parse(photoURL));
		releaseBitmap();
	}

	@Override
	public void sendFile() {
		TextView tv = (TextView) findViewById(R.id.filepath);
		String[] path = { tv.getText().toString() };
		netConnHelper.sendFileTransIRQ(connector, path);
	}

	private void releaseBitmap() {
		int start = mGallery.getFirstVisiblePosition() - 2;
		int end = mGallery.getLastVisiblePosition() + 2;
		// 释放position<start之外的bitmap资源
		Bitmap delBitmap;
		for (int del = 0; del < start; del++) {
			delBitmap = mDataCache.get(del);
			if (delBitmap != null) {
				mDataCache.remove(del);
				delBitmap.recycle();
			}
		}
		freeBitmapFromIndex(end);
	}

	private void freeBitmapFromIndex(int end) {
		// 释放之外的bitmap资源
		Bitmap delBitmap;
		for (int del = end + 1; del < mDataCache.size(); del++) {
			delBitmap = mDataCache.get(del);
			if (delBitmap != null) {
				mDataCache.remove(del);
				delBitmap.recycle();
			}
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		// 关于
		if (item.getItemId() == Menu.FIRST + 4) {
			Intent intent2 = new Intent();
			intent2.setClass(this, MusicActivity.class);
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

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO 自动生成的方法存根
		
	}
	
}
