package com.hpdev.picontrol;

/**
 * Created by harry on 09/10/2016.
 */
public class XMLPin {

    private int pinNumber;
    private String pinIdentifier;
    private int pinType;

    public XMLPin(int pinNumber, String pinIdentifier, int pinType) {
        this.pinNumber = pinNumber;
        this.pinIdentifier = pinIdentifier;
        this.pinType = pinType;
    }

    public int getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(int pinNumber) {
        this.pinNumber = pinNumber;
    }

    public String getPinIdentifier() {
        return pinIdentifier;
    }

    public void setPinIdentifier(String pinIdentifier) {
        this.pinIdentifier = pinIdentifier;
    }

    public int getPinType() {
        return pinType;
    }

    public void setPinType(int pinType) {
        this.pinType = pinType;
    }
}
