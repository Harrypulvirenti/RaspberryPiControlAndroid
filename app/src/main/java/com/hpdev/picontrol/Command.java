package com.hpdev.picontrol;

/**
 * Created by harry on 16/10/2016.
 */

public class Command {
    private String PiName;
    private String RoomName;
    private String UserName;
    private String CommandString;
    private int Command;
    private boolean commandNoUsername;

    public static final boolean TYPE_NO_USERNAME=false;
    public static final boolean TYPE_USERNAME=true;

    public Command(String piName, String roomName, String userName, String commandString, int command) {
        PiName = piName;
        RoomName = roomName;
        UserName = userName;
        Command = command;
        CommandString=commandString;
        commandNoUsername=TYPE_USERNAME;
    }

    public Boolean haveUserName(){
        return commandNoUsername;
    }

    public void setCommandNoUsername(boolean commandNoUsername) {
        this.commandNoUsername = commandNoUsername;
    }

    public String getPiName() {
        return PiName;
    }

    public String getRoomName() {
        return RoomName;
    }

    public String getUserName() {
        return UserName;
    }

    public int getCommand() {
        return Command;
    }

    public String getCommandString() {
        return CommandString;
    }
}
