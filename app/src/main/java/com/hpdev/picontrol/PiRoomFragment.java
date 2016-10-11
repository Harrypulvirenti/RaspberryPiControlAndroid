package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Admin on 04-06-2015.
 */
public class PiRoomFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    public final static String KEY_PI="MyPi";

    private Pi myPi;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView roomRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RoomAdapter adapter;
    private TextView tvEmptyRoom;
    private FloatingActionButton fabAddRoom;
    private final static int REQUEST_ADD_ROOM=21423;
    private final static String KEY_ROOM="myRoom";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pi_room_fragment,container,false);

        Bundle extra=getArguments();

       // refreshLayout= (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        //refreshLayout.setOnRefreshListener(this);
        // tvEmptyRoom= (TextView) v.findViewById(R.id.tvEmptyRom);

        roomRecyclerView= (RecyclerView) v.findViewById(R.id.recyclerRoomList);
        roomRecyclerView.setHasFixedSize(true);

        if(extra!=null){

            myPi=extra.getParcelable(KEY_PI);

        }


        // use a linear layout manager
        layoutManager = new LinearLayoutManager(v.getContext());
        roomRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new RoomAdapter(myPi.getRoomList());
        roomRecyclerView.setAdapter(adapter);

        tvEmptyRoom=(TextView)v.findViewById(R.id.tvEmptyRom);

        if(myPi.getRoomList().size()==0) {
            roomRecyclerView.setVisibility(View.GONE);
            tvEmptyRoom.setVisibility(View.VISIBLE);
        }

        fabAddRoom=(FloatingActionButton) v.findViewById(R.id.fabAddRoom);
        fabAddRoom.setOnClickListener(this);


        return v;
    }


    @Override
    public void onRefresh() {

        Log.v("HEREEEEEEEEEE","REFRESH");


                refreshLayout.setRefreshing(false);





    }


    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.fabAddRoom){

            Intent intent =new Intent(getActivity(),AddRoomActivity.class);

            intent.putExtra(AddRoomActivity.KEY_PI_IP,myPi.getPiIP());

            startActivityForResult(intent,REQUEST_ADD_ROOM);


        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_ADD_ROOM&&resultCode== Activity.RESULT_OK){
            myPi.addRoom(new Room(data.getStringExtra(KEY_ROOM)));
            adapter.setMyData(myPi.getRoomList());
            adapter.notifyDataSetChanged();
            roomRecyclerView.setVisibility(View.VISIBLE);
            tvEmptyRoom.setVisibility(View.GONE);
        }

    }

    private class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

        private ArrayList<Room> myData;

        public void setMyData(ArrayList<Room> list){
            myData=list;
        }


        public RoomAdapter(ArrayList<Room> roomList) {
            myData=roomList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView tvRoomName;
            public CardView cvRoomCard;
            public ViewHolder(CardView vCard) {
                super(vCard);
                cvRoomCard=vCard;
                tvRoomName = (TextView) vCard.findViewById(R.id.info_text);
            }
        }



        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.room_recycler_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            ViewHolder vh = new ViewHolder((CardView) v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.tvRoomName.setText(myData.get(position).getName());

        }

        @Override
        public int getItemCount() {
            return myData.size();
        }







    }
}
