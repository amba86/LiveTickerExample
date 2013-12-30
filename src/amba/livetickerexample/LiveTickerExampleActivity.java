package amba.livetickerexample;

import java.util.ArrayList;

import amba.livetickerexample.adapter.HorizontalListViewAdapter;
import amba.livetickerexample.view.HorizontalListView;
import android.os.Bundle;
import android.app.Activity;

public class LiveTickerExampleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live_ticker_example);

		final ArrayList<String> strings = new ArrayList<String>();
		final String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
				"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
				"Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
				"OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
				"Android", "iPhone", "WindowsMobile" };

		for (int i = 0; i < values.length; ++i) {
			strings.add(values[i]);
		}

		final HorizontalListViewAdapter adapter = new HorizontalListViewAdapter(
				this, strings);
		HorizontalListView horizontalListView = (HorizontalListView) this
				.findViewById(R.id.activity_live_ticker_example_hlistview);
		horizontalListView.setAdapter(adapter);
	}
}
