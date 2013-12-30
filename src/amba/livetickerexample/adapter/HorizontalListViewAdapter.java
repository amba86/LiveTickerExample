package amba.livetickerexample.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HorizontalListViewAdapter extends BaseAdapter {

	private Context mContext = null;
	private List<String> mStrings = null;

	public HorizontalListViewAdapter(Context context, List<String> strings) {
		this.mContext = context;
		this.mStrings = strings;
	}

	@Override
	public int getCount() {
		return this.mStrings.size();
	}

	@Override
	public Object getItem(int position) {
		return this.mStrings.get(position);
	}

	@Override
	public long getItemId(int position) {
		return this.getItem(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = new TextView(this.mContext);
		textView.setText((CharSequence) this.getItem(position));
		return textView;
	}
}
