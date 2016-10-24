package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ViewRoomActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String KEY_ROOM="myRoom";


    private FloatingActionButton fabAddUser;
    private final static int REQUEST_ADD_USER=4123;
    private final static int REQUEST_VIEW_USER=413;
    private int MyPi;
    private int MyRoom;
    private String roomName;
    private final static String KEY_PI="MyPi";
    private static final String KEY_ROOM_POS="Room_Pos";
    private static final String KEY_USER="myUser";
    private View snackView;
    private RecyclerView typeUserRecyclerView;
    private GridLayoutManager layoutManager;
    private TypeUserAdapter adapter;
    private ArrayList<XMLUser> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        roomName=getIntent().getStringExtra(KEY_ROOM);
        getSupportActionBar().setTitle(roomName);
        Intent intent=getIntent();
        MyPi=intent.getIntExtra(KEY_PI,0);
        MyRoom=intent.getIntExtra(KEY_ROOM_POS,0);


        fabAddUser = (FloatingActionButton) findViewById(R.id.fabAddUser);
        fabAddUser.setOnClickListener(this);


        roomName=intent.getStringExtra(KEY_ROOM);
        MyPi = intent.getIntExtra(KEY_PI,0);
        MyRoom=intent.getIntExtra(KEY_ROOM_POS,0);
        userList=ActivityCoordinator.getRoomUserList(MyPi,MyRoom);


        layoutManager = new GridLayoutManager(this, 2);


        typeUserRecyclerView = (RecyclerView) findViewById(R.id.recyclerRoomUser);
        typeUserRecyclerView.setHasFixedSize(true);

        typeUserRecyclerView.setLayoutManager(layoutManager);

        typeUserRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        typeUserRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        adapter = new TypeUserAdapter(this,prepareAdapterData());
        typeUserRecyclerView.setAdapter(adapter);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==REQUEST_ADD_USER&&resultCode== Activity.RESULT_OK){
            Intent intent=new Intent(ViewRoomActivity.this,ViewUserActivity.class);
            intent.putExtra(KEY_PI,MyPi);
            intent.putExtra(KEY_ROOM_POS,MyRoom);
            intent.putExtra(KEY_USER,data.getIntExtra(KEY_USER,0));
            intent.putExtra(KEY_ROOM,roomName);
            startActivityForResult(intent,REQUEST_VIEW_USER);
            userList=ActivityCoordinator.getRoomUserList(MyPi,MyRoom);
            adapter.setMyData(prepareAdapterData());
            adapter.notifyDataSetChanged();
        }
        if(requestCode==REQUEST_ADD_USER&&resultCode== Activity.RESULT_CANCELED){
            roomName=getIntent().getStringExtra(KEY_ROOM);
            getSupportActionBar().setTitle(roomName);
        }

        if(requestCode==REQUEST_VIEW_USER&&resultCode== Activity.RESULT_CANCELED){
            roomName=getIntent().getStringExtra(KEY_ROOM);
            getSupportActionBar().setTitle(roomName);
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.fabAddUser){
            snackView=v;
            Intent intent=new Intent(ViewRoomActivity.this,AddUserActivity.class);
            intent.putExtra(AddRoomActivity.KEY_PI, MyPi);
            intent.putExtra(KEY_ROOM,roomName);
            intent.putExtra(KEY_ROOM_POS,MyRoom);

            startActivityForResult(intent,REQUEST_ADD_USER);

        }
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

    private AdapterData[] prepareAdapterData(){
        AdapterData[] data=new AdapterData[userList.size()];

        for(int i=0;i<userList.size();i++){
            XMLUser user=userList.get(i);
            switch (user.getType()) {
                case XMLUser.USER_TYPE_RELAY:
                    data[i]=new AdapterData(user.getUserName(),R.drawable.img_relay);
                    break;
                case XMLUser.USER_TYPE_SENSOR_DH11:
                    data[i]=new AdapterData(user.getUserName(),R.drawable.img_dh11);
                    break;
            }}

        return data;
    }


    void showToastMessage(String message) {
        if(snackView!=null)
             Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

    private class TypeUserAdapter extends RecyclerView.Adapter<TypeUserAdapter.ViewHolder> {

        private AdapterData[] myData;
        private Context mContext;

        private int Selected=-1;
        private int selectedColor;
        private int notSelectedColor;


        public TypeUserAdapter(Context cont, AdapterData[] roomList) {
            myData = roomList;
            mContext=cont;
            Resources res=cont.getResources();
            // selectedColor=R.drawable.selected_background;
            //notSelectedColor=R.drawable.non_selected_background;
        }

        public void setMyData(AdapterData[] myData) {
            this.myData = myData;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView tvType;
            public CardView cvRoomCard;
            public ImageView imgRoomType;


            public ViewHolder(View vCard) {
                super(vCard);
                cvRoomCard = (CardView) vCard.findViewById(R.id.card_view);
                tvType = (TextView) vCard.findViewById(R.id.tvUserName);
                imgRoomType = (ImageView) vCard.findViewById(R.id.img_userType);
            }
        }

        public int getSelected() {
            return Selected;
        }

        @Override
        public TypeUserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.type_user_recycler_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            TypeUserAdapter.ViewHolder vh = new TypeUserAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(TypeUserAdapter.ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.tvType.setText(myData[position].getName());
            Glide.with(mContext).load(myData[position].getImageResources()).into(holder.imgRoomType);


           /* if(myData[position].isSelected())
                holder.tvType.setBackground(getDrawable(selectedColor));
            else
                holder.tvType.setBackground(getDrawable(notSelectedColor));


            */
            holder.cvRoomCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem(position);

                }
            });

        }

        private void selectedItem(int position) {
           Intent intent=new Intent(ViewRoomActivity.this,ViewUserActivity.class);
            intent.putExtra(KEY_PI,MyPi);
            intent.putExtra(KEY_ROOM_POS,MyRoom);
            intent.putExtra(KEY_USER,position);
            intent.putExtra(KEY_ROOM,roomName);
            startActivityForResult(intent,REQUEST_VIEW_USER);

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
