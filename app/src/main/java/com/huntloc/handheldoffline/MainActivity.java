package com.huntloc.handheldoffline;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/*import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;*/
import org.json.JSONArray;

import org.json.JSONObject;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
		OnRefreshListener {
	private NfcAdapter mNfcAdapter;
	private static long back_pressed;
	public static final String EXTRA_MESSAGE = "com.huntloc.handheldoffline.MESSAGE";
	public static final String PREFS_NAME = "HandheldOfflinePrefsFile";
	private SwipeRefreshLayout swipeRefreshLayout;
	ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.list_Layout);
		swipeRefreshLayout.setOnRefreshListener(this);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "This device doesn't support NFC.",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (!mNfcAdapter.isEnabled()) {
			Toast.makeText(this, "Please enable NFC.", Toast.LENGTH_LONG)
					.show();
		}
		handleIntent(getIntent());
		listRecords();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		/**Puertas*/
		if (!settings.contains("door_id")) {
			editor.putString("door_id", "Main Gate");
		}
		if (!settings.contains("area_id")) {
			editor.putString("area_id", "Plant");
		}
		if (!settings.contains("logEntry_id")) {
			editor.putString("logEntry_id", "EntryMainGate");
		}
		if (!settings.contains("logExit_id")) {
			editor.putString("logExit_id", "ExitMainGate");
		}
		/***/
		
		if (!settings.contains("descLogEntry_id")) {
			editor.putString("descLogEntry_id", "Entrance");
		}
		if (!settings.contains("descLogExit_id")) {
			editor.putString("descLogExit_id", "Exit");
		}
		editor.commit();

		((TextView) findViewById(R.id.textView_DoorId))
				.setText(getSharedPreferences(PREFS_NAME, 0).getString(
						"door_id", "Main Gate"));

		progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setIndeterminate(true);
		progress.setProgressNumberFormat(null);
		progress.setProgressPercentFormat(null);
		progress.setCanceledOnTouchOutside(false);

	}

	@Override
	public void onResume() {
		super.onResume();
		listRecords();
	}

	public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				listRecords();
			}
		}, 1000);
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage[] msgs;
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				Parcelable parcelable = intent
						.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				Tag tag = (Tag) parcelable;

				byte[] id = tag.getId();

				MySQLiteHelper db = new MySQLiteHelper(
						this.getApplicationContext());
				Portrait portrait = db.getPortrait(Long.toString(getDec(id)));
				if (portrait != null) {
					Intent newIntent = new Intent(MainActivity.this,
							JournalActivity.class);
					newIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					newIntent
							.putExtra(EXTRA_MESSAGE, Long.toString(getDec(id)));
					startActivity(newIntent);
				} else {
					Toast.makeText(this, "Couldn't find badge.",
							Toast.LENGTH_LONG).show();
				}
			}
		}

	}

	private void listRecords() {
		swipeRefreshLayout.setRefreshing(true);
		MySQLiteHelper db = new MySQLiteHelper(this.getApplicationContext());
		List<Journal> records = db.getAllRecords();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < records.size(); i++) {

			HashMap<String, String> item = new HashMap<String, String>();
			item.put("badge", records.get(i).getName() + "("
					+ records.get(i).getBadge() + ")");
			item.put("log", records.get(i).getDescLog());
			item.put("door", records.get(i).getDoor());
			String dateString = DateFormat.format("E, MMM dd, h:mm aa",
					new Date(records.get(i).getTime())).toString();
			item.put("time", dateString);
			list.add(item);
		}
		ListView recordsListView = null;
		recordsListView = (ListView) findViewById(R.id.journal_log);

		String[] columns = new String[] { "badge", "time", "door", "log" };
		int[] renderTo = new int[] { R.id.badge, R.id.time, R.id.door, R.id.log };

		ListAdapter listAdapter = new SimpleAdapter(this, list,
				R.layout.journal_row, columns, renderTo);

		recordsListView.setAdapter(listAdapter);
		swipeRefreshLayout.setRefreshing(false);
	}

	private void deleteRecords() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_delete_message);
		builder.setTitle(R.string.dialog_delete_title);
		builder.setPositiveButton(R.string.dialog_delete_delete,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						MySQLiteHelper db = new MySQLiteHelper(
								MainActivity.this.getApplicationContext());
						db.deleteRecords();
						listRecords();
					}
				});
		builder.setNegativeButton(R.string.dialog_delete_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

	private long getDec(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = 0; i < bytes.length; ++i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		switch (item.getItemId()) {

		/*
		 * case R.id.action_list: listRecords(); return true;
		 */
		case R.id.action_send:
			sendRecords();
			return true;
			/*
			  case R.id.action_delete: deleteRecords(); return true;*/
		case R.id.action_update:
			updatePortraits();
			return true;
		case R.id.door_sliding:
			editor.putString("door_id", "Sliding Gate");
			editor.putString("area_id", "Process");
			editor.putString("logEntry_id", "EntrySlidingGate");
			editor.putString("logExit_id", "ExitSlidingGate");
			editor.commit();
			break;
		case R.id.door_north:
			editor.putString("door_id", "North Entrance");
			editor.putString("area_id", "Process");
			editor.putString("logEntry_id", "EntryNorthEntrance");
			editor.putString("logExit_id", "ExitNorthEntrance");
			editor.commit();
			break;
		case R.id.door_south:
			editor.putString("door_id", "South Entrance");
			editor.putString("area_id", "Process");
			editor.putString("logEntry_id", "EntrySouthEntrance");
			editor.putString("logExit_id", "ExitSouthEntrance");
			editor.commit();
			break;
		case R.id.door_main:
			editor.putString("door_id", "Main Gate");
			editor.putString("area_id", "Plant");
			editor.putString("logEntry_id", "EntryMainGate");
			editor.putString("logExit_id", "ExitMainGate");
			editor.commit();
			break;
		default:
			break;
		}
		try {
			((TextView) findViewById(R.id.textView_DoorId))
					.setText(getSharedPreferences(PREFS_NAME, 0).getString(
							"door_id", "Main Gate"));
		} catch (Exception e) {
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendRecords() {

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (!ni.isConnected()) {
					Toast.makeText(this, "Please connect to PeruLNG network.",
							Toast.LENGTH_LONG).show();
					return;
				}
		}

		MySQLiteHelper db = new MySQLiteHelper(this.getApplicationContext());
		List<Journal> records = db.getAllRecords();

		if (records.size() > 0) {
			progress.setMessage(getResources().getString(
					R.string.action_send_message));
			progress.show();
			
			for (int i = 0; i < records.size(); i++) {
				// String dateString = DateFormat.format("E, MMM dd, h:mm aa",
				// new Date(records.get(i).getTime())).toString();

				String serverURL = getResources().getString(
						R.string.service_url)
						+ "/JournalLogService/"
						+ records.get(i).getBadge()
						+ "/"
						+ records.get(i).getLog()
						+ "/"
						+ records.get(i).getTime()
						+ "/"
						+ records.get(i).getGuid();
				JournalTask journalTask = new JournalTask();
				journalTask.setParent(this);
				journalTask.setProgressDialog(progress);
				journalTask.setIndex(i);
				journalTask.setTotal(records.size());
				journalTask.execute(serverURL);
				Log.d("Send", serverURL);
			}
		} else {
			Toast.makeText(this, "No hay registros disponibles",
					Toast.LENGTH_LONG).show();

		}
	}

	private void updatePortraits() {

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (!ni.isConnected()) {
					Toast.makeText(this, "Please connect to PeruLNG network.",
							Toast.LENGTH_LONG).show();
					return;
				}
		}
		String serverURL = getResources().getString(R.string.service_url)
				+ "/PersonnelOfflineService/" + UUID.randomUUID().toString();

		Log.d("URL Personnel", serverURL);
		QueryPortraitsTask portraitsTask = new QueryPortraitsTask();
		progress.setMessage(getResources().getString(
				R.string.action_update_message));
		progress.show();

		portraitsTask.execute(serverURL);
	}

	@Override
	public void onBackPressed() {
		if (back_pressed + 2000 > System.currentTimeMillis())
			super.onBackPressed();
		else
			Toast.makeText(getBaseContext(), "Press once again to exit!",
					Toast.LENGTH_SHORT).show();
		back_pressed = System.currentTimeMillis();
	}

	private class QueryPortraitsTask extends AsyncTask<String, Integer, Void> {

		String response = "";
		HttpURLConnection urlConnection;
		private QueryPortraitsTask() {
		}

		@SuppressWarnings("unchecked")
		protected Void doInBackground(String... urls) {

			HttpURLConnection urlConnection = null;

			/*HttpClient httpclient = new DefaultHttpClient();
			HttpPost request = new HttpPost(urls[0]);

			@SuppressWarnings("rawtypes")
			ResponseHandler handler = new BasicResponseHandler();*/

			StringBuilder result = new StringBuilder();
			try {
				URL url = new URL(urls[0]);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("POST");
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
				//response = (String)httpclient.execute(request, handler);
				response =  result.toString();
			} catch (Exception e) {
				Log.d(e.getClass().toString(), e.getMessage());
				progress.dismiss();
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(MainActivity.this,
								"Couldn't Connect with CCURE",
								Toast.LENGTH_LONG).show();
					}
				});
			}finally {
				urlConnection.disconnect();
			}
			//httpclient.getConnectionManager().shutdown();
			return null;
		}

		protected void onPostExecute(Void unused) {
			try {

				JSONObject jsonResponse = new JSONObject(response);
				JSONArray jsonArray = jsonResponse.getJSONArray("values");
				MySQLiteHelper db = new MySQLiteHelper(
						MainActivity.this.getApplicationContext());
				db.deletePortraits();

				for (int i = 0; i < jsonArray.length(); i++) {
					Portrait portrait = new Portrait(jsonArray.getJSONObject(i)
							.optString("InternalCode"), jsonArray
							.getJSONObject(i).optString("PrintedCode"),
							jsonArray.getJSONObject(i).optString("Portrait"),
							jsonArray.getJSONObject(i).optString("Name"));
					db.addPortrait(portrait);

				}
				progress.dismiss();
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(MainActivity.this, "Descarga Completa",
								Toast.LENGTH_LONG).show();
					}
				});

			} catch (Exception e) {
				Log.d(e.getClass().toString(), e.getMessage());
				/*
				 * MainActivity.this.runOnUiThread(new Runnable() { public void
				 * run() { Toast.makeText(MainActivity.this,
				 * "Couldn't Complete Update", Toast.LENGTH_LONG).show(); } });
				 */
			}
		}

	}

	private class JournalTask extends AsyncTask<String, Integer, Void> {

		String response;
		ProgressDialog progress;
		int index, total;
		HttpURLConnection urlConnection;
		MainActivity parent;

		public void setParent(MainActivity parent) {
			this.parent = parent;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public void setProgressDialog(ProgressDialog progress) {
			this.progress = progress;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		@SuppressWarnings("unchecked")
		protected Void doInBackground(String... arg0) {
			StringBuilder result = new StringBuilder();
			//HttpClient httpclient = new DefaultHttpClient();

			//HttpPost request = new HttpPost(arg0[0]);

			//@SuppressWarnings("rawtypes")
			//ResponseHandler handler = new BasicResponseHandler();
			try {
				URL url = new URL(arg0[0]);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("POST");
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
				response =  result.toString();
				//response = (String)httpclient.execute(request, handler);
			} catch (Exception e) {
				response = arg0[0];
				Log.d(e.getClass().toString(), e.getMessage());
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(MainActivity.this,
								"Couldn't Connect with CCURE",
								Toast.LENGTH_SHORT).show();
					}
				});

				progress.dismiss();
			}finally {
				urlConnection.disconnect();
			}
			//httpclient.getConnectionManager().shutdown();

			return null;
		}

		protected void onProgressUpdate(Integer... _progress) {
			if (index == total - 1) {
				parent.listRecords();
				progress.dismiss();
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(MainActivity.this, "Envio Completo",
								Toast.LENGTH_LONG).show();
					}
				});
			}
		}

		protected void onPostExecute(Void result) {

			try {
				JSONObject jsonResponse = new JSONObject(response);
				/*
				 * String log = jsonResponse.optString("log").contains("Entry")
				 * ? "Entrada" : "Salida"; response =
				 * jsonResponse.optString("records") + " " + log +
				 * " Registrada";
				 * 
				 * Toast.makeText(MainActivity.this, response,
				 * Toast.LENGTH_LONG) .show();
				 */
				String guid = jsonResponse.optString("guid");
				MySQLiteHelper db = new MySQLiteHelper(
						MainActivity.this.getApplicationContext());
				Journal record = new Journal(guid, null, null, null, 0, false,
						null, null);
				db.updateRecord(record);
				// Log.d("Sent", guid);
				MainActivity.this.runOnUiThread(new Runnable() { public void
				 run() { publishProgress(0); } });


			} catch (Exception e) {
				Log.d(e.getClass().toString(), e.getMessage());
				/*
				 * MainActivity.this.runOnUiThread(new Runnable() { public void
				 * run() { Toast.makeText(MainActivity.this,
				 * "Couldn't Complete Send", Toast.LENGTH_LONG).show(); } });
				 */
			}
		}
	}
}
