package com.hpdev.picontrol;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Admin on 04-06-2015.
 */
public class PiRoomFragment extends Fragment {

    final static String KEY_PI_NAME="piname";
    final static String KEY_PI_IP="piip";

    private String piName;
    private String piIP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pi_room_fragment,container,false);

        Bundle extra=getArguments();

        if(extra!=null){

        piName=extra.getString(KEY_PI_NAME);
        piIP=extra.getString(KEY_PI_IP);

        TextView tv=(TextView) v.findViewById(R.id.tvPiroom);
        tv.setText(piName);

        }

        return v;
    }
    
    
    
}
