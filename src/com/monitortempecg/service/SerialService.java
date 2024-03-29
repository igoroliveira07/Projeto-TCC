package com.monitortempecg.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.monitortempecg.MainActivity;
import com.monitortempecg.R;
import com.monitortempecg.service.USBConnection.ConnectionListner;

/**
 * http://developer.android.com/guide/components/bound-services.html#Messenger
 * 
 */
public class SerialService extends Service implements ConnectionListner {
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SEND_DATA = 3;

	private WakeLock wakeLock;
	private USBConnection mavConnection;
	Messenger msgCenter = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean couldNotOpenConnection = false;

	/**
	 * Handler of incoming messages from clients.
	 */
	@SuppressLint("HandlerLeak")
	// TODO fix this error message
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				msgCenter = msg.replyTo;
				if (couldNotOpenConnection) {
					selfDestryService();
				}
				break;
			case MSG_UNREGISTER_CLIENT:
				msgCenter = null;
				break;
			case MSG_SEND_DATA:
				Bundle b = msg.getData();
				Packet packet = (Packet) b.getSerializable("msg");
				if (mavConnection != null) {
					mavConnection.sendPacket(packet);
				}
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("SERIAL", "BIND");
		return mMessenger.getBinder();
	}

	private void notifyNewMessage(Packet m) {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null,
						SerialClient.MSG_RECEIVED_DATA);
				Bundle data = new Bundle();
				data.putSerializable("msg", m);
				msg.setData(data);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onReceiveMessage(Packet msg) {
		notifyNewMessage(msg);
	}

	@Override
	public void onDisconnect() {
		couldNotOpenConnection = true;
		selfDestryService();
	}

	private void selfDestryService() {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null,
						SerialClient.MSG_SELF_DESTRY_SERVICE);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		connectMAVconnection();
		showNotification();
		aquireWakelock();
		updateNotification("Connected");
	}

	@Override
	public void onDestroy() {
		disconnectMAVConnection();
		dismissNotification();
		releaseWakelock();
		super.onDestroy();
	}

	/**
	 * Toggle the current state of the MAVlink connection. Starting and closing
	 * the as needed. May throw a onConnect or onDisconnect callback
	 */
	private void connectMAVconnection() {
		Log.d("SERIAL", "new Connection");
		mavConnection = new USBConnection(this);
		mavConnection.start();
	}

	private void disconnectMAVConnection() {
		if (mavConnection != null) {
			mavConnection.disconnect();
			mavConnection = null;
		}
	}

	/**
	 * Show a notification while this service is running.
	 */
	static final int StatusBarNotification = 1;

	private void showNotification() {
		updateNotification("Disconnected");
	}

	private void updateNotification(String text) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText(text);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);
		mBuilder.setContentIntent(contentIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(StatusBarNotification, mBuilder.build());
	}

	private void dismissNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();

	}

	@SuppressWarnings("deprecation")
	protected void aquireWakelock() {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (PreferenceManager.getDefaultSharedPreferences(
					getApplicationContext()).getBoolean(
					"pref_keep_screen_bright", false)) {
				wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, "CPU");
			} else {
				wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
						"CPU");
			}

			wakeLock.acquire();
		}
	}

	protected void releaseWakelock() {
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

}
