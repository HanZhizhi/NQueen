package com.space.nqueen;

import java.util.ArrayList;
import java.util.logging.MemoryHandler;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class QueenActivity extends Activity {
	private Button btGo, btNext, btLast, btAuto, btSetColumns, btSpdUp, btSpqDn, btStop, btQuit;
	private Button btDanbu;
	private TextView tvSum, tvCur, tvStepGoing, tvQueenNum;
	private ScrollView stepSay;
	private GridLayout queenGrid;
	private LinearLayout cttGoAll, cttShow;
	private ArrayList<ImageView> bts = new ArrayList<ImageView>();
	private int mScreenHeight, mScreenWidth, itemLength;
	// ����ģ�鹲�ñ���
	private int colums; // ���̵�������
	private int resNum = 0; // �ʺ������Ӧ�Ľ�ĸ���
	// �����н�ģ��ı���
	private int[] testingLine; // ��ǰ��Ľ⣬��һ�ν�ı�һ��
	private int currentResult = 0; // ����չʾ�Ľ��������ֵ
	private String resultsChain = ""; // ƴ���ַ����������д�
	// private int[][] resCh=null; //���ö�ά���鱣�����д�
	// �Զ���ʾģ��ı���
	private int tmpCurrent = -1; // ������ʾʱ��ʵ�Ĵ�����ֵ
	private int runSpeed = 400; // �Զ���ʾ��Ĭ���ٶ�
	private ShowThread showingThread = null; // ��ʾ�߳�
	private boolean maxSpeed = false, minSpeed = false;
	// ����ִ��ģ��ı���
	private int stepingRow = 0; // ��ǰ��λ�õ������
	private int stepingColumn = 0; // ��ǰ��λ�õ������
	private boolean firstClick = true; // �״ε������ִ�а�ťflag
	private int dbStep = 0; // ����ִ�мƲ�
	String saying = ""; // ÿһ����ӡLog
	private int[] stepLine; // ����ִ���еĽ�����

	private class ShowThread extends Thread {
		private volatile boolean beRunning = true;

		public void stopShowing() {
			this.beRunning = false;
			super.interrupt();
		}

		@Override
		public void run() {
			for (int i = tmpCurrent; i <= resNum; ++i) {
				if (this.beRunning == true) {
					Message msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
					// Log.i("�̷߳���", "��һ���ź�");
					try {
						Thread.sleep(runSpeed);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_queen);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		WindowManager wManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dMetrics = new DisplayMetrics();
		wManager.getDefaultDisplay().getMetrics(dMetrics);
		mScreenHeight = dMetrics.heightPixels;
		mScreenWidth = dMetrics.widthPixels;

		Intent fromIntent = getIntent();
		Bundle getBundle = fromIntent.getExtras();
		colums = getBundle.getInt("num");

		if (colums <= 6)
			itemLength = mScreenHeight / colums / 3 * 2;
		else
			itemLength = mScreenHeight / colums;

		testingLine = new int[colums];
		for (int i = 0; i < colums; ++i) {
			testingLine[i] = -1;
		}
		// resCh=new int[60000][colums];

		stepLine = new int[colums];
		for (int i = 0; i < colums; ++i) {
			stepLine[i] = -1;
		}

		initViews();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (tmpCurrent < resNum) {
					drawResultTable(tmpCurrent);
					++tmpCurrent;
					if (tmpCurrent < resNum)
						currentResult = tmpCurrent;
				} else {
					btGo.setEnabled(true);
					btNext.setEnabled(true);
					btLast.setEnabled(true);
					btDanbu.setEnabled(true);
					btAuto.setEnabled(true);
					btAuto.setText("�Զ�չʾ");
					btSpdUp.setEnabled(false);
					btSpqDn.setEnabled(false);
					btStop.setEnabled(false);
					btQuit.setEnabled(false);
					cttShow.setVisibility(View.GONE);
					Toast toast = Toast.makeText(getApplicationContext(), "չʾ���~~", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				break;
			default:
				break;
			}
		}
	};

	private void initViews() {
		queenGrid = (GridLayout) findViewById(R.id.tableGrid);
		for (int i = 0; i < colums; i++) {
			for (int j = 0; j < colums; j++) {
				ImageView bn = new ImageView(this);
				bn.setClickable(false);
				bn.setMinimumHeight(itemLength);
				bn.setMaxHeight(itemLength);
				bn.setMinimumWidth(itemLength);
				bn.setMaxWidth(itemLength);
				bn.setScaleType(ScaleType.FIT_CENTER);
				bn.setAdjustViewBounds(true);
				GridLayout.Spec rowSpec = GridLayout.spec(i + 2);
				GridLayout.Spec columnSpec = GridLayout.spec(j);
				String msg = "rowSpec:" + (i + 2) + " - columnSpec:" + (j);
				GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
				params.setGravity(Gravity.FILL);
				queenGrid.addView(bn, params);
				bts.add(bn);
			}
		}
		initQueenTable();

		tvSum = (TextView) findViewById(R.id.tvSum);
		tvCur = (TextView) findViewById(R.id.tvCur);
		tvQueenNum = (TextView) findViewById(R.id.tvQueenNum);
		tvSum.setText("δִ�в�����");
		tvQueenNum.setText("�ʺ������" + colums);

		tvStepGoing = (TextView) findViewById(R.id.tvStepStatus);
		stepSay = (ScrollView) findViewById(R.id.scrollerStepSay);
		cttGoAll = (LinearLayout) findViewById(R.id.contentAllLayout);
		cttShow = (LinearLayout) findViewById(R.id.contentShow);

		btGo = (Button) findViewById(R.id.btGo);
		btLast = (Button) findViewById(R.id.btLastAnswer);
		btNext = (Button) findViewById(R.id.btNextAnswer);
		btAuto = (Button) findViewById(R.id.btAutoShowAll);
		btDanbu = (Button) findViewById(R.id.btStepGo);
		btSetColumns = (Button) findViewById(R.id.btSetCol);
		btSpdUp = (Button) findViewById(R.id.btSpeedUp);
		btSpqDn = (Button) findViewById(R.id.btSpeedDown);
		btStop = (Button) findViewById(R.id.btStopShow);
		btQuit = (Button) findViewById(R.id.btQuitShow);

		btNext.setEnabled(false);
		btLast.setEnabled(false);
		btAuto.setEnabled(false);
		btSpdUp.setEnabled(false);
		btSpqDn.setEnabled(false);
		btStop.setEnabled(false);
		btQuit.setEnabled(false);

		btSetColumns.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(QueenActivity.this, MainActivity.class));
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				finish();
			}
		});

		btGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// һϵ�г�ʼ��
				// initQueenTable();
				resNum = 0;
				currentResult = 0;
				// resultsChain = "";
				cttGoAll.setVisibility(View.VISIBLE);
				btLast.setEnabled(true);
				btNext.setEnabled(true);
				btAuto.setEnabled(true);
				btAuto.setText("�Զ�չʾ");
				stepSay.setVisibility(View.GONE);
				queen(0);
				drawResultTable(0);
			}
		});

		btNext.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onClick(View v) {
				if (currentResult >= resNum - 1) {
					Toast toast = Toast.makeText(getApplicationContext(), "����û�н��ˣ�", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else {
					++currentResult;
					initQueenTable();
					drawResultTable(currentResult);
				}
				/*
				 * bts.get(0).setImageBitmap(BitmapFactory.decodeResource(
				 * getApplicationContext().getResources(),R.raw.queen));
				 */
			}
		});

		btLast.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/* bts.get(0).setImageBitmap(null); */
				Log.i("LastJie", currentResult + ":" + resNum);
				if (currentResult <= 0) {
					Toast toast = Toast.makeText(getApplicationContext(), "��ǰû�н��ˣ�", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else if (currentResult >= resNum) {
					currentResult = resNum - 2;
					drawResultTable(currentResult);
				} else {
					--currentResult;
					drawResultTable(currentResult);
				}
			}
		});

		btAuto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cttShow.setVisibility(View.VISIBLE);

				btGo.setEnabled(false);
				btNext.setEnabled(false);
				btLast.setEnabled(false);
				btDanbu.setEnabled(false);
				btAuto.setEnabled(false);

				btSpdUp.setEnabled(true);
				btSpqDn.setEnabled(true);
				btStop.setEnabled(true);
				btQuit.setEnabled(true);

				if (currentResult >= resNum - 1) {
					tmpCurrent = 0;
				} else {
					tmpCurrent = currentResult;
				}

				runSpeed = 400;

				showingThread = new ShowThread();
				showingThread.start();
			}
		});

		btSpdUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (runSpeed > 100) {
					runSpeed -= 50;
				} else {
					if (!minSpeed) {
						minSpeed = true;
						Toast toast = Toast.makeText(getApplicationContext(), "�ѵ�������ٶȣ�", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				}
			}
		});

		btSpqDn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (runSpeed < 2000) {
					runSpeed += 200;
				} else {
					if (!minSpeed) {
						minSpeed = true;
						Toast toast = Toast.makeText(getApplicationContext(), "�ѵ�������ٶȣ�", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				}
			}
		});

		btStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btStop.setEnabled(false);
				btAuto.setEnabled(true);
				btAuto.setText("����չʾ");
				// btGo.setEnabled(true);
				showingThread.stopShowing();
				try {
					showingThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		btQuit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// currentResult=0;
				Log.i("quit��", "resNum:" + resNum + "CurrentResult" + currentResult);
				tmpCurrent = 0;
				resNum = 0;
				queen(0);
				Log.i("quit��", "resNum:" + resNum + "CurrentResult" + currentResult);
				drawResultTable(currentResult);

				btGo.setEnabled(true);
				btGo.setText("���¼���");
				btSpdUp.setEnabled(false);
				btSpqDn.setEnabled(false);
				btDanbu.setEnabled(true);
				btAuto.setEnabled(true);
				btNext.setEnabled(true);
				btLast.setEnabled(true);
				btAuto.setText("�Զ�չʾ");

				cttShow.setVisibility(View.GONE);

				showingThread.stopShowing();
				try {
					showingThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				btStop.setEnabled(false);
				btQuit.setEnabled(false);
			}
		});

		btDanbu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btNext.setEnabled(false);
				btLast.setEnabled(false);
				btAuto.setEnabled(false);

				tvSum.setText("����ִ��ing~~");
				tvCur.setText("");

				stepSay.setVisibility(View.VISIBLE);

				if (firstClick) {
					initQueenTable();
					saying = "��������Կ�ʼ����ִ�У�" + "\n" + "\n";
					tvStepGoing.setText(saying);
					firstClick = false;
				} else {
					++dbStep;
					queenDanbu();
				}
			}
		});
	}

	private void queen(int rowi) {
		if (rowi >= colums) {
			for (int j = 0; j < testingLine.length; ++j) {
				resultsChain += testingLine[j];
			}
			/*
			 * for(int i=0;i<colums;++i) { resCh[resNum][i]=testingLine[i]; }
			 */
			++resNum;
			return;
		} else {
			for (int i = 0; i < colums; ++i) {
				if (canPutQueen(rowi, i)) {
					testingLine[rowi] = i;
					queen(rowi + 1);
				}
			}
		}
	}

	private void queenDanbu() {
		Log.i("����+���У�",dbStep+"����"+stepingRow+"��"+stepingColumn+"��");
		if (stepingRow == 0 && stepingColumn >= colums) {
			Toast toast = Toast.makeText(getApplicationContext(), "���ҵ����н⣡", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		if (stepingRow >= colums) {
			if (stepLine[0] >= (colums - 1)) {
				saying += "All done !" + "\n";
				tvStepGoing.setText(saying);
			} else {
				stepingRow = 0;
				stepingColumn = stepLine[0] + 1;
				/*
				 * String jie=""; for(int i=0;i<colums;++i) { jie+=stepLine[i];
				 * } saying+="�ҵ�һ���⣺"+jie+"\n"; tvStepGoing.setText(saying);
				 */
				initQueenTable();
				initStepingLine();
			}
			return;
		} else {
			stepLine[stepingRow] = stepingColumn;
			drawStepTable();
			saying += "��" + dbStep + "��ִ��,������" + stepingRow + "��" + stepingColumn + "�з��ûʺ�" + "\n";
			tvStepGoing.setText(saying);
			if (stepCanPutQueen(stepingRow, stepingColumn)) {
				stepLine[stepingRow] = stepingColumn;
				saying += "��" + stepingRow + "��" + stepingColumn + "�п��Է��ûʺ󣬽�����һ��" + "\n" + "\n";
				tvStepGoing.setText(saying);
				stepSay.fullScroll(ScrollView.FOCUS_DOWN);
				if (stepingRow >= colums - 1) {
					stepLine[stepingRow] = -1;
					stepingRow = 0;
					stepingColumn = stepLine[0] + 1;
				} else {
					++stepingRow;
					stepingColumn = 0;
				}

			} else {
				saying += "��" + stepingRow + "��" + stepingColumn + "�в����Է��ûʺ󣬼����һ��" + "\n" + "\n";
				tvStepGoing.setText(saying);
				stepSay.fullScroll(ScrollView.FOCUS_DOWN);
				stepLine[stepingRow] = -1;
				if (stepingColumn >= (colums - 1)) {
					--stepingRow;
					if (stepLine[stepingRow] >= (colums - 1)) {
						saying += "��" + stepingRow + "��" + "��ȫ����⣬δ���ֿ���λ�ã���������һ��" + "\n" + "\n";
						tvStepGoing.setText(saying);
						stepSay.fullScroll(ScrollView.FOCUS_DOWN);
						stepLine[stepingRow] = -1;
						if (stepingRow==0) {    //���ҵ�ȫ����
							Toast toast = Toast.makeText(getApplicationContext(), "���ҵ����н⣡", Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
							//
							initQueenTable();
							stepSay.setVisibility(View.GONE);
							dbStep=stepingRow=stepingColumn=0;
							firstClick = true;
							saying = "��������Կ�ʼ����ִ�У�" + "\n" + "\n";
							tvStepGoing.setText(saying);
						}
						else
							{
							--stepingRow;
							stepingColumn = stepLine[stepingRow] + 1;
							}
						
					} else {
						stepingColumn = stepLine[stepingRow] + 1;
						saying += "��������" + stepingRow + "�У�����Ϊ��" + stepingColumn + "\n" + "\n";
						tvStepGoing.setText(saying);
						stepSay.fullScroll(ScrollView.FOCUS_DOWN);
					}
				} else {
					++stepingColumn;
				}
			}
		}
	}

	private void initStepingLine() {
		for (int i = 0; i < colums; ++i) {
			stepLine[i] = -1;
		}
	}

	private boolean canPutQueen(int hang, int lie) {
		for (int i = 0; i < hang; ++i) {
			if (testingLine[i] == lie || Math.abs(testingLine[i] - lie) == Math.abs(i - hang)) {
				return false;
			}
		}
		return true;
	}

	private boolean stepCanPutQueen(int hang, int lie) {
		for (int i = 0; i < hang; ++i) {
			if (stepLine[i] == lie || Math.abs(stepLine[i] - lie) == Math.abs(i - hang)) {
				return false;
			}
		}
		return true;
	}

	private void initQueenTable() {
		for (int i = 0; i < colums; ++i) {
			if (i % 2 == 1) {
				for (int j = 0; j < colums; ++j) {
					if (j % 2 == 0) {
						bts.get(i * colums + j).setBackgroundColor(Color.BLACK);
					} else {
						bts.get(i * colums + j).setBackgroundColor(Color.WHITE);
					}
				}
			} else {
				for (int j = 0; j < colums; ++j) {
					if (j % 2 == 1) {
						bts.get(i * colums + j).setBackgroundColor(Color.BLACK);
					} else {
						bts.get(i * colums + j).setBackgroundColor(Color.WHITE);
					}
				}
			}
		}
	}

	/*
	 * private void clearQueenTable() { for (int i = 0; i < colums; ++i) { if (i
	 * % 2 == 1) { for (int j = 0; j < colums; ++j) { if (j % 2 == 0) {
	 * bts.get(i * colums + j).setBackgroundColor(Color.BLACK); bts.get(i *
	 * colums + j).setImageBitmap(null); } else { bts.get(i * colums +
	 * j).setBackgroundColor(Color.WHITE); bts.get(i * colums +
	 * j).setImageBitmap(null); } } } else { for (int j = 0; j < colums; ++j) {
	 * if (j % 2 == 1) { bts.get(i * colums +
	 * j).setBackgroundColor(Color.BLACK); bts.get(i * colums +
	 * j).setImageBitmap(null); } else { bts.get(i * colums +
	 * j).setBackgroundColor(Color.WHITE); bts.get(i * colums +
	 * j).setImageBitmap(null); } } } } }
	 */

	private void putQueen(int hang, int in) {
		bts.get(hang * colums + in).setBackgroundColor(Color.RED);
		// setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.raw.queen));
	}

	private void drawResultTable(int index) {
		initQueenTable();
		tvSum.setText("����" + resNum + "���⣬");
		tvCur.setText("��ǰչʾ��" + (index + 1) + "����");
		String drawingString = resultsChain.substring(index * colums, index * colums + colums);
		int[] drawing = new int[colums];
		for (int i = 0; i < drawingString.length(); ++i) {
			drawing[i] = Integer.parseInt(drawingString.substring(i, i + 1));
		}
		/*
		 * int[] drawing = new int[colums]; drawing=resCh[index];
		 */
		for (int j = 0; j < colums; ++j) {
			putQueen(j, drawing[j]);
		}
	}

	private void drawStepTable() {
		initQueenTable();
		for (int i = 0; i < colums; ++i) {
			if (stepLine[i] != -1) {
				putQueen(i, stepLine[i]);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startActivity(new Intent(QueenActivity.this, MainActivity.class));
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
