package com.sl.achat;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import com.sl.db.DB.Device;
import com.sl.db.DB.User;
import com.sl.db.DBService;
import com.sl.protocol.PStorageVerify;
import com.sl.storage.SLStorageMgr;
import com.sl.storage.SLStorageMgrListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StorageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StorageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StorageFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView mRecyclerView;
    private DeviceRecyclerAdapter mRecyclerAdapter;
    private ArrayList<Device> mDeviceList = new ArrayList<Device>();


    private SLStorageMgr storageMgr = null;

    private int mOpttype;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public StorageFragment() {
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
    public static StorageFragment newInstance(int type, String param2) {
        StorageFragment fragment = new StorageFragment();
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
        mRecyclerAdapter = new DeviceRecyclerAdapter(this.getActivity(), mDeviceList, R.drawable.ic_cloud);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mRecyclerView.addOnItemTouchListener(new SimpleOnItemClickListener(mRecyclerView,onClickListener));

        List<PStorageVerify.StorageVasInfo> vlist = ((MainApplication) getActivity().getApplication()).getVlist();
        if(vlist == null) {
            handler.sendEmptyMessageDelayed(STORAGE_VAS_REQ, 100);
        }

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

        if(storageMgr != null){
            storageMgr.setSLStorageMgrListener(null);
        }

        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        List<PStorageVerify.StorageVasInfo> vlist = ((MainApplication) getActivity().getApplication()).getVlist();
        if(vlist != null){
            showStorageVas(vlist);
        }
    }

    private SimpleOnItemClickListener.OnItemClickListener onClickListener = new SimpleOnItemClickListener.OnItemClickListener(){
        @Override
        public void onItemClick(View view, int position) {
            Device dev = mDeviceList.get(position);

            Intent intent = new Intent(getActivity(), DevicePreviewActivity.class);
            intent.putExtra("sid", dev.getSid());
            intent.putExtra("uid", dev.getUid());
            intent.putExtra("uname", dev.getUsername());
            intent.putExtra("pwd", dev.getPasswd());
            startActivity(intent);
        }

        @Override
        public void onItemLongClick(View view, int position) {
        }
    };

    private void reqStorageVas() {
        storageMgr = SLStorageMgr.get();
        storageMgr.setSLStorageMgrListener(new StorageFragment.MySLStorageMgrListener());

        PStorageVerify.PCS_StorageVas4User vas = new PStorageVerify.PCS_StorageVas4User();
        storageMgr.request(PStorageVerify.PCS_StorageVas4User.SL_STORAGEMGR_REQ_VAS4USER, JSON.toJSONString(vas));
    }

    private void showStorageVas(List<PStorageVerify.StorageVasInfo> vlist){
        User user = ((MainApplication)getActivity().getApplication()).getUser();
        if(user != null){
            DBService db = new DBService(this.getActivity());
            updateStatus(db, user, vlist);
            db.close();
        }
    }

    private boolean isInVasList(Device dev, List<PStorageVerify.StorageVasInfo> vlist){
        for(int i=0; i<vlist.size(); i++){
            if(dev.getUid() == vlist.get(i).devid){
                return true;
            }
        }
        return false;
    }

    private void updateStatus(DBService db, User user, List<PStorageVerify.StorageVasInfo> vlist) {
        mDeviceList.clear();

        ArrayList<Device> devices = new ArrayList<Device>();
        db.queryDevice(devices, Device.SELECT_BY_TYPE, new String[]{""+user.getId(), "" + mOpttype});
        for(int i=0; i<devices.size(); i++){
            Device dev = devices.get(i);
            if(isInVasList(dev, vlist)){
                mDeviceList.add(dev);
            }
        }

        mRecyclerAdapter.notifyDataSetChanged();
    }


    private class MySLStorageMgrListener extends SLStorageMgrListener {
        @Override
        public void onResponse(int reqtype, int result, String jsondata) {
            System.out.println("Storage, onResponse, reqtype:" + reqtype + ", jsondata:" + jsondata);
            if(PStorageVerify.PCS_StorageVas4User.SL_STORAGEMGR_REQ_VAS4USER == reqtype){
                if(result != 0) return;

                PStorageVerify.PCS_StorageVas4UserRes res = JSON.parseObject(jsondata, PStorageVerify.PCS_StorageVas4UserRes.class);
                if(res.rc == 0 && res.vlist != null && res.vlist.size() > 0) {
                    ((MainApplication) getActivity().getApplication()).setStorageVasInfoList(res.vlist);
                    showStorageVas(res.vlist);
                }
            }
        }
    }

    private static final int STORAGE_VAS_REQ    = 31;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STORAGE_VAS_REQ:{
                    reqStorageVas();
                    break;
                }
            }
        }
    };

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
