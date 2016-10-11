package com.hpdev.picontrol;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private Resources Res;
    private View snackView;
    private int CommandResult=-1;
    private TextToSpeech tts=null;
    private View speakLayout;
    private SupportAnimator animator_reverse;
    private boolean speakIsOpen=false;
    private SpeechRecognizer speechReco;
    private boolean isSpeechOnListening=false;
    private TextView commandResultText;
    private String speakStringInit;
    private final String KEY_USERID="user_id";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView drawerEmail=null;
    private TextView drawerUsername;
    private int userID;
    private final String USER_DATA_URL="http://harrydev.altervista.org/Tesi/getUserData.php";
    private final String GET_IP_URL="http://harrydev.altervista.org/Tesi/getIP.php";
    private ArrayList<Pi> piList=null;
    private final String KEY_PI_ID="pi_id";
    private final String KEY_PI_NAME="pi_name";
    private final String KEY_PI_IP="pi_ip";
    private final String KEY_PI_LAST_UPDATE="pi_last_update";
    private final String KEY_USER_NAME="name";
    private final String KEY_USER_SURNAME="surname";
    private final String KEY_USER_EMAIL="email";
    private final int SETTING_ID=2000;
    private final int LOGOUT_ID=3000;
    private final int ADD_PI_ID=1000;
    private final int requestAddPi=23132;
    private final String newPiKEY="newPi";
    private int selectedPi=-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID=getIntent().getIntExtra(KEY_USERID,0);
        Res=getResources();

        speakLayout = findViewById(R.id.speakLayout);
        speakLayout.setVisibility(View.INVISIBLE);

        Button hideButton= (Button) findViewById(R.id.hide_button);
        hideButton.setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabStartListening);
        fab.setOnClickListener(this);

        speechReco = SpeechRecognizer.createSpeechRecognizer(this);
        speechReco.setRecognitionListener(new recognizerListener());

        commandResultText=(TextView) findViewById(R.id.commandResultText);
        speakStringInit=getString(R.string.SpeakString);
        commandResultText.setText(speakStringInit);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.inflateHeaderView(R.layout.header);
        drawerEmail=(TextView)headerView.findViewById(R.id.drawerEmail);
        drawerUsername=(TextView)headerView.findViewById(R.id.drawerUsername);
        piList=new ArrayList<Pi>();
        requestUserData(userID);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void requestUserData(final int id) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, USER_DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsArray=null;
                        JSONObject obj=null;
                        String username=null;
                        String email=null;
                        try {
                            jsArray=new JSONArray(response);
                            obj=null;
                            username=null;
                            email=null;
                            String piID=null;
                            String piName=null;
                            String piIP=null;
                            String piLastUpdate=null;
                            for(int i=0;i<jsArray.length();i++){
                                obj=jsArray.getJSONObject(i);
                                username=obj.getString(KEY_USER_NAME)+" "+obj.getString(KEY_USER_SURNAME);
                                email=obj.getString(KEY_USER_EMAIL);
                                piID=obj.getString(KEY_PI_ID);
                                piName=obj.getString(KEY_PI_NAME);
                                piIP=obj.getString(KEY_PI_IP);
                                piLastUpdate=obj.getString(KEY_PI_LAST_UPDATE);
                                addPi(new Pi(piID,piName,piIP,piLastUpdate));
                            }
                        } catch (JSONException e) {

                        }

                        updateDrawer(username,email);
                        if(piList.size()>0){
                            selectFragment(0);
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
                params.put(KEY_USERID, String.valueOf(id));
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void updateDrawer(String username, String email) {
        Menu drawerMenu= navigationView.getMenu();
        drawerUsername.setText(username);
        drawerEmail.setText(email);
        MenuItem mItem= null;


        if(piList.size()>0){

            for (int i=0;i<piList.size();i++){
                mItem=drawerMenu.add(Menu.NONE,i,i,piList.get(i).getPiName());
                mItem.setIcon(R.drawable.ic_drawer_rasp);

            }}

        mItem=drawerMenu.add(Menu.NONE,ADD_PI_ID,ADD_PI_ID,getString(R.string.drawerAddPi));
        mItem.setIcon(R.drawable.ic_plus);
        mItem=drawerMenu.add(Menu.NONE,SETTING_ID,SETTING_ID,getString(R.string.drawerSettings));
        mItem.setIcon(R.drawable.ic_settings);
        mItem=drawerMenu.add(Menu.NONE,LOGOUT_ID,LOGOUT_ID,getString(R.string.drawerLogout));
        mItem.setIcon(R.drawable.ic_logout);

    }


    private void addPi(Pi pi) {
        piList.add(pi);
    }


    @Override
    public void onClick(View v) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED&&v.getId()==R.id.fabStartListening&&!speakIsOpen) {



            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    0);

        } else if(v.getId()==R.id.fabStartListening&&!speakIsOpen){
            snackView=v;
            CheckIP(piList.get(selectedPi));
            StartVoiceRecognition();
            showSpeakCard();
        } else if(v.getId()==R.id.fabStartListening&&speakIsOpen&&!isSpeechOnListening){
            StartVoiceRecognition();
        }
        if(v.getId()==R.id.hide_button){
            speechReco.stopListening();
            hideButton();
        }


    }

    private void hideButton() {
        animator_reverse.addListener(new SupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                speakLayout.setVisibility(View.INVISIBLE);
                speakIsOpen=false;
                commandResultText.setText(speakStringInit);
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        animator_reverse.start();
    }


    private void showSpeakCard() {
        // get the center for the clipping circle
        int cx =  speakLayout.getRight();
        int cy = speakLayout.getTop();

        // get the final radius for the clipping circle
        int dx = speakLayout.getWidth();
        int dy = speakLayout.getHeight();
        float finalRadius = (float) Math.hypot(dx, dy);

        // Android native animator

        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(speakLayout, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(400);
        animator_reverse=animator.reverse();
        speakIsOpen=true;
        speakLayout.setVisibility(View.VISIBLE);
        animator.start();

    }

    private void SendCommand() {
        new TCPClient(piList.get(selectedPi)).execute();

    }
    private void serverResponse(int result){

        if(result>0){
            switch (CommandResult){
                case 2:
                    startTTS("La temperatura è di "+String.valueOf(result)+" gradi");
                break;
            }}


    }

    private void startTTS(final String s) {
        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ITALIAN);}
                tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,null);
            }
        });



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //Closing drawer on item click
        drawerLayout.closeDrawers();


        switch (item.getItemId()){
            case SETTING_ID:

                return true;
            case LOGOUT_ID:
                logout();
                return true;
            case ADD_PI_ID:
                addNewRaspberry();
                return true;

            default:
                if(selectedPi!=item.getItemId()){
                    selectedPi=item.getItemId();
                    selectFragment(selectedPi);}
                return true;
        }


    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(getString(R.string.spKeyAutoLogin),false);
        editor.commit();

        Intent intent=new Intent(this,LoginActivity.class);

        startActivity(intent);
        finish();


    }

    private void addNewRaspberry() {

        Intent intent=new Intent(HomeActivity.this,AddPiActivity.class);
        intent.putExtra(KEY_USERID,userID);

        startActivityForResult(intent,requestAddPi);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==requestAddPi&&resultCode== Activity.RESULT_OK){
            addDrawerPi(data.getStringExtra(newPiKEY));

        }

    }

    private void addDrawerPi(String stringExtra) {
        Menu drawerMenu= navigationView.getMenu();
        MenuItem mItem= null;


        try {
            JSONArray jsArray=new JSONArray(stringExtra);
            JSONObject obj=jsArray.getJSONObject(0);
            String piID=obj.getString(KEY_PI_ID);
            String piName=obj.getString(KEY_PI_NAME);
            String piIP=obj.getString(KEY_PI_IP);
            String piLastUpdate=obj.getString(KEY_PI_LAST_UPDATE);
            addPi(new Pi(piID,piName,piIP,piLastUpdate));

        } catch (JSONException e) {

        }

        int i=piList.size();

        mItem=drawerMenu.add(Menu.NONE,i,i,piList.get(i-1).getPiName());
        mItem.setIcon(R.drawable.ic_drawer_rasp);


    }


    private void selectFragment(int selected){

        PiRoomFragment fragment = new PiRoomFragment();
        Bundle bnd=new Bundle();
        bnd.putParcelable(PiRoomFragment.KEY_PI,piList.get(selected));
        fragment.setArguments(bnd);
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.piList_Frame,fragment);
        fragmentTransaction.commit();
    }


    private class TCPClient extends AsyncTask
    {
        private Pi pi;

        public TCPClient(Pi pi) {
            this.pi=pi;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            String modifiedSentence = null;
            BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            Socket clientSocket = null;
            DataOutputStream outToServer = null;
            BufferedReader inFromServer = null;
            try {
                clientSocket = new Socket(this.pi.getPiIP(), Res.getInteger(R.integer.PortNumber));
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outToServer.writeBytes(String.valueOf(CommandResult)+'\n');
                modifiedSentence = inFromServer.readLine();
                serverResponse(Integer.parseInt(modifiedSentence));
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void StartVoiceRecognition() {

        try{
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getClass().getPackage().getName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,Res.getInteger(R.integer.noOfMatches));
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,getString(R.string.RecognizerLanguage));
            isSpeechOnListening=true;
            speechReco.startListening(intent);
        }
        catch(ActivityNotFoundException e)
        {
            isSpeechOnListening=false;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,   Uri.parse(getString(R.string.NoRecognizerActivityLink)));
            startActivity(browserIntent);

        }

    }


    void  showToastMessage(String message){
        Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

    private void CheckIP(Pi pi) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date strDate=null;
        try {
            strDate = sdf.parse(pi.getPiLastUpdate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Long ts = System.currentTimeMillis();


        int validity= Res.getInteger(R.integer.IPValidity);



        if(strDate!=null&&(ts-strDate.getTime())>validity){
           requestIPUpdate(userID,pi);

        }

    }

    private void requestIPUpdate(final int id,final Pi pi) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_IP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(Integer.parseInt(response)>0){

                            Pi newPi=piList.get(selectedPi);
                            newPi.setPiIP(response);
                            piList.remove(selectedPi);
                            piList.add(selectedPi,newPi);
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
                params.put(KEY_USERID, String.valueOf(id));
                params.put(KEY_PI_ID,pi.getPiID());
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);



    }






    /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/




    private class recognizerListener implements RecognitionListener{


        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

            switch (error){
                case 6:

                    commandResultText.setText(getString(R.string.SpeakCommandNotFoundError));
                    break;
                case 7:
                    commandResultText.setText(getString(R.string.SpeakCommandNotFoundError));
                    break;
                default:

                    Log.d("hereeeeeeeeeee",  "error " +  error);
                    break;
            }
            isSpeechOnListening=false;
        }

        @Override
        public void onResults(Bundle results) {

            String[] CommandList=Res.getStringArray(R.array.commandArray);

            ArrayList<String> textMatchlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            CommandResult=-1;


            if (!textMatchlist.isEmpty()){

                for(int i=0;textMatchlist.size()>i;i++){
                    String text=textMatchlist.get(i).toLowerCase();
                    for(int j=0;CommandList.length>j;j++){
                        if(text.contains(CommandList[j].toLowerCase())){
                            CommandResult=j;
                            break;
                        }
                    }
                    if(CommandResult!=-1){
                        SendCommand();
                        isSpeechOnListening=false;
                        commandResultText.setText(CommandList[CommandResult]);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideButton();
                            }
                        }, 1500);
                        break;}
                }
            } else{
                commandResultText.setText(getString(R.string.SpeakCommandNotFoundError));
                isSpeechOnListening=false;

            }

            if(CommandResult==-1){
                commandResultText.setText(getString(R.string.SpeakCommandNotFoundError));
                isSpeechOnListening=false;
                }

           // speakCommand.setText("results: "+String.valueOf(data.size()));
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechReco.destroy();
    }


}
