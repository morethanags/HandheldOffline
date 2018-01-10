package com.huntloc.handheldoffline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class HandheldFragment extends Fragment {

    private EditText mCredentialId;
    private Button buttonCkeck;
    private OnHandheldFragmentInteractionListener mListener;
    public static final String EXTRA_MESSAGE = "com.huntloc.handheldoffline.MESSAGE";
    //public static final String PREFS_NAME = "HandheldOfflinePrefsFile";

    public HandheldFragment() {
        // Required empty public constructor
    }


    public static HandheldFragment newInstance() {
        HandheldFragment fragment = new HandheldFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void clearCredentialId() {
        if(mCredentialId!=null){
            mCredentialId.setText("");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_handheld, container, false);
        mCredentialId = (EditText) view.findViewById(R.id.editText_CredentialId);
        mCredentialId.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (mCredentialId.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(),
                                "Tap a Badge or Enter Credential ID",
                                Toast.LENGTH_LONG).show();
                    } else {
                        processCode(mCredentialId.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        buttonCkeck = (Button) view.findViewById(R.id.button_Register);
        buttonCkeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCredentialId.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(),
                            "Tap a Badge or Enter Credential ID",
                            Toast.LENGTH_LONG).show();
                } else {
                    processCode(mCredentialId.getText().toString());
                }
            }
        });
        ((TextView) view.findViewById(R.id.textView_DoorId))
                .setText(HandheldFragment.this.getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0).getString(
                        "door_id", "Main Gate"));
        return view;
    }

    public void setCredentialId(String id) {
        mCredentialId.setText(id);
        processCode(id);
    }
    private void processCode(String code){
        MySQLiteHelper db = new MySQLiteHelper(
                getContext());
        Portrait portrait = db.getPortrait(code);
        if (portrait != null) {
            Intent newIntent = new Intent(getActivity(),
                    JournalActivity.class);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            newIntent
                    .putExtra(EXTRA_MESSAGE, code);
            startActivity(newIntent);
            clearCredentialId();
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHandheldFragmentInteractionListener) {
            mListener = (OnHandheldFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnHandheldFragmentInteractionListener {
        // TODO: Update argument type and name
        void onHandheldFragmentInteraction();
    }
}
