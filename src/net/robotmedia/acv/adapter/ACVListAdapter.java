package net.robotmedia.acv.adapter;

import java.util.ArrayList;

import net.androidcomics.acv.R;
import android.content.Context;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.TextView;

public abstract class ACVListAdapter<T> extends BaseAdapter {

	Context context;
	protected ArrayList<T> contents;
	protected int rowResId;

	public ACVListAdapter(Context context, int rowResId) {
		this.context = context;
		this.rowResId = rowResId;
		contents = new ArrayList<T>();
	}

	public abstract void refresh();

	@Override
	public int getCount() {
		return contents.size();
	}

	@Override
	public T getItem(int position) {
		return contents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	
	protected View makeRow() {
		if(rowResId <= 0) {
			TextView tv = new TextView(context);
			tv.setId(R.id.text);
			return tv;
		} else {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(rowResId, null);
			return v;
		}
	}

}
