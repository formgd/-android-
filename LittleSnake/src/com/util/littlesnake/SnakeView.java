package com.util.littlesnake;


import java.util.ArrayList;
import java.util.Random;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
public class SnakeView extends MyTile {
	private static final String tag = "swz";
	private int mMode = READY;
	//游戏的四种状态
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;
    //蛇体运动的方向识别
	private int mDirection = NORTH;
	private int mNextDirection = NORTH;
	private static final int NORTH = 1;
	private static final int SOUTH = 2;
	private static final int EAST = 3;
	private static final int WEST = 4;
	//游戏中仅有的三种砖块对应的数值 yelow 是头  green是墙 red 是身子
	
	
	private static final int RIGHTHEAD =1;
    private static final int WALL =2;
    private static final int BODY =3;

	private long mScore = 0;//记录获得的分数
	private long mMoveDelay = 600;//每移动一步的延时。初始时设置为600ms
	private long mLastMoveTime;
	private TextView mStatusTextView;//用来显示游戏状态的TextView
	//两个链表，分别用来存储蛇体和果子的坐标
	private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();
    //随机数生成器
	private static final Random RNG = new Random();
	//用Handler机制实现定时刷新
	private RefreshHandler mRedrawHandler = new RefreshHandler();
	class RefreshHandler extends Handler {
        //获取消息并处理
		@Override
		public void handleMessage(Message msg) {
			SnakeView.this.update();
			SnakeView.this.invalidate();//刷新view为基类的界面
			Log.i(tag, "handleMessage|Thread Name="+Thread.currentThread().getName());
		}
        //定时发送消息给UI线程，以此达到更新效果
		public void sleep(long delayMillis) {
			this.removeMessages(0); //清空消息队列，Handler进入对新消息的等待   
			Log.i(tag, "sleep|Thread Name="+Thread.currentThread().getName());
			//定时发送新消息,激活handler   
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};
	public SnakeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(tag, "SnakeView Constructor");
		initSnakeView();//构造函数中，初始化游戏
	}
	//初始化SnakeView类，注意，这根初始化游戏是不一样的。 
	private void initSnakeView() {
		Log.e(tag, "initSnakeView");
		//设置焦点，由于存在 文字界面 和 游戏界面的跳转。这个focus是不可或缺的。
		setFocusable(true);
		//取得资源中的图片，加载到 砖块字典 中。 
		Resources r = this.getContext().getResources();
		resetTiles(4);
		loadTile(BODY, r.getDrawable(R.drawable.body));
		loadTile(RIGHTHEAD, r.getDrawable(R.drawable.righthead));
		loadTile(WALL, r.getDrawable(R.drawable.wall));
	}
	//更新游戏状态
	public void setMode(int newMode) {
		int oldMode = mMode;
		mMode = newMode;
		Resources res = getContext().getResources();
		CharSequence str = "";
		if (newMode == RUNNING & oldMode != RUNNING) {
			mStatusTextView.setVisibility(View.INVISIBLE);
			update();
			return;
		}
		if (newMode == PAUSE) {
			str = res.getText(R.string.mode_pause);
		}
		if (newMode == READY) {
			str = res.getText(R.string.mode_ready);
		}
		if (newMode == LOSE) {
			str = res.getString(R.string.mode_lose_prefix) + mScore
					+ res.getString(R.string.mode_lose_suffix);
		}
		mStatusTextView.setText(str);
		mStatusTextView.setVisibility(View.VISIBLE);
	}
    //绑定到相应的textview
	public void setStatusTextView(TextView newView) {
		mStatusTextView = newView;
	}
   //按键的监听
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			if (mMode == READY | mMode == LOSE) {
				initNewGame();
				setMode(RUNNING);
				update();
				return (true);
			}
			if (mMode == PAUSE) {
				setMode(RUNNING);
				update();
				return (true);
			}
			if (mDirection != SOUTH) {
				mNextDirection = NORTH;
			}
			return (true);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (mDirection != NORTH) {
				mNextDirection = SOUTH;
			}
			return (true);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (mDirection != EAST) {
				mNextDirection = WEST;
			}
			return (true);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mDirection != WEST) {
				mNextDirection = EAST;
			}
			return (true);
		}
		return super.onKeyDown(keyCode, msg);
	}
   //刷新游戏状态。每次游戏画面的更新、游戏数据的更新，都是依靠这个update()来完成的
	public void update() {
		if (mMode == RUNNING) {
			long now = System.currentTimeMillis();
			if (now - mLastMoveTime > mMoveDelay) {
				//
				clearTiles();
				updateWalls();
				updateSnake();
				updateApples();
				mLastMoveTime = now;
			}
			mRedrawHandler.sleep(mMoveDelay);
		}
	}
    //用setTile绘制墙壁
	private void updateWalls() {
		Log.i(tag, "updateWalls");
		for (int x = 0; x < xcount; x++) {
			setTile(WALL, x, 0);
			setTile(WALL, x, ycount - 1);
		}
		for (int y = 1; y < ycount - 1; y++) {
			setTile(WALL, 0, y);
			setTile(WALL, xcount - 1, y);
		}
	}
    //绘制果子 
	private void updateApples() {
		Log.i(tag, "updateApples");

		for (Coordinate c : mAppleList) {
			setTile(BODY, c.x, c.y);
		}
	}
    //更新蛇体
	private void updateSnake() {
		//吃过果子的蛇会长长。这个变量即为它的标记。
		boolean growSnake = false;
		//头部很重要，只有头部可能碰到果子。
		Coordinate head = mSnakeTrail.get(0); 
		//蛇下一步一定会前移，也就试newHead。长度只会从尾部增加。
		Coordinate newHead = new Coordinate(1, 1);
		mDirection = mNextDirection;
		switch (mDirection) {
		case EAST: {
			newHead = new Coordinate(head.x + 1, head.y);
			break;
		}
		case WEST: {
			newHead = new Coordinate(head.x - 1, head.y);
			break;
		}
		case NORTH: {
			newHead = new Coordinate(head.x, head.y - 1);
			break;
		}
		case SOUTH: {
			newHead = new Coordinate(head.x, head.y + 1);
			break;
		}
		}
		if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > xcount - 2)
				|| (newHead.y > ycount - 2)) {
			setMode(LOSE);
			return;
		}
		int snakelength = mSnakeTrail.size();
		for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
			Coordinate c = mSnakeTrail.get(snakeindex);
			if (c.equals(newHead)) {
				setMode(LOSE);
				return;
			}
		}
		int applecount = mAppleList.size();
		for (int appleindex = 0; appleindex < applecount; appleindex++) {
			Coordinate c = mAppleList.get(appleindex);
			if (c.equals(newHead)) {
				mAppleList.remove(c);
				addRandomApple();
				mScore++;
				mMoveDelay *= 0.9;
				growSnake = true;
			}
		}
		mSnakeTrail.add(0, newHead);
		if (!growSnake) {
			mSnakeTrail.remove(mSnakeTrail.size() - 1);
		}
		int index = 0;
		for (Coordinate c : mSnakeTrail) {
			if (index == 0) {
				setTile(RIGHTHEAD, c.x, c.y);
			} else {
				setTile(BODY, c.x, c.y);
			}
			index++;
		}
	}
    //这是坐标点的类,很简单的存储XY坐标
	private class Coordinate {
		public int x;
		public int y;
		public Coordinate(int newX, int newY) {
			x = newX;
			y = newY;
		}
		public boolean equals(Coordinate other) {
			if (x == other.x && y == other.y) {
				return true;
			}
			return false;
		}
		@Override
		public String toString() {
			return "Coordinate: [" + x + "," + y + "]";
		}
	}
    //在地图上随机的增加果子
	private void addRandomApple() {
		Coordinate newCoord = null;
		boolean flag = true;
		while (flag) {
			int newX = 1 + RNG.nextInt(xcount - 2);
			int newY = 1 + RNG.nextInt(ycount - 2);
			newCoord = new Coordinate(newX, newY);
			boolean collision = false;
			int snakelength = mSnakeTrail.size();
			for (int index = 0; index < snakelength; index++) {

				if (mSnakeTrail.get(index).equals(newCoord)) {
					collision = true;
				}
			}
			flag = collision;
		}
		if (newCoord == null) {
			Log.e(tag, "Somehow ended up with a null newCoord!");
		}
		mAppleList.add(newCoord);
	}
	 //如果不是从暂停中回复，就绪要 初始化游戏了。
	public void initNewGame() {
		Log.e(tag, "initNewGame!");
		//清空保存蛇体和果子的数据结构。  
		mSnakeTrail.clear();
		mAppleList.clear();
		// 设定初始状态的蛇体的位置。  
		mSnakeTrail.add(new Coordinate(7, 7));
		mSnakeTrail.add(new Coordinate(6, 7));
		mSnakeTrail.add(new Coordinate(5, 7));
		mSnakeTrail.add(new Coordinate(4, 7));
		mSnakeTrail.add(new Coordinate(3, 7));
		mSnakeTrail.add(new Coordinate(2, 7));
		mNextDirection = NORTH;
        //开始有两个果子
		
		addRandomApple();
		mMoveDelay = 600;
		mScore = 0;
	}
    //在意外情况下，暂时性保存游戏数据
	public Bundle saveState() {
		Bundle bundle = new Bundle();
		bundle.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
		bundle.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));
		bundle.putInt("mDirection", Integer.valueOf(mDirection));
		bundle.putInt("mNextDirection", Integer.valueOf(mNextDirection));
		bundle.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
		bundle.putLong("mScore", Long.valueOf(mScore));
		return bundle;
	}
   //回复游戏数据,是saveState()的逆过程 

	public void restoreState(Bundle icicle) {
		setMode(PAUSE);
		mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
		mDirection = icicle.getInt("mDirection");
		mNextDirection = icicle.getInt("mNextDirection");
		mMoveDelay = icicle.getLong("mMoveDelay");
		mScore = icicle.getLong("mScore");
		mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
	}
   //coordArrayListToArray（）的逆过程，用来读取保存在Bundle中的数据
	private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
		ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();
		int coordCount = rawArray.length;
		for (int index = 0; index < coordCount; index += 2) {
			Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
			coordArrayList.add(c);
		}
		return coordArrayList;
	}
    //蛇体和果子位置的数组转换成简单的序列化的int数组
	private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
		int count = cvec.size();
		int[] rawArray = new int[count * 2];
		for (int index = 0; index < count; index++) {
			Coordinate c = cvec.get(index);
			rawArray[2 * index] = c.x;
			rawArray[2 * index + 1] = c.y;
		}
		return rawArray;
	}
}
