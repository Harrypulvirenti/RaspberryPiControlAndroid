package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AddRoomActivity extends AppCompatActivity implements View.OnClickListener {

    private View snackView;
    private FloatingActionButton fabDoneAddRoom;
    private EditText etRoomName;
    private String roomName = null;
    public final static String KEY_PI="MyPi";
    public final static String KEY_PI_IP = "MyPi_IP";
    private final static String KEY_ROOM = "myRoom";
    private final static String KEY_ROOM_TYPE = "myRoom_Type";
    private final static String KEY_ROOM_LIST="myRoom_List";

    private RecyclerView typeRecyclerView;
    private GridLayoutManager layoutManager;
    private AddRoomActivity.TypeAdapter adapter;


    private int MyPi;
    private String[] roomNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabDoneAddRoom = (FloatingActionButton) findViewById(R.id.doneAddRoom);
        fabDoneAddRoom.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etRoomName = (EditText) findViewById(R.id.addRoomName);
        MyPi = getIntent().getIntExtra(KEY_PI,0);
        roomNameList=ActivityCoordinator.getRoomListName(MyPi);

        layoutManager = new GridLayoutManager(this, 2);


        typeRecyclerView = (RecyclerView) findViewById(R.id.recyclerTypeRoom);
        typeRecyclerView.setHasFixedSize(true);

        typeRecyclerView.setLayoutManager(layoutManager);

        typeRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        typeRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        adapter = new TypeAdapter(this,prepareAdapterData(getResources().getStringArray(R.array.roomTypeName)));
        typeRecyclerView.setAdapter(adapter);


    }


    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private AdapterData[] prepareAdapterData(String[] names){
        AdapterData[] data=new AdapterData[names.length];

        for(int i=0;i<names.length;i++){
        switch (i) {
            case XMLRoom.TYPE_ROOM:
                data[i]=new AdapterData(names[i],R.drawable.img_room_sqr);
                break;
            case XMLRoom.TYPE_BED_ROOM:
                data[i]=new AdapterData(names[i],R.drawable.img_bedroom_sqr);
                break;
            case XMLRoom.TYPE_GARDEN_ROOM:
                data[i]=new AdapterData(names[i],R.drawable.img_garden_sqr);
                break;
            case XMLRoom.TYPE_KITCHEN_ROOM:
                data[i]=new AdapterData(names[i],R.drawable.img_kitchen_sqr);
                break;
            case XMLRoom.TYPE_LIVING_ROOM:
                data[i]=new AdapterData(names[i],R.drawable.img_living_room_sqr);
                break;
            case XMLRoom.TYPE_SWIMMING_POOL_ROOM:
                data[i]=new AdapterData(names[i],R.drawable.img_swimming_pool_sqr);
                break;
        }}

        return data;
    }


    void showToastMessage(String message) {
        Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.doneAddRoom) {
            snackView = v;
            String myString = etRoomName.getText().toString().trim();

            if (myString.length() > 0) {
                boolean nameValid=true;

                for(int i=0;i<roomNameList.length;i++)
                    if(myString.equalsIgnoreCase(roomNameList[i])){
                        nameValid=false;
                        break;
                    }
                if(nameValid){
                    roomName = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                     addRoomToPi();}
                else {
                    showToastMessage(getString(R.string.roomNamePresent));
                }
            } else {
                showToastMessage(getString(R.string.noNameRoom));

            }
        }
    }

    private void addRoomToPi() {
        String ret = null;
        if(adapter.getSelected()>-1){
        try {
            ret = (String) new RaspberryTCPClient(ActivityCoordinator.getPiIP(MyPi), getResources(), RaspberryTCPClient.TYPE_ADD_ROOM, roomName,adapter.getSelected()).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (ret.equals(RaspberryTCPClient.OPERATION_DONE)) {

            showToastMessage(getString(R.string.roomAdded));

            ActivityCoordinator.addRoomToPi(new XMLRoom(roomName,adapter.getSelected()),MyPi);

            setResult(Activity.RESULT_OK);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1500);
        } else {
            showToastMessage(getString(R.string.addRoomError));
        }}else{
            showToastMessage(getString(R.string.textErrorSelectRoomType));
        }

    }


    private class TypeAdapter extends RecyclerView.Adapter<AddRoomActivity.TypeAdapter.ViewHolder> {

        private AdapterData[] myData;
        private Context mContext;

        private int Selected=-1;
        private int selectedColor;
        private int notSelectedColor;


        public TypeAdapter(Context cont, AdapterData[] roomList) {
            myData = roomList;
            mContext=cont;
            selectedColor=R.drawable.selected_background;
            notSelectedColor=R.drawable.non_selected_background;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView tvType;
            public CardView cvRoomCard;
            public ImageView imgRoomType;


            public ViewHolder(View vCard) {
                super(vCard);
                cvRoomCard = (CardView) vCard.findViewById(R.id.card_view);
                tvType = (TextView) vCard.findViewById(R.id.tvTypeName);
                imgRoomType = (ImageView) vCard.findViewById(R.id.img_roomType);
            }
        }

        public int getSelected() {
            return Selected;
        }

        @Override
        public AddRoomActivity.TypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.type_room_recycler_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            AddRoomActivity.TypeAdapter.ViewHolder vh = new AddRoomActivity.TypeAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(AddRoomActivity.TypeAdapter.ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.tvType.setText(myData[position].getName());
            Glide.with(mContext).load(myData[position].getImageResources()).into(holder.imgRoomType);
            if(myData[position].isSelected())
                 holder.tvType.setBackground(getDrawable(selectedColor));
            else
                holder.tvType.setBackground(getDrawable(notSelectedColor));


            holder.cvRoomCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  selectItem(position);

                }
            });


        }

        private void selectItem(int position) {
            if(Selected>-1){
                myData[Selected].setSelected(false);
                notifyItemChanged(Selected);}
            Selected=position;
            myData[Selected].setSelected(true);
            notifyItemChanged(Selected);
        }

        @Override
        public int getItemCount() {
            return myData.length;
        }


    }

    private class AdapterData {
        private String Name;
        private int ImageResources;
        private boolean Selected;

        public AdapterData(String name, int imageResources) {
            Name = name;
            ImageResources = imageResources;
            this.Selected=false;
        }

        public void setSelected(boolean selected){
            Selected=selected;
        }

        public String getName() {
            return Name;
        }

        public int getImageResources() {
            return ImageResources;
        }

        public boolean isSelected(){
            return Selected;
        }
    }

}
