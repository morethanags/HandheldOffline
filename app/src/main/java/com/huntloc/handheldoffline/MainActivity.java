package com.huntloc.handheldoffline;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;
import java.util.List;
import java.util.UUID;


import org.json.JSONArray;

import org.json.JSONObject;



import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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

import android.os.Parcelable;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        HandheldFragment.OnHandheldFragmentInteractionListener,
        EntranceFragment.OnEntranceFragmentInteractionListener, ExitFragment.OnExitFragmentInteractionListener {
    public static final String EXTRA_MESSAGE = "com.huntloc.handheldoffline.MESSAGE";
    public static final String PREFS_NAME = "HandheldOfflinePrefsFile";
    private static long back_pressed;
    //private SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog progress;
    TextView textView_lastupdate_date;
    private NfcAdapter mNfcAdapter;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "Por favor habilitar NFC.", Toast.LENGTH_LONG)
                    .show();
        }
        handleIntent(getIntent());
        //listRecords();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        /**Puertas*/
        if (!settings.contains("door_id")) {
            editor.putString("door_id", "Main Gate Offline");
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

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgressNumberFormat(null);
        progress.setProgressPercentFormat(null);
        progress.setCanceledOnTouchOutside(false);

        textView_lastupdate_date = (TextView) findViewById(R.id.textView_lastupdate_date);
        textView_lastupdate_date.setText("Última actualización: " + getSharedPreferences(PREFS_NAME, 0).getString("lastupdate", "No se ha sincronizado"));
    }

    private void showLastUpdate() {

        textView_lastupdate_date.setText("Última actualización: "
                + getSharedPreferences(PREFS_NAME, 0).getString("lastupdate",
                "No se ha sincronizado"));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
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
                        getApplicationContext());
                Portrait portrait = db.getPortrait(Long.toString(getDec(id)));

                if (portrait != null ) {
                    HandheldFragment handheldFragment = ((HandheldFragment) mSectionsPagerAdapter.getItem(0));
                    if (handheldFragment != null) {
                        handheldFragment.setCredentialId(portrait.getPrintedCode());
                    }
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Handheld");
                    alertDialogBuilder.setMessage("Credencial No Tiene Acceso");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    alertDialogBuilder.create().show();
                }
            }
        }
    }

    private Date parseString(String date) {
        String value = date.replaceFirst("\\D+([^\\)]+).+", "$1");
        String[] timeComponents = value.split("[\\-\\+]");
        long time = Long.parseLong(timeComponents[0]);

		/*  int timeZoneOffset = Integer.valueOf(timeComponents[1]) * 36000; if
          (value.indexOf("-") > 0) { timeZoneOffset *= -1; } time +=
		  timeZoneOffset;*/

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        /*calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);*/
        return calendar.getTime();

    }

    private void deleteRecords() {
        MySQLiteHelper db = new MySQLiteHelper(
                MainActivity.this.getApplicationContext());
        db.deleteRecords();
        //listRecords();
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_delete_message);
		builder.setTitle(R.string.dialog_delete_title);
		builder.setPositiveButton(R.string.dialog_delete_delete,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {


					}
				});
		builder.setNegativeButton(R.string.dialog_delete_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();*/
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
		/*case R.id.action_send:
			sendRecords();
			return true;
			  case R.id.action_delete: deleteRecords(); return true;*/

            case R.id.action_update:
                updatePortraits();
                return true;
            case R.id.door_sliding:
                checkClearance(getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("area_id", "Process"),"Process" );
                editor.putString("door_id", "Sliding Gate");
                editor.putString("area_id", "Process");
                editor.putString("logEntry_id", "EntrySlidingGate");
                editor.putString("logExit_id", "ExitSlidingGate");

                editor.commit();
                break;
            case R.id.door_north:
                checkClearance(getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("area_id", "Process"), "Process");
                editor.putString("door_id", "North Entrance");
                editor.putString("area_id", "Process");
                editor.putString("logEntry_id", "EntryNorthEntrance");
                editor.putString("logExit_id", "ExitNorthEntrance");
                editor.commit();
                break;
            case R.id.door_south:
                checkClearance(getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("area_id", "Process"), "Process");
                editor.putString("door_id", "South Entrance");
                editor.putString("area_id", "Process");
                editor.putString("logEntry_id", "EntrySouthEntrance");
                editor.putString("logExit_id", "ExitSouthEntrance");
                editor.commit();
                break;
            /*case R.id.door_main:
                checkClearance(getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("area_id", "Process"), "Plant");
                editor.putString("door_id", "Main Gate Offline");
                editor.putString("area_id", "Plant");
                editor.putString("logEntry_id", "EntryMainGate");
                editor.putString("logExit_id", "ExitMainGate");
                editor.commit();
                break;*/
            default:
                break;
        }
        try {
            ((TextView) findViewById(R.id.textView_DoorId))
                    .setText(getSharedPreferences(PREFS_NAME, 0).getString(
                            "door_id", "Sliding Gate"));
        } catch (Exception e) {
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkClearance(String currentarea, String newarea) {
        Log.d("areas", currentarea + " " + newarea);
        if (!currentarea.equalsIgnoreCase(newarea)) {
            Toast.makeText(getBaseContext(), "Por favor sincronizar información",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRecords() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (!ni.isConnected()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Handheld");
                    alertDialogBuilder.setMessage("Red WiFi no Disponible");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    alertDialogBuilder.create().show();
                    return;
                }
        }

        MySQLiteHelper db = new MySQLiteHelper(this.getApplicationContext());
        List<Journal> records = db.getAllRecords(null);

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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Handheld");
            alertDialogBuilder.setMessage("No hay marcaciones registradas");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alertDialogBuilder.create().show();

        }
    }

    private void updatePortraits() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (!ni.isConnected()) {
                    Toast.makeText(this, "Por favor conectar a red PeruLNG.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
        }
        String area = getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("area_id", "Process");
        String serverURL = getResources().getString(R.string.service_url)
                + "/PersonnelOfflineService/" + area + "/" + UUID.randomUUID().toString();

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
            Toast.makeText(getBaseContext(), "Presione una vez mas para salir!",
                    Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    @Override
    public void onHandheldFragmentInteraction() {

    }

    @Override
    public void onEntranceFragmentInteraction() {

    }

    @Override
    public void onExitFragmentInteraction() {

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
                response = result.toString();
            } catch (Exception e) {
                Log.d(e.getClass().toString(), e.getMessage());
                progress.dismiss();
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "No se pudo conectar con CCURE",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
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
                            jsonArray.getJSONObject(i).optString("Name"),
                            jsonArray.getJSONObject(i).optString("Access"),
                            jsonArray.getJSONObject(i).isNull("CAMOExpirationDate") ? null : jsonArray.getJSONObject(i).optString("CAMOExpirationDate"),
                            jsonArray.getJSONObject(i).isNull("ExpirationDate") ? null : jsonArray.getJSONObject(i).optString("ExpirationDate"));
                    db.addPortrait(portrait);

                }
                Log.d("countPortrait", jsonArray.length() + "");
                progress.dismiss();
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        SimpleDateFormat newDateFormat = new SimpleDateFormat(
                                "EEEE, d MMMM yyyy h:mm a");
                        Calendar today = Calendar.getInstance();
                        SharedPreferences settings = getSharedPreferences(
                                PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("lastupdate",
                                newDateFormat.format(today.getTime()));
                        editor.commit();

                        MainActivity.this.showLastUpdate();
                        //Concatenate task
                        MainActivity.this.sendRecords();
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
                response = result.toString();
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
            } finally {
                urlConnection.disconnect();
            }
            //httpclient.getConnectionManager().shutdown();

            return null;
        }

        protected void onProgressUpdate(Integer... _progress) {
            if (index == total - 1) {

                parent.deleteRecords();
                progress.dismiss();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Handheld");
                alertDialogBuilder.setMessage("Actualización Completa");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.create().show();
            }
        }

        protected void onPostExecute(Void result) {

            try {
				/*JSONObject jsonResponse = new JSONObject(response);

				 * String log = jsonResponse.optString("log").contains("Entry")
				 * ? "Entrada" : "Salida"; response =
				 * jsonResponse.optString("records") + " " + log +
				 * " Registrada";
				 *
				 * Toast.makeText(MainActivity.this, response,
				 * Toast.LENGTH_LONG) .show();

				String guid = jsonResponse.optString("guid");
				MySQLiteHelper db = new MySQLiteHelper(
						MainActivity.this.getApplicationContext());
				Journal record = new Journal(guid, null, null, null, 0, false,
						null, null);
				db.updateRecord(record);
				// Log.d("Sent", guid);*/
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void
                    run() {
                        publishProgress(0);
                    }
                });


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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private HandheldFragment handheldFragment;
        private EntranceFragment entranceFragment;
        private ExitFragment exitFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                if (handheldFragment == null) {
                    handheldFragment = new HandheldFragment();
                }
                fragment = handheldFragment;
            } else if (position == 1) {
                if (entranceFragment == null) {
                    entranceFragment = new EntranceFragment();
                }
                fragment = entranceFragment;
            } else if (position == 2) {
                if (exitFragment == null) {
                    exitFragment = new ExitFragment();
                }
                fragment = exitFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "HANDHELD";
                case 1:
                    return "ENTRANCE";
                case 2:
                    return "EXIT";
            }
            return null;
        }
    }
}
