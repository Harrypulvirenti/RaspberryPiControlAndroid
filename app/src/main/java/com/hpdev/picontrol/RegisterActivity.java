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
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText etName;
    private EditText etSurname;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etSecondPassword;
    private TextView errorText;
    private boolean error=false;
    private final String REGISTER_URL="http://harrydev.altervista.org/Tesi/registerUser.php";
    private final String CHECK_EMAIL_URL="http://harrydev.altervista.org/Tesi/checkEmail.php";
    private final String KEY_NAME="name";
    private final String KEY_SURNAME="surname";
    private final String KEY_EMAIL="email";
    private final String KEY_PASSWORD="password";
    private View snackView;
    private final int SERVER_ERROR=-1;
    private final int SERVER_SUCCESS=1;
    private final int EMAIL_VALID=0;
    private boolean emailValid=false;


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
                snackView=view;
                String myString=etSecondPassword.getText().toString();
                String firstPassword=etPassword.getText().toString();
                if(!myString.equals(firstPassword)) {
                    errorText.setText(getString(R.string.error_incorrect_password));
                    error=true;
                } else{
                    etSecondPassword.setText(myString.trim());
                    errorText.setText("");
                    error=false;
                }
                if(!error){
                    registerUser();
                }
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
        etPassword.setOnFocusChangeListener(this);
        errorText= (TextView) findViewById(R.id.registerError);


    }

    private void registerUser(){




        final String name = etName.getText().toString();
        final String surname = etSurname.getText().toString();
        final String password = etPassword.getText().toString();
        final String email = etEmail.getText().toString();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(Integer.parseInt(response)==SERVER_SUCCESS){
                            closeActivity();
                        }else{
                            showToastMessage(getString(R.string.server_error));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToastMessage(getString(R.string.server_error));
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_NAME,name);
                params.put(KEY_SURNAME,surname);
                params.put(KEY_PASSWORD,password);
                params.put(KEY_EMAIL, email);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void closeActivity() {
        showToastMessage(getString(R.string.registration_success));
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_EMAIL, etEmail.getText().toString());
        setResult(Activity.RESULT_OK,resultIntent);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1500);

    }

    private void requestCheckEmail(final String email){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECK_EMAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       if(Integer.parseInt(response)==EMAIL_VALID){
                           checkEmail(true);

                       }else{
                           checkEmail(false);

                       }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //checkEmail(false);

                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_EMAIL, email);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }

    private void checkEmail(boolean b) {
        emailValid=b;
        if(!b){
            error=true;
            errorText.setText(getString(R.string.error_email_used));
        }else{
            error=false;
            errorText.setText("");

        }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getId()==R.id.addName&&!hasFocus){

            String myString=etName.getText().toString();
            if(myString.length()>0){
                String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
                etName.setText(upperString.trim());
                errorText.setText("");
                error=false;
            } else{
                errorText.setText(getString(R.string.error_field_required));
                error=true;
            }

        }

        if(v.getId()==R.id.addSurname&&!hasFocus){

            String myString=etSurname.getText().toString();
            if(myString.length()>0){
                String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
                etSurname.setText(upperString.trim());
                errorText.setText("");
                error=false;
            } else{
                errorText.setText(getString(R.string.error_field_required));
                error=true;
            }

        }

        if(v.getId()==R.id.addEmail&&!hasFocus){
            String k=etEmail.getText().toString();
            String myString=k.replaceAll(" ", "");

            if (myString.length()==0) {
                errorText.setText(getString(R.string.error_field_required));
                error=true;

            } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(myString).matches()) {
                errorText.setText(getString(R.string.error_invalid_email));
                error=true;
            } else{
                errorText.setText("");
                etEmail.setText(myString);
                error=false;
                requestCheckEmail(myString);
            }}

        if(v.getId()==R.id.addPass&&!hasFocus){

            String myString=etPassword.getText().toString();
            if (myString.length()==0) {
                errorText.setText(getString(R.string.error_field_required));
                error=true;

            } else if(myString.length()<5) {
                errorText.setText(getString(R.string.error_invalid_password));
                error=true;
            } else{
                etPassword.setText(myString.trim());
                errorText.setText("");
                error=false;
            }

        }

        if(v.getId()==R.id.addPassSecond&&!hasFocus){

            String myString=etSecondPassword.getText().toString();
            String firstPassword=etPassword.getText().toString();
            if (myString.length()==0) {
                errorText.setText(getString(R.string.error_incorrect_password));
                error=true;

            } else if(!myString.equals(firstPassword)) {
                errorText.setText(getString(R.string.error_incorrect_password));
                error=true;
            } else{
                etSecondPassword.setText(myString.trim());
                errorText.setText("");
                error=false;
            }

        }



    }


    void  showToastMessage(String message){
        Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

}
