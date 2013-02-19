package edu.umbc.CMSC628.galleria;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	
	String extensions[] = {"jpg", "png"};
	private Context context;
	public ImageAdapter(Context c) {
		context=c;
	}

	@Override
	public int getCount() {
		return FileUtils.listFiles(context.getFilesDir(), 
				extensions, true).size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(260, 260));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }
        
        Collection<File> photoList = FileUtils.listFiles(context.getFilesDir(), 
        		extensions, true);
        File img= photoList.toArray(new File[photoList.size()])[position];
        Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
        imageView.setImageBitmap(myBitmap);
        return imageView;
	}

}
