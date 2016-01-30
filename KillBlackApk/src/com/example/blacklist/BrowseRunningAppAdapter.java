package com.example.blacklist;

import java.util.List;

import com.example.blacklist.R;

import android.app.ActivityManager;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//�Զ����������࣬�ṩ��listView���Զ���view
public class BrowseRunningAppAdapter extends BaseAdapter {

	private List<RunningAppInfo> mlistAppInfo = null;
	LayoutInflater infater = null;
	private int mChildCount = 0;
	public BrowseRunningAppAdapter(Context context, List<RunningAppInfo> apps) {
		// ��ʵ�ʹ����У�����д�õĲ����ļ����������������ǵ�������ʱ���������ڴ������Զ���ؼ��������Ҫ�õ�LayoutInflater
		infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mlistAppInfo = apps;

	}

	// ����Ӧ�õĸ���
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		//System.out.println("size" + mlistAppInfo.size());		
		return mlistAppInfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mlistAppInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	// position�Ǵ������listView�ϵ���һ��
	// ������ÿ��ʾ����һ��itemʱ���ͻ���ø÷���,getView()������������
	// ��һ��������ʾ��item��Adapter�е�λ�ã�
	// �ڶ���������item��View�����ǻ���listʱ��Ҫ��ʾ�ڽ����ϵ�item�������item����ʾ������ʧ��
	// ��ʱandroid�Ὣ��ʧ��item���أ���Ϊ��view��Ҳ����˵��ʱ��view��Ϊnull���������������ڼ���xml��ͼ��
	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		//System.out.println("getView at " + position);
				
		View view = null;
		// ��ȡ�����ļ��Ŀؼ�
		ViewHolder holder = null;
		// �����ͼΪ�գ��Ͱ���ͼ���ؽ������浽view��
		if (convertview == null || convertview.getTag() == null) {
			// view=LinearLayout (id=830040609408)
			view = infater.inflate(R.layout.browse_app_item, null);
			// ����ͼ��view=LinearLayout����ViewHolder����ΪҪ��ȡ��ͼ��Ŀؼ�
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertview;
			holder = (ViewHolder) convertview.getTag();
		}
		// ����ͼset��Ӧ������
		RunningAppInfo appInfo = (RunningAppInfo) getItem(position);
		holder.appIcon.setImageDrawable(appInfo.getAppIcon());
		holder.tvAppLabel.setText(appInfo.getAppLabel());
		holder.tvPkgName.setText(appInfo.getPkgName());
		holder.tvProcessId.setText(appInfo.getPid() + "");
		//holder.tvProcessName.setText(appInfo.getProcessName());
		// ��󷵻�һ��view��һ������������ͼ��view�����ظ���������
		return view;
	}

	class ViewHolder {
		ImageView appIcon;
		TextView tvAppLabel;
		TextView tvPkgName;
		TextView tvProcessId;
		//TextView tvProcessName;

		public ViewHolder(View view) {
			this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
			this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
			this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
			this.tvProcessId = (TextView) view.findViewById(R.id.tvProcessId);
			//this.tvProcessName = (TextView) view.findViewById(R.id.tvProcessName);
		}
	}
}