package com.hpdev.picontrol;

import java.util.ArrayList;

/**
 * Created by harry on 11/10/2016.
 */
public class XMLUser {

    //UPDATE BOTH TYPE HERE AND IN STRING.XML IN THE ARRAY WITH THE SAME ORDER

    public final static int USER_TYPE_RELAY=0;
    public final static int USER_TYPE_SENSOR_DH11=1;



    private ArrayList<XMLPin> PinList;
    private String UserName;
    private int Type;
    private int pinNumber;

    public XMLUser(String UserName, int Type, ArrayList<XMLPin> pins) {
        this.UserName = UserName;
        this.Type = Type;
        this.pinNumber = pins.size();
        PinList=pins;
    }

    public ArrayList<XMLPin> getPinList() {
        return PinList;
    }

    public String getUserName() {
        return UserName;
    }

    public int getType() {
        return Type;
    }

    public int getPinNumber() {
        return pinNumber;
    }
}
