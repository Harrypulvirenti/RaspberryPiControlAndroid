package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class ViewRoomActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String KEY_ROOM="myRoom";


    private FloatingActionButton fabAddUser;
    private final static int REQUEST_ADD_USER=4123;
    private int MyPi;
    private int MyRoom;
    private String roomName;
    private final static String KEY_PI="MyPi";
    private static final String KEY_ROOM_POS="Room_Pos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        roomName=getIntent().getStringExtra(KEY_ROOM);
        getSupportActionBar().setTitle(roomName);
        Intent intent=getIntent();
        MyPi=intent.getIntExtra(KEY_PI,0);
        MyRoom=intent.getIntExtra(KEY_ROOM_POS,0);


        fabAddUser = (FloatingActionButton) findViewById(R.id.fabAddUser);
        fabAddUser.setOnClickListener(this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==REQUEST_ADD_USER&&resultCode== Activity.RESULT_OK){

        }
        if(requestCode==REQUEST_ADD_USER&&resultCode== Activity.RESULT_CANCELED){
            roomName=getIntent().getStringExtra(KEY_ROOM);
            getSupportActionBar().setTitle(roomName);
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.fabAddUser){
            Intent intent=new Intent(ViewRoomActivity.this,AddUserActivity.class);
            intent.putExtra(AddRoomActivity.KEY_PI, MyPi);
            intent.putExtra(KEY_ROOM,roomName);
            intent.putExtra(KEY_ROOM_POS,MyRoom);

            startActivityForResult(intent,REQUEST_ADD_USER);

        }
    }

}
