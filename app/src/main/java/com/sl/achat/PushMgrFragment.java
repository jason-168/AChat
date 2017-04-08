package com.sl.achat;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.sl.SLService;
import com.sl.db.DB;
import com.sl.db.DB.Device;
import com.sl.db.DB.User;
import com.sl.db.DBService;
import com.sl.push.SLPush;
import com.sl.push.SLPushListener;
import com.sl.protocol.Ppushinfo.PCS_GetUserPushOnoff;
import com.sl.protocol.Ppushinfo.PCS_GetUserPushOnoffRes;
import com.sl.protocol.Ppushinfo.PCS_UserPushOnoff;
import com.sl.protocol.Ppushinfo.PCS_UserPushOnoffRes;
import com.sl.protocol.Ppushinfo.PushOnoffInfo;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PushMgrFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PushMgrFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PushMgrFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView mRecyclerView;
    private DeviceRecyclerAdapter mRecyclerAdapter;
    private ArrayList<DB.Device> mDeviceList = new ArrayList<DB.Device>();


    private SLPush push = null;
    private boolean havePushOnoff = false;

    private int mOpttype;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PushMgrFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceFragment.
     */
    public static PushMgrFragment newInstance(int type, String param2) {
        PushMgrFragment fragment = new PushMgrFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOpttype = getArguments().getInt(ARG_TYPE, MainApplication.SL_USER);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_push_mgr, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerAdapter = new DeviceRecyclerAdapter(this.getActivity(), mDeviceList, R.drawable.ic_push);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mRecyclerView.addOnItemTouchListener(new SimpleOnItemClickListener(mRecyclerView,onClickListener));

        push = SLPush.get();
        push.setSLPushListener(new MySLPushListener());

        handler.sendEmptyMessageDelayed(PUSH_ONOFF_LIST, 100);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        push.setSLPushListener(null);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(havePushOnoff == false) {
            DB.User user = ((MainApplication) this.getActivity().getApplication()).getUser();
            if (user != null) {
                DBService db = new DBService(this.getActivity());
                updateStatus(db, user);
                db.close();
            }
        }
    }

    private void updateStatus(DBService db, DB.User user) {
        db.queryDevice(mDeviceList, DB.Device.SELECT_ONLINK_BY_TYPE, new String[]{""+user.getId(), "" + mOpttype});
        for(int i=0; i<mDeviceList.size(); i++){
            mDeviceList.get(i).setStatus(0);
        }
        mRecyclerAdapter.notifyDataSetChanged();
    }


    private SimpleOnItemClickListener.OnItemClickListener onClickListener = new SimpleOnItemClickListener.OnItemClickListener(){
        @Override
        public void onItemClick(View view, int position) {
            Device dev = mDeviceList.get(position);
            dialogOnoff(dev);
        }

        @Override
        public void onItemLongClick(View view, int position) {
        }
    };

    private void dialogOnoff(final Device dev) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Push开关: " + dev.getStatus());
        builder.setPositiveButton("开", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reqDevOnoff(1, dev);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("关", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reqDevOnoff(0, dev);
                dialog.dismiss();
            }
        }).show();
    }


    private void reqDevOnoff(int opt, Device dev){
        if(opt == dev.getStatus()){
            return ;
        }

        PCS_UserPushOnoff req = new PCS_UserPushOnoff();
        req.uid = SLService.getInstance().myUID();
        req.devid = dev.getUid();
        if(opt == 1) {
            req.nick = dev.getName(); //dev remark
        }
        req.onoff = opt;
        push.request(PCS_UserPushOnoff.PCS_UserPushOnoff_TYPE, JSON.toJSONString(req));
    }


    private void reqDevOnoffList(){
        User user = ((MainApplication)getActivity().getApplication()).getUser();
        if(user != null){
            PCS_GetUserPushOnoff req = new PCS_GetUserPushOnoff();
            req.uid = SLService.getInstance().myUID();

            push.request(PCS_GetUserPushOnoff.PCS_GetUserPushOnoff_TYPE, JSON.toJSONString(req));
        }
    }

    private Device findDevice(long devid){
        for(int i=0; i<mDeviceList.size(); i++){
            Device dev = mDeviceList.get(i);
            if(dev.getUid() == devid){
                return dev;
            }
        }

        return null;
    }

    private class MySLPushListener extends SLPushListener {
        public void onResponse(int reqtype, int result, String jsondata){
            System.out.println("push, onResponse, reqtype:" + reqtype + ", jsondata:" + jsondata);
            switch(reqtype){
                case PCS_GetUserPushOnoff.PCS_GetUserPushOnoff_TYPE:{
                    if(result != 0) return;

                    PCS_GetUserPushOnoffRes res = JSON.parseObject(jsondata, PCS_GetUserPushOnoffRes.class);
                    if(res.rc == 0 && res.devList != null){
                        for(int i=0; i<res.devList.size(); i++){
                            PushOnoffInfo info = res.devList.get(i);
                            Device dev = findDevice(info.devid);
                            if(dev != null) {
//                                dev.setSid(info.nick);
                                dev.setStatus(info.onoff);
                            }
                        }

                        mRecyclerAdapter.notifyDataSetChanged();

                        havePushOnoff = true;
                    }
                    break;
                }
                case PCS_UserPushOnoff.PCS_UserPushOnoff_TYPE:{
                    if(result != 0) return;

                    PCS_UserPushOnoffRes res = JSON.parseObject(jsondata, PCS_UserPushOnoffRes.class);
                    if(res.rc == 0){
                        handler.sendEmptyMessageDelayed(PUSH_ONOFF_LIST, 100);
                    }
                    break;
                }
            }
        }
    }

    private static final int PUSH_ONOFF_LIST = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PUSH_ONOFF_LIST: {
                    reqDevOnoffList();
                    break;
                }
            }
        }
    };

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
