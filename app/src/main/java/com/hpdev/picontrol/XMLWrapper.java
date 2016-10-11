package com.hpdev.picontrol;

import java.util.ArrayList;

/**
 * Created by harry on 09/10/2016.
 */
public class XMLWrapper {

    private String Loginkey;
    private ArrayList<XMLRoom> RoomList;


    public XMLWrapper(String loginkey) {
        Loginkey = loginkey;
        RoomList =new ArrayList<XMLRoom>();
    }

    public String getLoginkey() {
        return Loginkey;
    }

    public void setXMLRoomList(ArrayList<XMLRoom> room){
        RoomList=room;
    }

    public ArrayList<XMLRoom> getXMLRoomList() {
        return RoomList;
    }
}
