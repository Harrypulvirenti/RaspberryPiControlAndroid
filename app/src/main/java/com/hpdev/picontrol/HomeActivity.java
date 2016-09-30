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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;


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
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private String IP;
    private  static  final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
}
