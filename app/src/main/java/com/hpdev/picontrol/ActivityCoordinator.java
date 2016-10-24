package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by harry on 14/10/2016.
 */

public class ActivityCoordinator {

    private static ArrayList<Pi> PiList;
    private static Resources Res;
    private static Activity Act;
    private static ArrayList<Command> CommandList;
    private static final String GET_IP_URL="http://harrydev.altervista.org/Tesi/getIP.php";
    private static final String KEY_USERID="user_id";
    private static final String KEY_PI_ID="pi_id";
    private static int UserID;
    private static HashMap<Integer,Integer> PinMap=null;
    private static final int PIN_TOTAL=26;




    protected static void initIstance(Resources res, Activity activity, int userID){
        PiList=new ArrayList<Pi>();
        CommandList=new ArrayList<Command>();
        Res=res;
        Act=activity;
        UserID=userID;
    }

    protected static void addPi(Pi pi){
        PiList.add(pi);
    }

    protected static ArrayList<Pi> getPiList(){
        return PiList;
    }

    protected static String getPiName(int index){
        return PiList.get(index).getPiName();
    }

    protected static Pi getPi(int index){
        return PiList.get(index);
    }


    protected static String getPiIP(int index){
        return PiList.get(index).getPiIP();
    }
    protected static ArrayList<XMLRoom> getRoomList(int index){
        return PiList.get(index).getRoomList();
    }

    protected static int getRoomListSize(int index){
        return PiList.get(index).getRoomList().size();
    }

    protected static boolean initRoomList(int index){

        XMLWrapper fileWrapper=null;
        String xml=null;
        try {
            if(CheckIP(index)){
                xml= (String) new RaspberryTCPClient(PiList.get(index).getPiIP(),Res,RaspberryTCPClient.TYPE_UPDATE_REQUEST).execute().get();
            } else {
                return false;
            }
            if(!xml.equals(RaspberryTCPClient.ERROR)){
                xml=xml.replaceAll(Res.getString(R.string.raspberryPkg),Act.getPackageName());
                XStream xstream = new XStream(new DomDriver());

                fileWrapper=(XMLWrapper)xstream.fromXML(xml);
                if(fileWrapper!=null){

                    ArrayList<XMLRoom> room=fileWrapper.getXMLRoomList();
                    Pi pi=PiList.get(index);
                    for(int i=0;i<room.size();i++){
                        XMLRoom Room=room.get(i);
                        Room.initRoomImg();
                        pi.addRoom(Room);
                    }
                   // replacePi(pi,index);
                }
                if(PinMap==null)
                    initMap();
                updateCommandList();
                return true;
            }else{
                return false;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    return false;
    }

    private static void initMap() {

        PinMap=new HashMap<Integer,Integer>();
        PinMap.put(0,R.drawable.img_gpio_00);
        PinMap.put(1,R.drawable.img_gpio_01);
        PinMap.put(2,R.drawable.img_gpio_02);
        PinMap.put(3,R.drawable.img_gpio_03);
        PinMap.put(4,R.drawable.img_gpio_04);
        PinMap.put(5,R.drawable.img_gpio_05);
        PinMap.put(6,R.drawable.img_gpio_06);
        PinMap.put(7,R.drawable.img_gpio_07);
        PinMap.put(8,R.drawable.img_gpio_08);
        PinMap.put(9,R.drawable.img_gpio_09);
        PinMap.put(10,R.drawable.img_gpio_10);
        PinMap.put(11,R.drawable.img_gpio_11);
        PinMap.put(12,R.drawable.img_gpio_12);
        PinMap.put(13,R.drawable.img_gpio_13);
        PinMap.put(14,R.drawable.img_gpio_14);
        PinMap.put(15,R.drawable.img_gpio_15);
        PinMap.put(16,R.drawable.img_gpio_16);
        PinMap.put(21,R.drawable.img_gpio_21);
        PinMap.put(22,R.drawable.img_gpio_22);
        PinMap.put(23,R.drawable.img_gpio_23);
        PinMap.put(24,R.drawable.img_gpio_24);
        PinMap.put(25,R.drawable.img_gpio_25);
        PinMap.put(26,R.drawable.img_gpio_26);
        PinMap.put(27,R.drawable.img_gpio_27);
        PinMap.put(28,R.drawable.img_gpio_28);
        PinMap.put(29,R.drawable.img_gpio_29);

    }

    public static HashMap<Integer, Integer> getPinMap() {
        return PinMap;
    }

    public static boolean CheckIP(int index) {

        boolean ret=true;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastUpdate=PiList.get(index).getPiLastUpdate();
        Date strDate=null;
        try {
            strDate = sdf.parse(lastUpdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Long ts = System.currentTimeMillis();


        int validity= Res.getInteger(R.integer.IPValidity);



        if(strDate!=null&&(ts-strDate.getTime())>validity){
            ret=false;
            requestIPUpdate(index);

        }
        return ret;
    }

    private static void requestIPUpdate(final int index) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_IP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(!response.equals("error")){

                            PiList.get(index).setPiIP(response);


                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_USERID, String.valueOf(UserID));
                params.put(KEY_PI_ID,PiList.get(index).getPiID());
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(Act);
        requestQueue.add(stringRequest);


    }


    protected static ArrayList<XMLRoom> updateRoomList(int index){
        Pi pi=PiList.get(index);
        pi.destroyRoomList();

        XMLWrapper fileWrapper=null;

        try {
            String xml= (String) new RaspberryTCPClient(PiList.get(index).getPiIP(),Res,RaspberryTCPClient.TYPE_UPDATE_REQUEST).execute().get();
            if(!xml.equals(RaspberryTCPClient.ERROR)){
                xml=xml.replaceAll(Res.getString(R.string.raspberryPkg),Act.getPackageName());
                XStream xstream = new XStream(new DomDriver());

                fileWrapper=(XMLWrapper)xstream.fromXML(xml);
                if(fileWrapper!=null){
                    ArrayList<XMLRoom> room=fileWrapper.getXMLRoomList();
                    for(int i=0;i<room.size();i++){
                        XMLRoom Room=room.get(i);
                        Room.initRoomImg();
                        pi.addRoom(Room);
                    }
                   // replacePi(pi,index);
                    updateCommandList();
                }
                return pi.getRoomList();
            }else{
                return null;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        return null;
    }

    protected static String[] getRoomListName(int index) {
        ArrayList<XMLRoom> list=PiList.get(index).getRoomList();
        String[] array=new String[list.size()];

        for(int i=0;i<list.size();i++)
            array[i]=list.get(i).getName();

        return array;
    }

    protected static void addRoomToPi(XMLRoom room,int index){
        Pi pi=PiList.get(index);
        pi.addRoom(room);
       // replacePi(pi,index);

    }

    protected static String[] getRoomUserStringArray(int piIndex, int roomIndex){
        ArrayList<XMLUser> list=PiList.get(piIndex).getRoomList().get(roomIndex).getUserList();
        String[] array=new String[list.size()];

        for(int i=0;i<list.size();i++)
            array[i]=list.get(i).getUserName();


        return array;

    }

    protected static ArrayList<XMLUser> getRoomUserList(int piIndex, int roomIndex){
      return PiList.get(piIndex).getRoomList().get(roomIndex).getUserList();

    }

    protected static XMLUser getXMLUser(int piIndex, int roomIndex,int userIndex){
        return PiList.get(piIndex).getRoomList().get(roomIndex).getUserList().get(userIndex);

    }

    protected static void addUserToRoom(XMLUser user,int piIndex,int roomIndex){
        Pi pi=PiList.get(piIndex);


        ArrayList<XMLRoom> list=pi.getRoomList();

        XMLRoom room=list.get(roomIndex);
        room.addUser(user);


       // list.remove(roomIndex);
        //list.add(roomIndex,room);

       // pi.destroyRoomList();
        //pi.setRoomList(list);

        //replacePi(pi,piIndex);


        updateCommandList();
    }

    private static void updateCommandList() {

        CommandList.clear();



        for (int q=0;q<PiList.size();q++){
            Pi pi=PiList.get(q);
            ArrayList<XMLRoom> rooms=pi.getRoomList();
            for(int i=0;i<rooms.size();i++){
                XMLRoom room=rooms.get(i);
                ArrayList<XMLUser> users=room.getUserList();

                for (int k=0;k<users.size();k++){
                    XMLUser user=users.get(k);

                    String[] commands=getCommand(user.getType());
                    for (int n=0;n<commands.length;n++){
                        Command cmd=new Command(pi.getPiName(),room.getName(),user.getUserName(),commands[n],n);
                        if(user.getType()==XMLUser.USER_TYPE_SENSOR_DH11)
                            cmd.setCommandNoUsername(Command.TYPE_NO_USERNAME);
                        CommandList.add(cmd);
                    }
                }
            }
        }
    }

    public static String[] getCommand(int type) {
        switch (type){
            case XMLUser.USER_TYPE_RELAY:
                return Res.getStringArray(R.array.command_array_relay);
            case XMLUser.USER_TYPE_SENSOR_DH11:
                return Res.getStringArray(R.array.command_array_dht11);
        }
        return null;
    }

    public static ArrayList<Command> getCommandList() {
        return CommandList;
    }
}
