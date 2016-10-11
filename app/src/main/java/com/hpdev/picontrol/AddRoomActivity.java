package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.ExecutionException;

public class AddRoomActivity extends AppCompatActivity implements View.OnClickListener {

    private View snackView;
    private FloatingActionButton fabDoneAddRoom;
    private EditText etRoomName;
    private String roomName=null;
    public final static String KEY_PI_IP="MyPi_IP";
    private final static String KEY_ROOM="myRoom";


    private String myPi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabDoneAddRoom = (FloatingActionButton) findViewById(R.id.doneAddRoom);
        fabDoneAddRoom.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etRoomName=(EditText)findViewById(R.id.addRoomName);
        myPi=getIntent().getStringExtra(KEY_PI_IP);



    }




    void  showToastMessage(String message){
        Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.doneAddRoom){
            snackView=v;
            String myString=etRoomName.getText().toString();

            if(myString.length()>0){
                roomName = myString.substring(0,1).toUpperCase() + myString.substring(1);
                addRoomToPi();
            }else{
                showToastMessage(getString(R.string.noNameRoom));

            }
        }
    }

    private void addRoomToPi() {
        Integer ret=-1;
        try {
            ret= (Integer) new RaspberryTCPClient(myPi,getResources(),RaspberryTCPClient.TYPE_ADD_ROOM,roomName).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(ret==RaspberryTCPClient.OPERATION_DONE){

            showToastMessage(getString(R.string.roomAdded));

            Intent data=new Intent();
            data.putExtra(KEY_ROOM,roomName);
            setResult(Activity.RESULT_OK,data);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1500);
        }else{
            showToastMessage(getString(R.string.addRoomError));
        }

    }
}
