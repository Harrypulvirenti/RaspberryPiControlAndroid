package com.hpdev.picontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class ViewUserActivity extends AppCompatActivity {

    private int MyPi;
    private int MyRoom;
    private int MyUser;
    private String roomName;
    private final static String KEY_PI="MyPi";
    private static final String KEY_ROOM_POS="Room_Pos";
    private static final String KEY_USER="myUser";
    private final static String KEY_ROOM="myRoom";
    private XMLUser User;
    private RelativeLayout rlUserView;
    private ImageView imGPIOBase;
    private TextView tvCommandList;
    private static Map<Integer,Integer> PinMap=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();

        MyPi=intent.getIntExtra(KEY_PI,0);
        MyRoom=intent.getIntExtra(KEY_ROOM_POS,0);
        MyUser=intent.getIntExtra(KEY_USER,0);
        User=ActivityCoordinator.getXMLUser(MyPi,MyRoom,MyUser);
        getSupportActionBar().setTitle(User.getUserName());
        roomName=intent.getStringExtra(KEY_ROOM);
        rlUserView= (RelativeLayout) findViewById(R.id.rlUserView);
        imGPIOBase=(ImageView)findViewById(R.id.imgGpio_Base);
        tvCommandList=(TextView) findViewById(R.id.tvCommandList);

        PinMap=ActivityCoordinator.getPinMap();

        ArrayList<XMLPin> list=User.getPinList();

        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) imGPIOBase.getLayoutParams();

        for (int i=0;i<list.size();i++){
            ImageView pin=new ImageView(this);
            pin.setLayoutParams(params);
            pin.setImageResource(PinMap.get(list.get(i).getPinNumber()));
            rlUserView.addView(pin);
        }

        String[] command=ActivityCoordinator.getCommand(User.getType());
        String text="";
        boolean noName=false;
        if(User.getType()==XMLUser.USER_TYPE_SENSOR_DH11)
            noName=true;

        for (int i=0;i<command.length;i++){

            if(noName){
                text+=roomName+" + "+command[i]+"\n";
            }else {
                text+=roomName+" + "+command[i]+" + "+User.getUserName()+"\n";
            }
        }

        text=text.substring(0, text.length()-1);
        tvCommandList.setText(text);

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            Intent intent=new Intent();
            intent.putExtra(KEY_ROOM,roomName);
            setResult(RESULT_CANCELED,intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
