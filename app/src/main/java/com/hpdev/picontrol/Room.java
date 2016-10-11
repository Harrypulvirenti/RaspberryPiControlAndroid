package com.hpdev.picontrol;

/**
 * Created by harry on 10/10/2016.
 */

import java.util.ArrayList;

/**
 * Created by harry on 09/10/2016.
 */
public class Room {

    private String Name;
    private ArrayList<PiAddOn> userList;

    public Room(String name) {
        Name = name;
        userList=new ArrayList<PiAddOn>();
    }

    public void addUser(PiAddOn user){
        userList.add(user);
    }

    public String getName() {
        return Name;
    }

    public ArrayList<PiAddOn> getUserList() {
        return userList;
    }
}
