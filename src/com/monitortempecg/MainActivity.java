package com.monitortempecg;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.monitortempecg.service.Packet;
import com.monitortempecg.service.PacketECG;
import com.monitortempecg.service.PacketTemp;
import com.monitortempecg.service.SerialClient;
import com.monitortempecg.service.SerialClient.OnClientListner;
import com.monitortempecg.widgets.graph.Chart;
import com.monitortempecg.widgets.graph.ChartSeries;

public class MainActivity extends Activity implements OnClickListener,
		OnClientListner {

	private TextView textView;
	private TextView textView2;
	private Button botao1;

	public SerialClient client;
	private Chart chart1;
	private ChartSeries ecgChartSeries;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		botao1 = (Button) findViewById(R.id.button1);
		chart1 = (Chart) findViewById(R.id.chart1);
		botao1.setOnClickListener(this);

		client = new SerialClient(this, this);
		
		
		ecgChartSeries = new ChartSeries(800);
		ecgChartSeries.enable();
		ecgChartSeries.setColor(Color.BLUE);
		chart1.series.add(ecgChartSeries);
		chart1.update();

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
		}
	}

	@Override
	public void notifyReceivedData(Packet pacote) {
		// TODO Auto-generated method stub
		if (pacote instanceof PacketECG) {
			PacketECG ecg = (PacketECG) pacote;
			processaPacoteEcg(ecg);
		} else if (pacote instanceof PacketTemp) {
			PacketTemp temp = (PacketTemp) pacote;
			processaPacoteTemp(temp);
		}
	}

	private void processaPacoteEcg(PacketECG ecg) {
		textView.setText("ECG: "+ecg.getValue());
		ecgChartSeries.newData(ecg.getValue());
		chart1.update();
		
	}

	private void processaPacoteTemp(PacketTemp temp) {
		textView2.setText("Temperatura: "+temp.getValue());
	}


	@Override
	public void notifyConnected() {
		// TODO Auto-generated method stub
		Log.d("SERIAL", "conectado");
		botao1.setText("Desconectar");

	}

	@Override
	public void notifyDisconnected() {
		// TODO Auto-generated method stub
		Log.d("SERIAL", "desconectado");
		botao1.setText("Conectar");

	}

}
