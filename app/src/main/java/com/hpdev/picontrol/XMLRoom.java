package com.hpdev.picontrol;

import java.util.ArrayList;

/**
 * Created by harry on 11/10/2016.
 */
public class XMLRoom {


    private String Name;
    private ArrayList<XMLUser> userList;

    public XMLRoom(String name, ArrayList<XMLUser> user) {
        Name = name;
        userList=user;
    }

    public String getName() {
        return Name;
    }

    public ArrayList<XMLUser> getUserList() {
        return userList;
    }
}
