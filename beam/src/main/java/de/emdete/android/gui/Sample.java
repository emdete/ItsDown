package de.emdete.android.gui;

import android.net.Uri;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Sample extends Activity implements CreateNdefMessageCallback {
	// see https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#p2p
	static final String TAG = "de.emdete.sample";
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }
	static final String uri = "xmpp://mdt@emdete.de";

	Context context;
	NfcAdapter nfcAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		setContentView(R.layout.main);
		context = getBaseContext();
		Button button = (Button)findViewById(R.id.button);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (DEBUG) Log.d(TAG, "onRestart");
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	Uri getJellyBean(NdefRecord record) {
		return record.toUri();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume nfcAdapter=" + nfcAdapter);
		Button button = (Button)findViewById(R.id.button);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (DEBUG) Log.d(TAG, "onCreate nfcAdapter=" + nfcAdapter);
		if (nfcAdapter != null) {
			if (nfcAdapter.isEnabled() && nfcAdapter.isNdefPushEnabled()) {
				// only if nfc and nde/beam is enabled we can proceed
				button.setText("NFC and Beam available");
				nfcAdapter.setNdefPushMessageCallback(this, this);
				Intent intent = getIntent();
				// this is the moment where we actually notice that we received a beam event:
				if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
					Uri uri = Uri.parse("");
					for (Parcelable message : getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
						if (message instanceof NdefMessage) {
							for (NdefRecord record : ((NdefMessage)message).getRecords()) {
								switch (record.getTnf()) {
									case NdefRecord.TNF_WELL_KNOWN: {
										Log.d(TAG, "use TNF_MIME_MEDIA");
										if (Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
											Log.d(TAG, "use RTD_URI");
											if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
												uri = getJellyBean(record);
											} else {
												byte[] payload = record.getPayload();
// see https://github.com/android/platform_frameworks_base/blob/master/core/java/android/nfc/NdefRecord.java#L713
												if (payload[0] == 0) {
													uri = Uri.parse(new String(Arrays.copyOfRange(
															payload, 1, payload.length)));
												}
											}
										}
										else {
											Log.d(TAG, "ignored by Type record=" + record);
										}
										break;
									}
									case NdefRecord.TNF_MIME_MEDIA: {
										Log.d(TAG, "use TNF_MIME_MEDIA");
										uri = Uri.parse(new String(record.getPayload()));
										break;
									}
									default: {
										Log.d(TAG, "ignored by Tnf record=" + record);
										break;
									}
								}
							}
						}
						else {
							Log.e(TAG, "ignored by class message=" + message);
						}
					}
					button.setText(uri.toString());
				}
			}
			else {
				// if either is swithed off ask the user to turn it on, it may
				// well be that we land here without any nfc in the device
				button.setText(nfcAdapter.isEnabled() ?
					"Beam not enabled"
					:
					"NFC and Beam not enabled"
					);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(nfcAdapter.isEnabled() ?
					"For this operation you need Beam which is currently disabled, you have to enable it in the settings."
					:
					"For this operation you need NFC and Beam which is currently disabled, you have to enable it in the settings."
					);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
					}
				});
				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});
				builder.create().show();
			}
		}
		else {
			// devices without any nfc capability often just return null
			button.setText("NFC not available");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		NdefMessage msg = new NdefMessage(new NdefRecord[] {
			NdefRecord.createMime("application/vnd.de.emdete.android.sample", uri.getBytes()),
			NdefRecord.createUri(uri),
			NdefRecord.createApplicationRecord("de.emdete.android.gui"),
			});
		return msg;
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
	}
}
