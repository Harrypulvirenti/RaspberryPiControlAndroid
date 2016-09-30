package com.hpdev.picontrol;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText etName;
    private EditText etSurname;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etSecondPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.doneRegister);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        etName= (EditText) findViewById(R.id.addName);
        etSurname= (EditText) findViewById(R.id.addSurname);
        etEmail= (EditText) findViewById(R.id.addEmail);
        etPassword= (EditText) findViewById(R.id.addPass);
        etSecondPassword= (EditText) findViewById(R.id.addPassSecond);
        etName.setOnFocusChangeListener(this);
        etSurname.setOnFocusChangeListener(this);
        etEmail.setOnFocusChangeListener(this);
        etSecondPassword.setOnFocusChangeListener(this);


    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getId()==R.id.addName&&!hasFocus){
            EditText et=(EditText)v;
            String myString=et.getText().toString();
            if(myString.length()>0){
                String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
                etName.setText(upperString);
            }

        }

        if(v.getId()==R.id.addSurname&&!hasFocus){
            EditText et=(EditText)v;
            String myString=et.getText().toString();
            if(myString.length()>0){
                String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
                etSurname.setText(upperString);
            }

        }



    }



}
