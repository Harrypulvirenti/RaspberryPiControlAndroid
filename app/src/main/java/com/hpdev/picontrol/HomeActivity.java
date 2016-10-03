package com.hpdev.picontrol;

import android.Manifest;
import android.animation.Animator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private String IP;
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
    private final String KEY_USERID="USER_ID";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView drawerEmail=null;
    private TextView drawerUsername;
    private int userID;
    private final String USER_DATA_URL="http://harrydev.altervista.org/Tesi/getUserData.php";
    private ArrayList<Pi> piList=null;
    private final String KEY_PI_ID="pi_id";
    private final String KEY_PI_NAME="pi_name";
    private final String KEY_PI_IP="pi_ip";
    private final String KEY_PI_LAST_UPDATE="pi_last_update";
    private final String KEY_USER_NAME="name";
    private final String KEY_USER_SURNAME="surname";
    private final String KEY_USER_EMAIL="email";


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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
                                updateDrawerUser(username,email);
                            }
                        } catch (JSONException e) {
                            updateDrawerUser(username,email);
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

    private void updateDrawerUser(String username, String email) {

        Log.v("HEREeeeeeeeeeeeee",username+" "+email);
        drawerUsername.setText(username);
        drawerEmail.setText(email);

    }

    private void addPi(Pi pi) {
        piList.add(pi);
    }


    @Override
    public void onClick(View v) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED&&v.getId()==R.id.fab&&!speakIsOpen) {



            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    0);

        } else if(v.getId()==R.id.fab&&!speakIsOpen){
            snackView=v;
            CheckIP();
            StartVoiceRecognition();
            showSpeakCard();
        } else if(v.getId()==R.id.fab&&speakIsOpen&&!isSpeechOnListening){
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
        Log.v("HEREEEEE","Client Start");
        new TCPClient().execute();

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

        //Checking if the item is in checked state or not, if not make it in checked state
        if(item.isChecked()) item.setChecked(false);
        else item.setChecked(true);

        //Closing drawer on item click
        drawerLayout.closeDrawers();

        //Check to see which item was being clicked and perform appropriate action
        switch (item.getItemId()){


            //Replacing the main content with ContentFragment Which is our Inbox View;
            case R.id.inbox:
                Toast.makeText(getApplicationContext(),"Inbox Selected",Toast.LENGTH_SHORT).show();
                ContentFragment fragment = new ContentFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.piList_Frame,fragment);
                fragmentTransaction.commit();
                return true;

            // For rest of the options we just show a toast on click

            case R.id.starred:
                Toast.makeText(getApplicationContext(),"Stared Selected",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.trash:
                Toast.makeText(getApplicationContext(),"Trash Selected",Toast.LENGTH_SHORT).show();
                return true;
            default:
                Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                return true;

        }
    }


    private class TCPClient extends AsyncTask
    {

        @Override
        protected Object doInBackground(Object[] params) {
            String modifiedSentence = null;
            BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            Socket clientSocket = null;
            DataOutputStream outToServer = null;
            BufferedReader inFromServer = null;
            try {
                clientSocket = new Socket(IP, Res.getInteger(R.integer.PortNumber));
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

    private void CheckIP() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String defaultIP = getString(R.string.defaultIP);
        IP = sharedPref.getString(getString(R.string.spIPKey), defaultIP);
        Long ts = System.currentTimeMillis();
        Long IPts=sharedPref.getLong(getString(R.string.spIPValidity),ts);

        int validity= Res.getInteger(R.integer.IPValidity);


        if(IP.equals(defaultIP)||(ts-IPts)>validity){
            try {
                IP=(String)new GetIP().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } finally {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.spIPKey), IP);
                editor.putLong(getString(R.string.spIPValidity),ts);
                editor.commit();
            }}

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

    private class GetIP extends AsyncTask {


        private String s=null;
        @Override
        protected String doInBackground(Object[] params) {

            URL url = null;
            HttpURLConnection mUrlConnection = null;
            InputStream is = null;

            try {
                url = new URL(getString(R.string.getIPURL));
                mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                is = new BufferedInputStream(mUrlConnection.getInputStream());
                s = readStream(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return s;
        }

        private String readStream(InputStream is) throws IOException {


            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return sb.toString();


        }

    }


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


    private class Pi {

        private String piID;
        private String piName;
        private String piIP;
        private String piLastUpdate;

        public Pi(String piID, String piName, String piIP, String piLastUpdate) {
            this.piID = piID;
            this.piName = piName;
            this.piIP = piIP;
            this.piLastUpdate = piLastUpdate;
        }

        public String getPiID() {
            return piID;
        }

        public String getPiName() {
            return piName;
        }

        public String getPiIP() {
            return piIP;
        }

        public String getPiLastUpdate() {
            return piLastUpdate;
        }

        public void setPiID(String piID) {
            this.piID = piID;
        }

        public void setPiName(String piName) {
            this.piName = piName;
        }

        public void setPiIP(String piIP) {
            this.piIP = piIP;
        }

        public void setPiLastUpdate(String piLastUpdate) {
            this.piLastUpdate = piLastUpdate;
        }
    }
}
