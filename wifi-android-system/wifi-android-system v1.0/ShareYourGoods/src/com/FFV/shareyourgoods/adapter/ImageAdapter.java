package com.FFV.shareyourgoods.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.FFV.shareyourgoods.R;
import com.FFV.shareyourgoods.gallery.GalleryFlow;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * 自定义Gallery适配器，添加视图阴影
 */

public class ImageAdapter extends BaseAdapter {

	private int mGalleryItemBackground;
	private Context mContext;
	private HashMap<Integer, Bitmap> dataCache;
	public List<String> mImgList;

	private ContentResolver cr;
	public Cursor cur;

	public ImageAdapter(Context c, HashMap<Integer, Bitmap> dCache) {
		mContext = c;
		mImgList = new ArrayList<String>();
		dataCache = dCache;

		cr = mContext.getContentResolver();
		refreshImageList();

		TypedArray typedArray = mContext
				.obtainStyledAttributes(R.styleable.Gallery);

		mGalleryItemBackground = typedArray.getResourceId(
				R.styleable.Gallery_android_galleryItemBackground, 0);
		typedArray.recycle();
	}

	public void refreshImageList() {
		// TODO 自动生成的方法存根
		mImgList.clear();

		String[] proj = new String[] {
				MediaColumns.DATA,
				MediaColumns.SIZE};
		cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
				null, null, null);
		if (cur != null) {
			cur.moveToFirst();
			int count = cur.getCount();

			for (int i = 0; i < count; i++) {
				if (cur.getString(0).endsWith(".jpg")
						|| cur.getString(0).endsWith(".jpeg")
						|| cur.getString(0).endsWith(".gif"))
					if(cur.getInt(1) >= 10240)
						mImgList.add(cur.getString(0));
				cur.moveToNext();
			}
		}
	}

	public Bitmap createReflectedImages(String filePath) {
		// final int reflectionGap = 4;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap originalImage = BitmapFactory.decodeFile(filePath, options);
		options.inJustDecodeBounds = false;
		int rate = options.outHeight / 360 == 0 ? 1 : options.outHeight / 360;
		options.inSampleSize = rate;
		originalImage = BitmapFactory.decodeFile(filePath, options);
		// int width = originalImage.getWidth();
		// int height = originalImage.getHeight();
		//
		// Matrix matrix = new Matrix();
		// matrix.preScale(1, -1);
		//
		// Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
		// height / 2, width, height / 2, matrix, false);
		// Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
		// (height + height / 2), Config.ARGB_8888);
		//
		//
		// Paint deafaultPaint = new Paint();
		// Canvas canvas = new Canvas(bitmapWithReflection);
		// canvas.drawBitmap(originalImage, 0, 0, null);
		// canvas.drawRect(0, height, width, height + reflectionGap,
		// deafaultPaint);
		// canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		//
		// Paint paint = new Paint();
		// LinearGradient shader = new LinearGradient(0, originalImage
		// .getHeight(), 0, bitmapWithReflection.getHeight()
		// + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		// paint.setShader(shader);
		// paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		//
		// canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
		// + reflectionGap, paint);
		//
		// if(originalImage != null)
		// originalImage.recycle();
		//
		// if(reflectionImage != null)
		// reflectionImage.recycle();

		// return bitmapWithReflection;
		return originalImage;
	}

	@Override
	public int getCount() {
		return mImgList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imgView = new ImageView(mContext);
		Bitmap current = dataCache.get(position);
		if (current != null) {// 如果缓存中已解码该图片，则直接返回缓存中的图片
			imgView.setImageBitmap(current);
		} else {
			current = createReflectedImages(mImgList.get(position));
			imgView.setImageBitmap(current);
			dataCache.put(position, current);
		}
		imgView.setLayoutParams(new GalleryFlow.LayoutParams(160, 200));
		imgView.setBackgroundResource(mGalleryItemBackground);
		return imgView;
	}
}
