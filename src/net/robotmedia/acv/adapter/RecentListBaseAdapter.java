package net.robotmedia.acv.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.androidcomics.acv.R;
import net.robotmedia.acv.provider.HistoryManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecentListBaseAdapter extends ACVListAdapter<String> {
	protected int maxNumItems = -1;
	
	public RecentListBaseAdapter(Context context, int rowResId) {
		super(context, rowResId);
		contents = new ArrayList<String>();
		refresh();
	}

	public void setMaxNumItems(int num) {
		maxNumItems = num;
		refresh();
	}
	
	@Override
	public void refresh() {
		contents.clear();
		List<String> lastFiles = HistoryManager.getInstance(context).getRecentFiles();
		if(maxNumItems > 0) {
			int c = 0;
			for(String file : lastFiles) {
				contents.add(file);
				if(++c >= maxNumItems) {
					break;
				}
			}
		} else {
			// No limit
			contents.addAll(lastFiles);
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv;
		View v;
		
		if(convertView == null) {
			//tv = new TextView(context);
			v = makeRow();
		} else {
			v = convertView;
		}
		
		File f = new File(getItem(position));
		tv = (TextView) v.findViewById(R.id.text);
		tv.setText(f.getName());
		
		return v;
	}

}
