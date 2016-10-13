package com.hpdev.picontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ViewRoomActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String KEY_PI_IP = "MyPi_IP";
    private final static String KEY_USER_LIST="myUser_List";
    private final static String KEY_ROOM="myRoom";

    private FloatingActionButton fabAddUser;
    private final static int REQUEST_ADD_USER=4123;
    private String myPi;
    private String[] userList;
    private String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        roomName=getIntent().getStringExtra(KEY_ROOM);
        getSupportActionBar().setTitle(roomName);
        myPi = getIntent().getStringExtra(KEY_PI_IP);
        userList=getIntent().getStringArrayExtra(KEY_USER_LIST);


        fabAddUser = (FloatingActionButton) findViewById(R.id.fabAddUser);
        fabAddUser.setOnClickListener(this);





    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.fabAddUser){
            Intent intent=new Intent(ViewRoomActivity.this,AddUserActivity.class);
            intent.putExtra(AddRoomActivity.KEY_PI_IP, myPi);
            intent.putExtra(KEY_ROOM,roomName);
            intent.putExtra(KEY_USER_LIST,userList);

            startActivityForResult(intent,REQUEST_ADD_USER);

        }
    }
}
