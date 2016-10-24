package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddPiActivity extends AppCompatActivity implements View.OnClickListener{

    private FloatingActionButton fabAddPi;
    private TextView tvLoginKey;
    private SecureRandom random = new SecureRandom();
    private View snackView;
    private String uniqeKey;
    private EditText etAddPi;
    private String piName="Raspberry";
    private final String LOGIN_PI_URL="http://harrydev.altervista.org/Tesi/loginPi.php";
    private final String KEY_PI_ID="pi_id";
    private final String KEY_USER_ID="user_id";
    private final String KEY_PI_NAME="pi_name";
    private int userID;
    private final String newPiKEY="newPi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fabAddPi= (FloatingActionButton) findViewById(R.id.doneAddPi);
        fabAddPi.setOnClickListener(this);
        tvLoginKey= (TextView) findViewById(R.id.loginKey);
        tvLoginKey.setText(getLoginKey());

        etAddPi=(EditText)findViewById(R.id.addPiName);

        userID=getIntent().getIntExtra(KEY_USER_ID,0);
    }

    @Override
    public void onClick(View v) {
        snackView=v;
        if(isOnline())
            isPiOnline();
        else
            showToastMessage(getString(R.string.errorOffline));

    }

    private void isPiOnline() {
        String myString=etAddPi.getText().toString().trim();

        if(myString.length()>0){
            piName = myString.substring(0,1).toUpperCase() + myString.substring(1);}

            StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_PI_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response!=null){
                                addPiSuccess(response);

                            }
                            else{
                                showToastMessage(getString(R.string.addPi_Error));
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            showToastMessage(getString(R.string.addPi_Error));

                        }
                    }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put(KEY_USER_ID, String.valueOf(userID));
                    params.put(KEY_PI_ID, uniqeKey);
                    params.put(KEY_PI_NAME, piName);
                    return params;
                }

            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);


    }

    private void addPiSuccess(String response) {

        Intent intent=new Intent();

        intent.putExtra(newPiKEY,response);

       showToastMessage(getString(R.string.add_pi_success));
        setResult(Activity.RESULT_OK,intent);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1500);


    }


    private String getLoginKey() {
        uniqeKey=UUID.randomUUID().toString().substring(0,8);
        return uniqeKey;
    }


    void  showToastMessage(String message){
        if(snackView!=null)
             Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

    private boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
