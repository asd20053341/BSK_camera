package com.cnlaunch.autoclear.cleartask;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.cnlaunch.autoclear.data.ClearData;

public class FileClearTask extends AbsClearTask
{
	private static final String TAG = "FileClearTask";

	public FileClearTask(List<ClearData> clearDatas)
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
				if (cd.getType().equals(ClearData.TYPE_FILE))
				{
					File file = new File(cd.getPath());
					if (file.exists())
					{
						file.delete();
						Log.d(TAG, "delete file:" + cd.getPath());
					}
					it.remove();
				}
			}
		}
	}
}
