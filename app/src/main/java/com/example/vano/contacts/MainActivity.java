package com.example.vano.contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class MainActivity extends Activity {

	public static final String CONTACT_ID = "_ID";
	public static final String DISPLAY_NAME = "DISPLAY_NAME";
	public static final String PHOTO_THUMBNAIL_URI = "PHOTO_THUMBNAIL_URI";
	public static final String NUMBER = "NUMBER";
//	public static final String DISPLAY_NAME = "DISPLAY_NAME";


	private ListView listView;

	private LazyAdapter adapter;

	private MainActivity me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

		final SearchView searchView = (SearchView) findViewById(R.id.search_field);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				String q = searchView.getQuery().toString();
				System.out.println(q);
				initList(q);
				return true;
			}
		});

		initList("");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_acitvity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



	private void initList(String q){
		Cursor cursor = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				null,
				ContactsContract.PhoneLookup.DISPLAY_NAME + " like '%" + q + "%'",
				null,
				"IS_USER_PROFILE DESC, STARRED DESC, HAS_PHONE_NUMBER DESC, TIMES_CONTACTED DESC");

		listView = getListView();

		adapter = new LazyAdapter(this, getDataFromCursor(cursor));
		listView.setAdapter(adapter);

//
//		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.activity_main,
//				cursor, fromColumns, toViews, 0);
//
//		listView.setAdapter(cursorAdapter);
		listView.setOnItemClickListener(contactClickedHandler);
		cursor.close();
	}

	private AdapterView.OnItemClickListener contactClickedHandler = new AdapterView.OnItemClickListener() {

		public void onItemClick(AdapterView parent, View v, int position, long id) {
			String ids = Long.toString(id);
			System.out.println(ids);
			Cursor cursor = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ids ,
					null,
					null);

			Set<HashMap<String, String>> numberSet= getContactPhoneNumbers(cursor);

			if(numberSet.size() > 1){
				openDialog(numberSet);
			} else if(numberSet.size() == 1){
				makeACall(numberSet.iterator().next().get("number"));
			}

			//cursor.close();
		}


		private Set<HashMap<String, String>> getContactPhoneNumbers(Cursor cursor){
			int normNumberInd = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
			int numberInd = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			int typeInd = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
			int labelInd = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
			Set<HashMap<String, String>> numberSet = new HashSet<HashMap<String, String>>();
			//int i = 0;
			while(cursor.moveToNext()){
				String typeName;
				switch(cursor.getInt(typeInd)){
					case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
						typeName = "MOBILE";
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
						typeName = "HOME";
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
						typeName = "MAIN";
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
						typeName = "CAR";
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
						typeName = "WORK";
						break;
					case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
						typeName = cursor.getString(labelInd);
						break;
					default:
						typeName = "OTHER";
				}
				String number = cursor.getString(normNumberInd);
				if(number == null || "null".equals(number))
					number = cursor.getString(numberInd);
				HashMap<String, String> num = new HashMap<String, String>();
				num.put("number", number);
				num.put("label", typeName);
				numberSet.add(num);
			}
			return numberSet;
		}

		private void openDialog(Set<HashMap<String, String>> numberSet){
			Iterator<HashMap<String, String>> it = numberSet.iterator();
			final String[] numbers = new String[numberSet.size()];
			String[] labelsAndNumbers = new String[numberSet.size()];
			int i= 0;
			while(it.hasNext()){
				HashMap<String, String> map = it.next();
				String label = map.get("label");
				String number = map.get("number");
				numbers[i] = number;
				labelsAndNumbers[i] = label + ":\t\t" + number;
				i++;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
					.setTitle("Choose Number")
					.setItems(labelsAndNumbers, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							makeACall(numbers[which]);
						}
					})
					.setCancelable(true);
			AlertDialog alert = builder.create();
			alert.show();
		}

	};

	public void makeACall(String number){
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setData(Uri.parse("tel:" + number));
		getApplicationContext().startActivity(intent);
	}

	private String getContactPrimaryNumber(int contact_id){
		return "";
//		String ids = Long.toString(contact_id);
//		Cursor cursor = getContentResolver().query(
//				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//				null,
//				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ids + " and " +
//						ContactsContract.CommonDataKinds.Phone.IS_PRIMARY + "=" + 1,
//				null,
//				null);
//		if(cursor.getCount() == 0){
//			//System.out.println("---------------> 0");
//			return "";
//		}
//		cursor.moveToNext();
//		String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
//		cursor.close();
//		return number;
	}

	private ArrayList<HashMap<String, Object>> getDataFromCursor(Cursor cursor){

		int nameInd = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
		int photoInd = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI);
		int idInd = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);

		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

		while(cursor.moveToNext()){
			HashMap<String, Object> row = new HashMap<String, Object>();
			row.put(DISPLAY_NAME, cursor.getString(nameInd));
			row.put(PHOTO_THUMBNAIL_URI, cursor.getString(photoInd));
			row.put(NUMBER, getContactPrimaryNumber(cursor.getInt(idInd)));
			row.put(CONTACT_ID, cursor.getLong(idInd));
			data.add(row);
		}
		return data;
	}

	private ListView getListView(){
		return ( ListView ) findViewById(R.id.list);
	}

}
