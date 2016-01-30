package com.cnlaunch.autoclear.cleartask;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.cnlaunch.autoclear.data.ClearData;

public class DirClearTask extends AbsClearTask
{
	private static final String TAG = "DirClearTask";

	public DirClearTask(List<ClearData> clearDatas)
	{
		super(clearDatas);
	}

	@Override
	public void checkAndRun()
	{
		if (mClearDatas != null)
		{
			Iterator<ClearData> it = mClearDatas.iterator();
			while (it.hasNext())
			{
				ClearData cd = it.next();
				if (cd.getType().equals(ClearData.TYPE_DIR))
				{
					File file = new File(cd.getPath());
					if (file.exists() && file.isDirectory())
					{
						innerDelete(cd.getPath());
						Log.d(TAG, "delete dir:" + cd.getPath());
					}
					it.remove();
				}
			}
		}
	}

	private void innerDelete(String path)
	{
		File f = new File(path);
		if (f.isDirectory())
		{
			String[] list = f.list();
			for (int i = 0; i < list.length; i++)
			{
				innerDelete(path + "/" + list[i]);
			}
		}
		f.delete();
	}
}
