package com.hpdev.picontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AddUserActivity extends AppCompatActivity implements View.OnClickListener{

    private FloatingActionButton fabDoneAddOn;
    private EditText etUserName;
    private int MyPi;
    private int MyRoom;
    private final static String KEY_PI="MyPi";
    private static final String KEY_ROOM_POS="Room_Pos";
    private final static String KEY_ROOM="myRoom";

    private View snackView;

    private RecyclerView typeUserRecyclerView;
    private GridLayoutManager layoutManager;
    private TypeUserAdapter adapter;
    private String UserName="";
    private String roomName;
    private String[] userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        fabDoneAddOn = (FloatingActionButton) findViewById(R.id.fabDoneAddOn);
        fabDoneAddOn.setOnClickListener(this);


        etUserName = (EditText) findViewById(R.id.addUserName);
        Intent intent=getIntent();
        roomName=intent.getStringExtra(KEY_ROOM);
        MyPi = intent.getIntExtra(KEY_PI,0);
        MyRoom=intent.getIntExtra(KEY_ROOM_POS,0);
        userList=ActivityCoordinator.getRoomUserList(MyPi,MyRoom);


        layoutManager = new GridLayoutManager(this, 2);


        typeUserRecyclerView = (RecyclerView) findViewById(R.id.recyclerTypeUser);
        typeUserRecyclerView.setHasFixedSize(true);

        typeUserRecyclerView.setLayoutManager(layoutManager);

        typeUserRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        typeUserRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        adapter = new TypeUserAdapter(this,prepareAdapterData(getResources().getStringArray(R.array.userTypeName)));
        typeUserRecyclerView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fabDoneAddOn) {
            snackView = v;
            String myString = etUserName.getText().toString().trim();

            if (myString.length() > 0) {
                boolean nameValid=true;

                for(int i=0;i<userList.length;i++)
                    if(myString.equalsIgnoreCase(userList[i])){
                        nameValid=false;
                        break;
                    }
                if(nameValid){
                    UserName = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                    addUserToPi();}
                else {
                    showToastMessage(getString(R.string.userNamePresent));
                }
            } else {
                showToastMessage(getString(R.string.noUserName));

            }
        }

    }

    private void addUserToPi() {

        ArrayList<XMLPin> xmlPins=null;
        if(adapter.getSelected()>-1){
            try {
                xmlPins = (ArrayList<XMLPin>) new RaspberryTCPClient(ActivityCoordinator.getPiIP(MyPi), getResources(), RaspberryTCPClient.TYPE_ADD_USER, UserName,adapter.getSelected(),roomName).execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (xmlPins!=null) {

                showToastMessage(getString(R.string.addOnAdded));

                ActivityCoordinator.addUserToRoom(new XMLUser(UserName,adapter.getSelected(),xmlPins),MyPi,MyRoom);

                //Intent data = new Intent();
                //data.putExtra(KEY_ROOM, roomName);
                //setResult(Activity.RESULT_OK, data);

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
            showToastMessage(getString(R.string.errorSelectUserType));
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

       if(item.getItemId()==android.R.id.home){
           Intent intent=new Intent();
           intent.putExtra(KEY_ROOM,roomName);
           setResult(RESULT_CANCELED,intent);
           finish();
           return true;
       }



        return super.onOptionsItemSelected(item);


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
                case XMLUser.USER_TYPE_RELAY:
                    data[i]=new AdapterData(names[i],R.drawable.img_relay);
                    break;
                case XMLUser.USER_TYPE_SENSOR_DH11:
                    data[i]=new AdapterData(names[i],R.drawable.img_dh11);
                    break;
            }}

        return data;
    }


    void showToastMessage(String message) {
        Snackbar.make(snackView, message, Snackbar.LENGTH_LONG).show();
    }

    private class TypeUserAdapter extends RecyclerView.Adapter<AddUserActivity.TypeUserAdapter.ViewHolder> {

        private AdapterData[] myData;
        private Context mContext;

        private int Selected=-1;
        private int selectedColor;
        private int notSelectedColor;


        public TypeUserAdapter(Context cont, AdapterData[] roomList) {
            myData = roomList;
            mContext=cont;
            Resources res=cont.getResources();
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
                tvType = (TextView) vCard.findViewById(R.id.tvUserName);
                imgRoomType = (ImageView) vCard.findViewById(R.id.img_userType);
            }
        }

        public int getSelected() {
            return Selected;
        }

        @Override
        public AddUserActivity.TypeUserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.type_user_recycler_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            AddUserActivity.TypeUserAdapter.ViewHolder vh = new AddUserActivity.TypeUserAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(AddUserActivity.TypeUserAdapter.ViewHolder holder, int position) {
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
