package de.emdete.android.gui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Sample extends Activity {
	static final String TAG = "de.emdete.sample";
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }
	Messenger mService = null;
	boolean mIsBound;
	Button mStatus;
	final Messenger mMessenger = new Messenger(new Handler(){
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
				case R.id.msg_set_value:
					mStatus.setText("Received from service: " + msg.arg1);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	});
	ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, R.id.msg_register_client);
				msg.replyTo = mMessenger;
				mService.send(msg);
				msg = Message.obtain(null, R.id.msg_set_value, this.hashCode(), 0, new Bundle());
				mService.send(msg);
			}
			catch (RemoteException ignore) {
			}
			Log.d(TAG, "Sample.ServiceConnection.onServiceConnected: remote service connected");
			mStatus.setText("Attached.");
		}
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			Log.d(TAG, "Sample.ServiceConnection.onServiceDisconnected: remote service disconnected");
			mStatus.setText("Disconnected.");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		if (DEBUG) Log.d(TAG, "onCreate");
		setContentView(R.layout.main);
		mStatus = (Button)findViewById(R.id.button);
		mStatus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (DEBUG) Log.d(TAG, "onClick");
				doTest(context);
			}
		});
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

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume");
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
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		if (DEBUG) Log.d(TAG, "onSaveInstanceState bundle=" + bundle);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
	}

	public void doTest(Context context) {
		if (DEBUG) Log.d(TAG, "doTest");
		mStatus.setText("Start!");
		if(mIsBound) {
			doUnbindService();
		}
		else {
			doBindService();
		}
	}

	void doBindService() {
		bindService(new Intent(this, MessengerService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		mStatus.setText("Binding.");
	}

	void doUnbindService() {
		if (mIsBound) {
			mIsBound = false;
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, R.id.msg_unregister_client);
					msg.replyTo = mMessenger;
					mService.send(msg);
				}
				catch (RemoteException ignore) {
				}
			}
			unbindService(mConnection);
			mStatus.setText("Unbinding.");
		}
	}
}
