package com.hpdev.picontrol;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

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

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private String IP;
    private  static  final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
    private Resources Res;
    private View snackView;
    private int CommandResult=-1;
    private TextToSpeech tts=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Res=getResources();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        snackView=v;
        CheckIP();
        StartVoiceRecognition();

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
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.SpeakString));
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,Res.getInteger(R.integer.noOfMatches));
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,getString(R.string.RecognizerLanguage));
            startActivityForResult(intent,VOICE_RECOGNITION_REQUEST_CODE);
        }
        catch(ActivityNotFoundException e)
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,   Uri.parse(getString(R.string.NoRecognizerActivityLink)));
            startActivity(browserIntent);

        }

    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == RESULT_OK){
            ArrayList<String> textMatchlist = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String[] CommandList=Res.getStringArray(R.array.commandArray);

            if (!textMatchlist.isEmpty()){

                for(int i=0;textMatchlist.size()>i;i++){
                    String text=textMatchlist.get(i).toLowerCase();
                    for(int j=0;CommandList.length>j;j++){
                        if(text.contains(CommandList[j].toLowerCase())){
                            CommandResult=j;
                            Log.v("HEREEEEEEEEEEEE",text);

                            break;
                        }
                    }
                    if(CommandResult!=-1){
                        SendCommand();
                        break;}
                }
            }
        }
        else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
            showToastMessage("Audio Error");

        }
        else if ((resultCode == RecognizerIntent.RESULT_CLIENT_ERROR)){
            showToastMessage("Client Error");

        }
        else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
            showToastMessage("Network Error");
        }
        else if (resultCode == RecognizerIntent.RESULT_NO_MATCH){
            showToastMessage("No Match");
        }
        else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
            showToastMessage("Server Error");
        }
        super.onActivityResult(requestCode, resultCode, data);

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


}
