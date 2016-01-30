package com.example.blacklist;

import java.util.HashMap;
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
public class BlackListAdapter extends BaseAdapter {

	private HashMap<Integer, String> blackListHashmap = null;
	LayoutInflater infater = null;
	private int mChildCount = 0;

	public BlackListAdapter(Context context, HashMap<Integer, String> apps) {
		// ��ʵ�ʹ����У�����д�õĲ����ļ����������������ǵ�������ʱ���������ڴ������Զ���ؼ��������Ҫ�õ�LayoutInflater
		infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		blackListHashmap = apps;

	}

	// ����Ӧ�õĸ���
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		// System.out.println("size" + mlistAppInfo.size());
		return blackListHashmap.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return blackListHashmap.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	// ������ÿ��ʾ����һ��itemʱ���ͻ���ø÷���,getView()������������
	// ��һ��������ʾ��item��Adapter�е�λ�ã�
	// �ڶ���������item��View�����ǻ���listʱ��Ҫ��ʾ�ڽ����ϵ�item�������item����ʾ������ʧ��
	// ��ʱandroid�Ὣ��ʧ��item���أ���Ϊ��view��Ҳ����˵��ʱ��view��Ϊnull���������������ڼ���xml��ͼ��
	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		// System.out.println("getView at " + position);

		View view = null;
		// ��ȡ�����ļ��Ŀؼ�
		ViewHolder2 holder = null;
		// �����ͼΪ�գ��Ͱ���ͼ���ؽ������浽view��
		if (convertview == null || convertview.getTag() == null) {
			// view=LinearLayout (id=830040609408)
			view = infater.inflate(R.layout.browse_black_item, null);
			// ����ͼ��view=LinearLayout����ViewHolder����ΪҪ��ȡ��ͼ��Ŀؼ�
			holder = new ViewHolder2(view);
			view.setTag(holder);
			// System.out.println(view.setTag(holder));
		} else {
			view = convertview;
			holder = (ViewHolder2) convertview.getTag();
		}
		// ����ͼset��Ӧ������
		String blackInfo =(String) getItem(position);
		holder.tvPId2.setText("������"+blackInfo);
		/*holder.tvAppLabel.setText(appInfo.getAppLabel());
		holder.tvPkgName.setText(appInfo.getPkgName());
		holder.tvProcessId.setText(appInfo.getPid() + "");
		holder.tvProcessName.setText(appInfo.getProcessName());*/
		// ��󷵻�һ��view��һ������������ͼ��view�����ظ���������
		return view;
	}

	class ViewHolder2 {
		TextView tvPId2;
		public ViewHolder2(View view) {
			this.tvPId2 = (TextView) view.findViewById(R.id.tvPId2);
		}
	}
}