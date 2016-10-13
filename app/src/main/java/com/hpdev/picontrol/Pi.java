package com.hpdev.picontrol;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by harry on 04/10/2016.
 */

@SuppressLint("ParcelCreator")
public class Pi implements Parcelable {

    private String piID;
    private String piName;
    private String piIP;
    private String piLastUpdate;
    private ArrayList<XMLRoom> roomList;

    public Pi(String piID, String piName, String piIP, String piLastUpdate) {
        this.piID = piID;
        this.piName = piName;
        this.piIP = piIP;
        this.piLastUpdate = piLastUpdate;
        roomList=new ArrayList<XMLRoom>();
    }

    public void destroyRoomList(){
        roomList.clear();
    }

    public void addRoom(XMLRoom room){
        roomList.add(room);
    }

    public ArrayList<XMLRoom> getRoomList(){
        return roomList;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        /*
        dest.writeString(this.piID);
        dest.writeString(this.piName);
        dest.writeString(this.piIP);
        dest.writeString(this.piLastUpdate);
        dest.writeList(this.roomList);*/

    }


}
