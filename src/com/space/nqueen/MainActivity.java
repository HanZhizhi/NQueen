package com.space.nqueen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText numEditText;
	private Button btDraw;
	private ImageButton aboutIb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		numEditText=(EditText) findViewById(R.id.etNum);
		btDraw=(Button) findViewById(R.id.btOk);
		btDraw.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (numEditText.getText().toString().length()==0) {
					Toast toast=Toast.makeText(getApplicationContext(), "请输入棋盘大小！", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER,0,0);
					toast.show();
				}
				else{
					int n;
					n=Integer.parseInt(numEditText.getText().toString());
					if (n<4) {
						n=4;
						Toast toast = Toast.makeText(getApplicationContext(), "输入数值小于最小值，已自动调整！", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					if (n>10) {
						n=10;
						Toast toast = Toast.makeText(getApplicationContext(), "输入数值大于最大值，已自动调整！", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					Bundle numBundle=new Bundle();
					numBundle.putInt("num",n);
					Intent drawIntent=new Intent(MainActivity.this,QueenActivity.class);
					drawIntent.putExtras(numBundle);
					startActivity(drawIntent);
					overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
					numEditText.setText("");
					finish();
				}
			}
		});
		
		aboutIb=(ImageButton) findViewById(R.id.ibAbout);
		aboutIb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this,InfoActivity.class));
				overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
			}
		});
	}
}
