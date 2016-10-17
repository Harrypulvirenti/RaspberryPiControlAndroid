package com.hpdev.picontrol;

import java.util.ArrayList;

/**
 * Created by harry on 11/10/2016.
 */
public class XMLRoom {


    //UPDATE BOTH TYPE HERE AND IN STRING.XML IN THE ARRAY WITH THE SAME ORDER

    public final static int TYPE_ROOM=0;
    public final static int TYPE_LIVING_ROOM=1;
    public final static int TYPE_BED_ROOM=2;
    public final static int TYPE_KITCHEN_ROOM=3;
    public final static int TYPE_SWIMMING_POOL_ROOM=4;
    public final static int TYPE_GARDEN_ROOM=5;


        private String Name;
        private ArrayList<XMLUser> userList;

        private int RoomType=0;
        private int RoomImage;

        public XMLRoom(String name, ArrayList<XMLUser> user) {
            Name = name;
            userList=user;
        }
    public XMLRoom(String name,int roomType) {
        Name = name;
        userList=new ArrayList<XMLUser>();
        RoomType=roomType;
        initRoomImg();
    }

    public void initRoomImg(){
        switch (RoomType) {
            case XMLRoom.TYPE_ROOM:
                RoomImage=R.drawable.img_room;
                break;
            case XMLRoom.TYPE_BED_ROOM:
                RoomImage=R.drawable.img_bedroom;
                break;
            case XMLRoom.TYPE_GARDEN_ROOM:
                RoomImage=R.drawable.img_garden;
                break;
            case XMLRoom.TYPE_KITCHEN_ROOM:
                RoomImage=R.drawable.img_kitchen;
                break;
            case XMLRoom.TYPE_LIVING_ROOM:
                RoomImage=R.drawable.img_living_room;
                break;
            case XMLRoom.TYPE_SWIMMING_POOL_ROOM:
                RoomImage=R.drawable.img_swimming_pool;
                break;
        }

    }


    public XMLRoom(String name, ArrayList<XMLUser> userList, int roomType) {
            Name = name;
            this.userList = userList;
            RoomType = roomType;

            initRoomImg();
        }

    public void addUser(XMLUser user){
        userList.add(user);
    }

        public String getName() {
            return Name;
        }

        public ArrayList<XMLUser> getUserList() {
            return userList;
        }

        public int getRoomType() {
            return RoomType;
        }

    public int getRoomImage() {
        return RoomImage;
    }
}
