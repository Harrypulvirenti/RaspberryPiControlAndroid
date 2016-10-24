package com.hpdev.picontrol;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by harry on 11/10/2016.
 */

public class RaspberryTCPClient extends AsyncTask {

    public static final String TYPE_ADD_ROOM="room";
    public static final String TYPE_ADD_USER="user";
    public static final String TYPE_EXEC_COMMAND="command";
    public static final String TYPE_UPDATE_REQUEST="update";
    public static final String WAIT_MESSAGE="ok_send_me";
    public static final String DONE_MESSAGE="done";
    public static final String OPERATION_DONE="done";
    public static final String OPERATION_FAIL="fail";
    public static final String OPERATION_FAIL_NO_PIN="noPin";
    public static final String ERROR="error";

    private String pi_ip;
    private Resources Res;
    private String RequestType;
    private String roomName;
    private String roomType;
    private String UserType;
    private String UserName;

    private Command CommandResult;

    public RaspberryTCPClient(String pi_ip, Resources res, String type_request) {
        this.pi_ip=pi_ip;
        Res=res;
        RequestType=type_request;
    }

    public RaspberryTCPClient(String pi_ip, Resources res, String type_request, Command command) {
        this.pi_ip=pi_ip;
        Res=res;
        RequestType=type_request;
        CommandResult=command;
    }

    public RaspberryTCPClient(String pi_ip, Resources res, String type_request,String roomName, int roomType) {
        this.pi_ip=pi_ip;
        Res=res;
        RequestType=type_request;
        this.roomName=roomName;
        this.roomType=String.valueOf(roomType);
    }

    public RaspberryTCPClient(String pi_ip, Resources res, String type_request,String UserName, int UserType,String roomName) {
        this.pi_ip=pi_ip;
        Res=res;
        RequestType=type_request;
        this.UserName=UserName;
        this.UserType=String.valueOf(UserType);
        this.roomName=roomName;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String serverResp = null;
        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        Socket clientSocket = null;
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;

        Object Ret=ERROR;

        try {
            clientSocket = new Socket(this.pi_ip, Res.getInteger(R.integer.PortNumber));
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            if(RequestType.equals(TYPE_ADD_ROOM)){

                outToServer.writeBytes(TYPE_ADD_ROOM+"\n");
                serverResp = inFromServer.readLine();
                if(serverResp.equals(WAIT_MESSAGE)){

                    outToServer.writeBytes(roomName+"\n");
                    outToServer.writeBytes(roomType+"\n");
                    serverResp = inFromServer.readLine();
                    if(serverResp.equals(DONE_MESSAGE)){
                        clientSocket.close();
                        Ret= OPERATION_DONE;
                    }else{
                        clientSocket.close();
                        Ret= OPERATION_FAIL;
                    }
                }

            }

            if(RequestType.equals(TYPE_UPDATE_REQUEST)){

                outToServer.writeBytes(TYPE_UPDATE_REQUEST+"\n");
                serverResp = inFromServer.readLine();
                String xml="";
                if(serverResp.equals(WAIT_MESSAGE)){

                    while (true){
                        serverResp = inFromServer.readLine();
                        if(!serverResp.equals(DONE_MESSAGE)){
                            xml+=serverResp+"\n";
                        }else{
                            xml = xml.substring(0, xml.length()-1);
                            break;
                        }

                    }
                }
                clientSocket.close();
                Ret= xml;

            }

            if(RequestType.equals(TYPE_ADD_USER)){

                outToServer.writeBytes(TYPE_ADD_USER+"\n");
                serverResp = inFromServer.readLine();
                if(serverResp.equals(WAIT_MESSAGE)){
                    serverResp="";
                    outToServer.writeBytes(UserType+"\n");
                    serverResp = inFromServer.readLine();
                    if(serverResp.equals(WAIT_MESSAGE)){
                        outToServer.writeBytes(UserName+"\n");
                        outToServer.writeBytes(roomName+"\n");
                        ArrayList<XMLPin> users=new ArrayList<XMLPin>();
                        while (true){
                            serverResp = inFromServer.readLine();
                            if(!serverResp.equals(DONE_MESSAGE)){
                                users.add(new XMLPin(Integer.parseInt(serverResp),UserName,Integer.parseInt(UserType)));
                            }else{
                                clientSocket.close();
                                Ret= users;
                            }

                        }

                    }else{
                        clientSocket.close();
                        Ret= OPERATION_FAIL_NO_PIN;
                    }
                }
            }


            if(RequestType.equals(TYPE_EXEC_COMMAND)){

                outToServer.writeBytes(TYPE_EXEC_COMMAND+"\n");
                serverResp = inFromServer.readLine();
                if(serverResp.equals(WAIT_MESSAGE)){

                    outToServer.writeBytes(CommandResult.getRoomName()+"\n");
                    outToServer.writeBytes(CommandResult.getUserName()+"\n");
                    outToServer.writeBytes(CommandResult.getCommand()+"\n");
                    String resp=inFromServer.readLine();
                    serverResp = inFromServer.readLine();
                    if(serverResp.equals(DONE_MESSAGE)){
                        clientSocket.close();
                        Ret= resp;
                    }else{
                        clientSocket.close();
                        Ret= OPERATION_FAIL;
                    }


                }else{
                    clientSocket.close();

                    Ret= OPERATION_FAIL;
                }


            }



        } catch (IOException e) {

        }finally {
            return Ret;
        }


    }
}
