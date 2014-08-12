package com.example.vano.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vano on 8/11/14.
 */
public class LazyAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, Object>> data;
	private static LayoutInflater inflater = null;

	public LazyAdapter(Activity activity, ArrayList<HashMap<String, Object>> data){
		this.activity = activity;
		this.data = data;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return (Long) data.get(position).get(MainActivity.CONTACT_ID);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if(vi == null){
			vi = inflater.inflate(R.layout.contact_list_item, null);
		}
		ImageView imageView = (ImageView) vi.findViewById(R.id.person_photo);
		TextView nameView = (TextView) vi.findViewById(R.id.person_name);
		//TextView numberView = (TextView) vi.findViewById(R.id.person_number);
		String imageUrl = (String) data.get(position).get(MainActivity.PHOTO_THUMBNAIL_URI);
		if(imageUrl != null)
			imageView.setImageURI(Uri.parse(imageUrl));
		else
			imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_launcher));
		nameView.setText((String) data.get(position).get(MainActivity.DISPLAY_NAME));
		//numberView.setText((String) data.get(position).get(MainActivity.NUMBER));

		System.out.println((String) data.get(position).get(MainActivity.NUMBER));


		return vi;
	}
}
