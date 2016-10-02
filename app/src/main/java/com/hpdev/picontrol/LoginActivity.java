package com.hpdev.picontrol;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
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


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener, View.OnFocusChangeListener{

    // UI references.
    private EditText etEmail;
    private EditText etPassword;
    private View mProgressView;
    private TextView registerButton;
    private CheckBox cbAutologin;
    static final int REGISTER_REQUEST = 12;
    private FloatingActionButton mEmailSignInButton;
    private TextView errorText;
    private boolean error=false;
    private final String KEY_EMAIL="email";
    private final String KEY_PASSWORD="password";
    private final String KEY_USERID="USER_ID";
    private final int LOGIN_ERROR_EMAIL=-250;
    private final int LOGIN_ERROR_PASSWORD=-350;
    private View snackView;
    private final String LOGIN_URL="http://harrydev.altervista.org/Tesi/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set up the login form.

        etEmail = (EditText) findViewById(R.id.loginEmail);
        etPassword = (EditText) findViewById(R.id.loginPassword);


        mEmailSignInButton = (FloatingActionButton) findViewById(R.id.loginButton);
        mEmailSignInButton.setOnClickListener(this);

        mProgressView = findViewById(R.id.login_progress);

        registerButton= (TextView) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        errorText= (TextView) findViewById(R.id.loginError);
        cbAutologin= (CheckBox) findViewById(R.id.autologin);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        final String email=sharedPref.getString(getString(R.string.spKeyLoginEmail), "");
        final String password=sharedPref.getString(getString(R.string.spKeyLoginPassword), "");

        if(sharedPref.getBoolean(getString(R.string.spKeyAutoLogin), false)){
            etEmail.setText(email);
            etPassword.setText(password);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loginUser(email,password);
                }
            }, 500);

        }else{
            etEmail.setText(email);
            cbAutologin.setChecked(false);
        }
    }



    private boolean isEmailValid(String email) {

            String myString=email.replaceAll(" ", "");

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
            }
        return error;
    }

    private boolean isPasswordValid(String password) {

        if (password.length()==0) {
            errorText.setText(getString(R.string.error_field_required));
            error=true;

        } else if(password.length()<5) {
            errorText.setText(getString(R.string.error_invalid_password));
            error=true;
        } else{
            errorText.setText("");
            etPassword.setText(password.trim());
            error=false;
        }
        return error;
    }


    private void showProgress(final boolean show) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mEmailSignInButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mEmailSignInButton.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEmailSignInButton.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

    }

    @Override
    public void onClick(View v) {


        if(v.getId()==R.id.loginButton){
            snackView=v;

            String email=etEmail.getText().toString();
            String password=etPassword.getText().toString();

            isPasswordValid(password);
            if(!error){
                loginUser(email,password);
            }

        }
        if(v.getId()==R.id.registerButton){
            Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
            startActivityForResult(intent,REGISTER_REQUEST);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REGISTER_REQUEST&&resultCode== Activity.RESULT_OK){
           etEmail.setText(data.getStringExtra(KEY_EMAIL));

        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if(v.getId()==R.id.loginEmail&&!hasFocus){
            isEmailValid(((EditText)v).getText().toString());
        }

        if(v.getId()==R.id.loginPassword&&!hasFocus){

            isPasswordValid(((EditText)v).getText().toString());

        }



    }

    private void loginUser(final String email, final String password){
        showProgress(true);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int resp=Integer.parseInt(response);
                        if(resp>0){

                            loginSuccess(resp,email,password);
                        }else{
                            showProgress(false);

                            switch (resp){
                                case LOGIN_ERROR_EMAIL:
                                    errorText.setText(getString(R.string.login_email_error));
                                    break;
                                case LOGIN_ERROR_PASSWORD:
                                    errorText.setText(getString(R.string.login_password_error));
                                    break;
                                default:
                                    showToastMessage(getString(R.string.server_error));
                                    break;}}

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false);
                        showToastMessage(getString(R.string.server_error));
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_PASSWORD,password);
                params.put(KEY_EMAIL, email);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loginSuccess(int userID, String email, String password) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(KEY_USERID,userID);

        if(cbAutologin.isChecked()){
            addSharedPref(email,password);
        }
        else{
            addSharedPref(email,null);
        }

        startActivity(intent);
        finish();
    }

    private void addSharedPref(String email, String password) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(password!=null){
            editor.putString(getString(R.string.spKeyLoginEmail), email);
            editor.putString(getString(R.string.spKeyLoginPassword), password);
            editor.putBoolean(getString(R.string.spKeyAutoLogin),true);
            editor.commit();
        }else{
            editor.putString(getString(R.string.spKeyLoginEmail), email);
            editor.putString(getString(R.string.spKeyLoginPassword), "");
            editor.putBoolean(getString(R.string.spKeyAutoLogin),false);
            editor.commit();
        }


    }


    void  showToastMessage(String message){
        Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

}
