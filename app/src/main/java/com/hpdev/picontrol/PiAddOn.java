package com.hpdev.picontrol;

/**
 * Created by harry on 10/10/2016.
 */

public class PiAddOn {

    public final static int TYPE_ACTUATOR=0;
    public final static int TYPE_SENSOR_DH11=1;

    private String Name;


    private int Type;

    public PiAddOn(String name, int type) {
        Name = name;
        Type = type;
    }

    public String getName() {
        return Name;
    }

    public int getType() {
        return Type;
    }
}
