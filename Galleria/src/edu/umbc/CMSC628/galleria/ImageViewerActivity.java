package edu.umbc.CMSC628.galleria;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageViewerActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	CustomImageAdapter myImageAdapter;
	
	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_viewer);

		// Create an adapter that when requested, will return a fragment
		// representing an object in
		// the collection.
		//
		// ViewPager and its adapters use support library fragments, so we must
		// use
		// getSupportFragmentManager.
		myImageAdapter = new CustomImageAdapter(
				getSupportFragmentManager(), this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(myImageAdapter);
	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class CustomImageAdapter extends
			FragmentStatePagerAdapter {

		Context context;
		public CustomImageAdapter(FragmentManager fm, Context c) {
			super(fm);
			context = c;
		}

		@Override
		public Fragment getItem(int i) {
			ImageFragment fragment = new ImageFragment();
			Bundle args = new Bundle();
			args.putInt(Constants.SELECTED_IMAGE_INDEX, i); 
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return FileUtils.listFiles(context.getFilesDir(), 
	        		Constants.extensions, true).size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Image " + (position + 1);
		}
	}

	public static class ImageFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment, container, false);
			Bundle args = getArguments();
			
			Context context = getActivity();
			Collection<File> photoList = FileUtils.listFiles(context.getFilesDir(), 
	        		Constants.extensions, true);
			
	        File img= photoList.toArray(new File[photoList.size()])
	        		[args.getInt(Constants.SELECTED_IMAGE_INDEX)];
	        
			Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
			((ImageView) rootView.findViewById(R.id.imageView1))
					.setImageBitmap(myBitmap);
			return rootView;
		}
	}
}
