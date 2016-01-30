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

//自定义适配器类，提供给listView的自定义view
public class BlackListAdapter extends BaseAdapter {

	private HashMap<Integer, String> blackListHashmap = null;
	LayoutInflater infater = null;
	private int mChildCount = 0;

	public BlackListAdapter(Context context, HashMap<Integer, String> apps) {
		// 在实际工作中，事先写好的布局文件往往不能满足我们的需求，有时会根据情况在代码中自定义控件，这就需要用到LayoutInflater
		infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		blackListHashmap = apps;

	}

	// 返回应用的个数
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

	// 当界面每显示出来一个item时，就会调用该方法,getView()有三个参数，
	// 第一个参数表示该item在Adapter中的位置；
	// 第二个参数是item的View对象，是滑动list时将要显示在界面上的item，如果有item在显示界面消失，
	// 这时android会将消失的item返回，称为旧view，也就是说此时的view不为null；第三个参数用在加载xml视图。
	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		// System.out.println("getView at " + position);

		View view = null;
		// 获取布局文件的控件
		ViewHolder2 holder = null;
		// 如果视图为空，就把视图加载进来，存到view里
		if (convertview == null || convertview.getTag() == null) {
			// view=LinearLayout (id=830040609408)
			view = infater.inflate(R.layout.browse_black_item, null);
			// 把视图（view=LinearLayout）给ViewHolder，因为要获取视图里的控件
			holder = new ViewHolder2(view);
			view.setTag(holder);
			// System.out.println(view.setTag(holder));
		} else {
			view = convertview;
			holder = (ViewHolder2) convertview.getTag();
		}
		// 把视图set对应的数据
		String blackInfo =(String) getItem(position);
		holder.tvPId2.setText("包名："+blackInfo);
		/*holder.tvAppLabel.setText(appInfo.getAppLabel());
		holder.tvPkgName.setText(appInfo.getPkgName());
		holder.tvProcessId.setText(appInfo.getPid() + "");
		holder.tvProcessName.setText(appInfo.getProcessName());*/
		// 最后返回一个view，一个有数据与视图的view（返回给适配器）
		return view;
	}

	class ViewHolder2 {
		TextView tvPId2;
		public ViewHolder2(View view) {
			this.tvPId2 = (TextView) view.findViewById(R.id.tvPId2);
		}
	}
}