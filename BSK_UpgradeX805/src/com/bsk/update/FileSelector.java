package com.bsk.update;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
//import android.os.storage.StorageVolume;


import com.bsk.update.util.SwFile;
import com.bsk.update.R;

public class FileSelector extends Activity implements OnItemClickListener {

	public static final String FILE = "file";
	public static final String ROOT = "root";

	private File mCurrentDirectory;

	private LayoutInflater mInflater;

	private FileAdapter mAdapter = new FileAdapter();

	private ListView mListView;
	
	private String mRootPath;
	
	private StorageManager mStorageManager;
//	private StorageVolume[] mVolumes;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInflater = LayoutInflater.from(this);
		setContentView(R.layout.file_list);
		mListView = (ListView) findViewById(R.id.file_list);
		mListView.setAdapter(mAdapter);		
		mListView.setOnItemClickListener(this);
		mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
//		mVolumes = mStorageManager.getVolumeList();
		
		
		Intent intent = getIntent();
		String rootPath = intent.getStringExtra(ROOT);
		if(rootPath != null){
		//	mAdapter.setCurrentList(new File(rootPath).listFiles(new VolumeFilter()));
			
			
			mAdapter.setCurrentList(getRootFiles());
			
			mRootPath = rootPath;
		}else{
			setResult(RESULT_CANCELED);
		}
	}
	
	public List<String> getRootPaths() {
		List<String> pathsList = new ArrayList<String>();
		try {
            Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
            method.setAccessible(true);
            Object result = method.invoke(mStorageManager);
            if (result != null && result instanceof String[]) {
                String[] pathes = (String[]) result;
                StatFs statFs;
                for (String path : pathes) {
                    if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                        statFs = new StatFs(path);
                        if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
                            pathsList.add(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            File externalFolder = Environment.getExternalStorageDirectory();
            if (externalFolder != null) {
                pathsList.add(externalFolder.getAbsolutePath());
            }
        }
		return pathsList;
	}
	
	public File[] getRootFiles() {
		List<String> pathsList = getRootPaths();
		File[] files = new File[pathsList.size()];
		for(int i=0;i<pathsList.size();i++) {
			files[i] = new File(pathsList.get(i));
		}
		return files;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		File selectFile = (File) adapterView.getItemAtPosition(position);
		if (selectFile.isDirectory()) {
			mCurrentDirectory = selectFile;
			FileAdapter adapter = (FileAdapter) adapterView.getAdapter();
			adapter.setCurrentList(selectFile.listFiles(new ZipFilter()));
		} else if (selectFile.isFile()) {
			Intent intent = new Intent();			
			intent.putExtra(FILE, selectFile.getPath());
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		if (mCurrentDirectory == null
				|| mCurrentDirectory.getPath().equals(mRootPath)) {
			super.onBackPressed();
		} else {
			mCurrentDirectory = mCurrentDirectory.getParentFile();
			if(mCurrentDirectory.getPath().equals(mRootPath)){
//				mAdapter.setCurrentList(mCurrentDirectory.listFiles(new VolumeFilter()));
				mAdapter.setCurrentList(getRootFiles());
			}else{
				mAdapter.setCurrentList(mCurrentDirectory.listFiles(new ZipFilter()));
			}
		}
	}
	
	public boolean isVolume(File file){
		List<String> pathsList = getRootPaths();
		for(int i=0;i<pathsList.size();i++) {
			if(file.getAbsolutePath().equals(pathsList.get(i))) {
				return true;
			}
		}
		return false;
	}

	private class FileAdapter extends BaseAdapter {

		private File mFiles[];
		
		private class ViewHolder{
			ImageView image;
			TextView fileName;
			TextView fileSize;
		}

		public void setCurrentList(File directory) {
			mFiles = directory.listFiles();
			notifyDataSetChanged();
		}
		
		public void setCurrentList(File[] files){
			mFiles = files;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mFiles == null ? 0 : mFiles.length;
		}

		@Override
		public File getItem(int position) {
			File file = mFiles == null ? null : mFiles[position];
			return file;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.file_list_item, null);
				ViewHolder holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.fileName = (TextView)convertView.findViewById(R.id.file_name);
				holder.fileSize = (TextView)convertView.findViewById(R.id.file_size);
				convertView.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			File file = mFiles[position];
			if (isVolume(file)) {
				holder.fileSize.setText("");
				holder.image.setImageResource(R.drawable.litter_disk);
			} else if (file.isDirectory()) {
				holder.fileSize.setText("");
				holder.image.setImageResource(R.drawable.litter_file);
			} else if (file.isFile()) {
				holder.fileSize.setText(SwFile.byteToSize(file.length()));
				holder.image.setImageResource(R.drawable.litter_zip);
			}
			holder.fileName.setText(file.getName());
			return convertView;
		}

	}
	
	private class ZipFilter implements FileFilter{

		@Override
		public boolean accept(File pathname) {
			if(pathname != null){
				if(pathname.isDirectory()){
					return true;
				}
				String path = pathname.getPath().toLowerCase();
				return path.endsWith(".zip");
			}
			return false;
		}
		
	}
	
}
