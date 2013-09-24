package com.monitortempecg;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.monitortempecg.service.SerialClient;
import com.monitortempecg.service.SerialClient.OnClientListner;
import com.monitortempecg.service.Packet;

public class MainActivity extends Activity implements OnClickListener,
		OnClientListner {

	private TextView textView;
	private Button botao1;
	private Button botao2;
	
	public SerialClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView1);
		botao1 = (Button) findViewById(R.id.button1);
		botao2 = (Button) findViewById(R.id.button2);

		botao1.setOnClickListener(this);
		botao2.setOnClickListener(this);

		client = new SerialClient(this, this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		if (arg0 == botao1) {
			client.toggleConnectionState();
			Log.d("SERIAL", "Toogle");
			textView.setText("Rodou1");
		} else if (arg0 == botao2) {
			textView.setText("Rodou2");
		}

	}

	@Override
	public void notifyReceivedData(Packet m) {
		// TODO Auto-generated method stub
		Log.d("SERIAL", "Recebeu pacote");

	}

	@Override
	public void notifyConnected() {
		// TODO Auto-generated method stub
		Log.d("SERIAL", "connectado");

	}

	@Override
	public void notifyDisconnected() {
		// TODO Auto-generated method stub
		Log.d("SERIAL", "desconnectado");
	}

}
