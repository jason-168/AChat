package com.sl.achat;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sl.SLService;
import com.sl.db.DB;
import com.sl.db.DBService;
import com.sl.protocol.Pbuddy;
import com.sl.usermgr.SLUserManager;
import com.sl.usermgr.SLUserManagerListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView mRecyclerView;
    private DeviceRecyclerAdapter mRecyclerAdapter;
    private ArrayList<DB.Device> mDeviceList = new ArrayList<DB.Device>();


    private int mOpttype;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DeviceFragment() {
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
    public static DeviceFragment newInstance(int type, String param2) {
        DeviceFragment fragment = new DeviceFragment();
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
        View view = inflater.inflate(R.layout.fragment_device, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerAdapter = new DeviceRecyclerAdapter(this.getActivity(), mDeviceList,
                mOpttype == MainApplication.SL_USER ? R.drawable.ic_contacts : R.drawable.ic_videocam);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mRecyclerView.addOnItemTouchListener(new SimpleOnItemClickListener(mRecyclerView,onClickListener));
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
    }
    @Override
    public void onResume() {
        super.onResume();
        DB.User user = ((MainApplication)this.getActivity().getApplication()).getUser();
        if(user != null){
            DBService db = new DBService(this.getActivity());
            updateStatus(db, user);
            db.close();
        }
    }

    private void updateStatus(DBService db, DB.User user) {
        db.queryDevice(mDeviceList, DB.Device.SELECT_BY_TYPE, new String[]{""+user.getId(), "" + mOpttype});
        mRecyclerAdapter.notifyDataSetChanged();
    }

    public void notifyDataSetChanged(DBService db, DB.User user){
        if(db!= null && user != null) {
            updateStatus(db, user);
        }else{
            mDeviceList.clear();
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }


    private SimpleOnItemClickListener.OnItemClickListener onClickListener = new SimpleOnItemClickListener.OnItemClickListener(){
        @Override
        public void onItemClick(View view, int position) {
            if(mOpttype == MainApplication.SL_USER) {
                callDialog(position);
            }else{
                call(0, position);
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
            System.out.println("onItemLongClick");
            delDialog(position);
        }
    };

    private void callDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setPositiveButton("视频通话", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                call(3, position);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("语音通话", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                call(2, position);
                dialog.dismiss();
            }
        }).show();
    }

    private void call(int type, int position){
        DB.Device dev = mDeviceList.get(position);

        Intent intent;
        if(mOpttype == MainApplication.SL_USER) {
            if(type == 3){
                intent = new Intent(getActivity(), VideoCallActivity.class);
            }else{
                intent = new Intent(getActivity(), VoiceCallActivity.class);
            }
            intent.putExtra("dial", true);
        }else{
            intent = new Intent(getActivity(), DevicePreviewActivity.class);
        }

        intent.putExtra("sid", dev.getSid());
        intent.putExtra("uid", dev.getUid());
        intent.putExtra("uname", dev.getUsername());
        intent.putExtra("pwd", dev.getPasswd());
        startActivity(intent);
    }

    private void delDialog(final int position) {
        final DB.Device dev = mDeviceList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setMessage("确定删除"+dev.getSid()+"吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 删除确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {// 确定按钮的响应事件
                Pbuddy.BuddyDelReq req = new Pbuddy.BuddyDelReq();
                req.uid = SLService.getInstance().myUID();
                req.bid = dev.getUid();

                SLUserManager mgr = SLUserManager.get();
                mgr.setSLUserManagerListener(userManagerListener);

                mgr.request(Pbuddy.BuddyDelReq.SL_USERMGR_REQ_DEL, JSON.toJSONString(req));

                dialog.dismiss();

                delfromDB(dev);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {// 返回按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {// 响应事件
                dialog.dismiss();
            }
        }).show();
    }

    private void delfromDB(DB.Device dev){
        DB.User user = ((MainApplication)this.getActivity().getApplication()).getUser();

        DBService db = new DBService(this.getActivity());
        db.executeSQL(DB.Device.DEL_BY_ID, new Object[] {dev.getId()});
        updateStatus(db, user);
        db.close();
    }

    private SLUserManagerListener userManagerListener = new SLUserManagerListener() {
        @Override
        public void onResponse(int reqtype, int result, String jsondata){
            switch(reqtype){
                case Pbuddy.BuddyDelReq.SL_USERMGR_REQ_DEL:{
                    if(result == 0){
                        Pbuddy.BuddyDelRes res = JSON.parseObject(jsondata, Pbuddy.BuddyDelRes.class);
                        if(res.rc == 0){
                            Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
            }
        }
    };

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


}
