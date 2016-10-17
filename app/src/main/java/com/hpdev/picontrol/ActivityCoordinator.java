package com.hpdev.picontrol;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by harry on 14/10/2016.
 */

public class ActivityCoordinator {

    private static ArrayList<Pi> PiList;
    private static Resources Res;
    private static Activity Act;
    private static ArrayList<Command> CommandList;




    protected static void initIstance(Resources res, Activity activity){
        PiList=new ArrayList<Pi>();
        CommandList=new ArrayList<Command>();
        Res=res;
        Act=activity;
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

        try {
            String xml= (String) new RaspberryTCPClient(PiList.get(index).getPiIP(),Res,RaspberryTCPClient.TYPE_UPDATE_REQUEST).execute().get();
            if(xml!=null){
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

    protected static ArrayList<XMLRoom> updateRoomList(int index){
        Pi pi=PiList.get(index);
        pi.destroyRoomList();

        XMLWrapper fileWrapper=null;

        try {
            String xml= (String) new RaspberryTCPClient(PiList.get(index).getPiIP(),Res,RaspberryTCPClient.TYPE_UPDATE_REQUEST).execute().get();
            if(xml!=null){
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

    protected static String[] getRoomUserList(int piIndex,int roomIndex){
        ArrayList<XMLUser> list=PiList.get(piIndex).getRoomList().get(roomIndex).getUserList();
        String[] array=new String[list.size()];

        for(int i=0;i<list.size();i++)
            array[i]=list.get(i).getUserName();


        return array;

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
                        CommandList.add(new Command(pi.getPiName(),room.getName(),user.getUserName(),commands[n],n));
                    }
                }
            }
        }
    }

    private static String[] getCommand(int type) {
        switch (type){
            case XMLUser.USER_TYPE_RELAY:
                return new String[]{"Accendi","Spegni","Accendi Normalmente Chiuso","Spegni Normalmente Chiuso"};
            case XMLUser.USER_TYPE_SENSOR_DH11:
                return new String[]{"Temperatura","Umidità","Caldo"};
        }
        return null;
    }

    public static ArrayList<Command> getCommandList() {
        return CommandList;
    }
}
