package com.hpdev.picontrol;

/**
 * Created by harry on 04/10/2016.
 */

public class Pi {

    private String piID;
    private String piName;
    private String piIP;
    private String piLastUpdate;

    public Pi(String piID, String piName, String piIP, String piLastUpdate) {
        this.piID = piID;
        this.piName = piName;
        this.piIP = piIP;
        this.piLastUpdate = piLastUpdate;
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

}
