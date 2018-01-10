package com.huntloc.handheldoffline;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class JournalActivity extends AppCompatActivity {
	String response, badge, name;
	TextView cardIdText;
	Button buttonEntrance, buttonExit;
	ImageView iv_portrait;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Intent intent = getIntent();
		response = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);
		/*final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);*/

		

		cardIdText = (TextView) findViewById(R.id.textView_CardId);
		iv_portrait = (ImageView) findViewById(R.id.imageView_Portrait);
		MySQLiteHelper db = new MySQLiteHelper(this.getApplicationContext());

		Portrait portrait = db.getPortrait(response);

		String outputData;

		outputData = "CardID: " + portrait.getPrintedCode() + "\r\n";
		outputData += "Nombre: " + portrait.getName() + "\r\n";
		cardIdText.setText(outputData);
		
		byte[] byteArray;
		Bitmap bitmap;
		try {
			byteArray = Base64
					.decode(portrait.getPortrait(), 0);
			bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
					byteArray.length);
			iv_portrait.setImageBitmap(bitmap);
		} catch (Exception ex) {
		}
		
		badge = portrait.getPrintedCode();
		name = portrait.getName();

		buttonEntrance = (Button) findViewById(R.id.button_Entrance);
		buttonEntrance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String door = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("door_id", "Sliding Gate");
				String area = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("door_id", "Process");
				String log = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("logEntry_id", "EntrySlidingGate");
				String desc = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("descLogEntry_id", "Entrance");
				addRecord(badge, name, door, log, desc );
			}
		});
		buttonExit = (Button) findViewById(R.id.button_Exit);
		buttonExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String door = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("door_id", "Sliding Gate");
				String area = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("door_id", "Process");
				String log = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("logExit_id", "ExitSlidingGate");
				String desc = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("descLogExit_id", "Exit");
				addRecord(badge, name, door, log, desc);
			}
		});

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addRecord(String badge, String name,String door, String log, String descLog) {
		java.util.Date date = new java.util.Date();

		MySQLiteHelper db = new MySQLiteHelper(this.getApplicationContext());

		db.addRecord(new Journal(badge, log, door, date.getTime(), name, descLog));
		//NavUtils.navigateUpFromSameTask(this);
		finish();
	}
}
