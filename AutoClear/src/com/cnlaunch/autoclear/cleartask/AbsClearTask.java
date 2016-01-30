package com.cnlaunch.autoclear.cleartask;

import java.util.List;

import com.cnlaunch.autoclear.data.ClearData;

public abstract class AbsClearTask implements Runnable
{
	protected List<ClearData> mClearDatas;

	protected AbsClearTask(List<ClearData> clearDatas)
	{
		mClearDatas = clearDatas;
	}

	public void run()
	{
		checkAndRun();
	}

	public abstract void checkAndRun();
}
