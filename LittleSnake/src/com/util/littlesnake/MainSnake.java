package com.util.littlesnake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainSnake extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_snake);
        initialstart();//初始化      
    }
     public void initialstart(){
    	 ImageButton imageButton = (ImageButton)this.findViewById(R.id.ImageButton1);
    	 Button button1 = (Button)this.findViewById(R.id.Button1);
    	 Button button3 = (Button)this.findViewById(R.id.Button3);
    	 Button button5 = (Button)this.findViewById(R.id.Button5);
         button1.setText("作者信息");
         button3.setText("退出");
         button5.setText("游戏说明");
         imageButton.setImageResource(R.drawable.star1);
         //按钮注册到各自的监听器
         imageButton.setOnClickListener(new View.OnClickListener() {
                  public void onClick(View view) {
                	  Intent intent=new Intent();
                      intent.setClass(MainSnake.this, GAME.class);
                      startActivity(intent);//跳转到Game界面
                     MainSnake.this.finish();
                    }
                });
          button1.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View view) {
               //setContentView(R.layout.author);
                	 Intent intent=new Intent();
                   intent.setClass( MainSnake.this,AuthorView.class);
                   startActivity(intent);//跳转到作者信息界面
                  MainSnake.this.finish();//注销跳转之前的界面
    
    }
  });
          button3.setOnClickListener(new View.OnClickListener() {
              public void onClick(View view) {
                      MainSnake.this.finish();//退出应用程序
                }
            });
          button5.setOnClickListener(new View.OnClickListener() {
              public void onClick(View view) {
            	  Intent intent=new Intent();
                  intent.setClass( MainSnake.this,Help.class);
                  startActivity(intent);//跳转到游戏说明界面
                 MainSnake.this.finish();
                }
            });
     }
}
