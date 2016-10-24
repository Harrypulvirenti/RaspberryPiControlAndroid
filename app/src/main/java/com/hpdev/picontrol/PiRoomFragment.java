package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration;
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Admin on 04-06-2015.
 */
public class PiRoomFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private final static String KEY_PI="MyPi";
    private static final String KEY_ROOM_POS="Room_Pos";

    private int MyPi;
    private SwipeRefreshLayout refreshLayout;
    private static RecyclerView roomRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RoomAdapter adapter;
    private TextView tvEmptyRoom;
    private static FloatingActionButton fabAddRoom;
    private final static int REQUEST_ADD_ROOM=21423;
    private final static String KEY_ROOM="myRoom";
    private static TextView tvOffline;
    private View snackView;
    private TextView tvNoRaspberry;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pi_room_fragment, container, false);

        Bundle extra = getArguments();

        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setOnRefreshListener(this);


        roomRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerRoomList);
        roomRecyclerView.setHasFixedSize(true);

        tvOffline = (TextView) v.findViewById(R.id.tvRaspberryOffline);
        tvNoRaspberry = (TextView) v.findViewById(R.id.tvNoRaspberry);
        fabAddRoom = (FloatingActionButton) v.findViewById(R.id.fabAddRoom);
        fabAddRoom.setOnClickListener(this);

        if (extra != null) {

            MyPi = extra.getInt(KEY_PI);

        }

        if(MyPi>-1){

        if (!ActivityCoordinator.initRoomList(MyPi)) {
            roomRecyclerView.setVisibility(View.GONE);
            tvOffline.setVisibility(View.VISIBLE);
        }

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(v.getContext());
        roomRecyclerView.setLayoutManager(layoutManager);

        roomRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));

        int offsetPx = 25;


        roomRecyclerView.addItemDecoration(new EndOffsetItemDecoration(offsetPx));
        roomRecyclerView.addItemDecoration(new StartOffsetItemDecoration(offsetPx));

        roomRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        adapter = new RoomAdapter(getContext(), ActivityCoordinator.getRoomList(MyPi));
        roomRecyclerView.setAdapter(adapter);


        tvEmptyRoom = (TextView) v.findViewById(R.id.tvEmptyRom);

        if (ActivityCoordinator.getRoomListSize(MyPi) == 0 && tvOffline.getVisibility() == View.GONE) {
            roomRecyclerView.setVisibility(View.GONE);
            tvEmptyRoom.setVisibility(View.VISIBLE);
        }


    }else {
            roomRecyclerView.setVisibility(View.GONE);
            fabAddRoom.setVisibility(View.GONE);
            tvNoRaspberry.setVisibility(View.VISIBLE);

        }

        return v;
    }

    public static boolean piIsOnline(){
        if(tvOffline.getVisibility()==View.VISIBLE)
            return false;
        else
            return true;
    }

    public static void comunicatePiOffline(){
        roomRecyclerView.setVisibility(View.GONE);
        tvOffline.setVisibility(View.VISIBLE);
        fabAddRoom.setVisibility(View.GONE);
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }





    @Override
    public void onRefresh() {
        if(isOnline()) {
            ArrayList<XMLRoom> list = ActivityCoordinator.updateRoomList(MyPi);

            if (list != null) {

                if (list.size() > 0) {

                    adapter.setMyData(list);
                    adapter.notifyDataSetChanged();
                    roomRecyclerView.setVisibility(View.VISIBLE);
                    fabAddRoom.setVisibility(View.VISIBLE);
                    tvOffline.setVisibility(View.GONE);
                    tvEmptyRoom.setVisibility(View.GONE);
                } else {
                    roomRecyclerView.setVisibility(View.GONE);
                    tvEmptyRoom.setVisibility(View.VISIBLE);
                    fabAddRoom.setVisibility(View.GONE);
                    tvOffline.setVisibility(View.GONE);
                    tvOffline.setText(getString(R.string.textRaspberryOffline));
                }
            } else {
                roomRecyclerView.setVisibility(View.GONE);
                fabAddRoom.setVisibility(View.GONE);
                tvOffline.setVisibility(View.VISIBLE);
            }
            refreshLayout.setRefreshing(false);

        }else {
            refreshLayout.setRefreshing(false);
            tvOffline.setText(getString(R.string.errorOffline));
            fabAddRoom.setVisibility(View.GONE);
            roomRecyclerView.setVisibility(View.GONE);
            tvOffline.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.fabAddRoom){
            snackView=v;
            if(tvOffline.getVisibility()==View.GONE) {
                Intent intent = new Intent(getActivity(), AddRoomActivity.class);

                intent.putExtra(KEY_PI, MyPi);

                startActivityForResult(intent, REQUEST_ADD_ROOM);
            }else if(isOnline()){

                showToastMessage(getString(R.string.textRaspberryOffline));
            }else {
                showToastMessage(getString(R.string.errorOffline));
            }

        }


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_ADD_ROOM&&resultCode== Activity.RESULT_OK){

            adapter.setMyData(ActivityCoordinator.getRoomList(MyPi));
            adapter.notifyDataSetChanged();
            roomRecyclerView.setVisibility(View.VISIBLE);
            tvEmptyRoom.setVisibility(View.GONE);
        }

    }

    private class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder>{

        private ArrayList<XMLRoom> myData;
        private Context mContext;

        public void setMyData(ArrayList<XMLRoom> list){
            myData=list;
        }


        public RoomAdapter(Context context,ArrayList<XMLRoom> roomList) {
            myData=roomList;
            mContext=context;
        }


        public void onItemClick(int position) {

            Intent intent =new Intent(getActivity(),ViewRoomActivity.class);
            intent.putExtra(KEY_ROOM,myData.get(position).getName());
            intent.putExtra(KEY_PI, MyPi);
            intent.putExtra(KEY_ROOM_POS,position);

            startActivity(intent);

        }


        public void onItemLongClick(int position) {
            Log.v("HEREEEEEEEEE","Long Press "+String.valueOf(position));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView tvRoomName;
            public CardView cvRoomCard;
            public ImageView imgRoomType;
            public ViewHolder(View vCard) {
                super(vCard);
                cvRoomCard= (CardView) vCard.findViewById(R.id.card_view);
                tvRoomName = (TextView) vCard.findViewById(R.id.tvRoomName);
                imgRoomType=(ImageView)vCard.findViewById(R.id.img_room);
            }
        }



        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.room_recycler_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.tvRoomName.setText(myData.get(position).getName());
            Glide.with(mContext).load(myData.get(position).getRoomImage()).into(holder.imgRoomType);

            holder.cvRoomCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
            holder.cvRoomCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    onItemLongClick(position);

                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            return myData.size();
        }


    }


    void  showToastMessage(String message){
        if(snackView!=null)
          Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

    private boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


}
