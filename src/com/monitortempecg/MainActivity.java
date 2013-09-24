package com.monitortempecg;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private TextView textView;
	private Button botao1;
	private Button botao2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView1);
		botao1 = (Button) findViewById(R.id.button1);
		botao2 = (Button) findViewById(R.id.button2);

		botao1.setOnClickListener(this);
		botao2.setOnClickListener(this);


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
			textView.setText("Rodou1");
		}else if (arg0 == botao2) {
			textView.setText("Rodou2");
		}

	}

}
