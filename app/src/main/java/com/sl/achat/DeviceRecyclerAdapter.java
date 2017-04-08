package com.sl.achat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sl.db.DB;

import java.util.List;

/**
 * Created by Clock on 2016/7/26.
 */
public class DeviceRecyclerAdapter extends RecyclerView.Adapter<DeviceRecyclerAdapter.DeviceViewHolder> {

    private Context mContext;
    private List<DB.Device> mDeviceList;

    private int mIcon = 0;

    public DeviceRecyclerAdapter(Context context, List<DB.Device> list, int icon) {
        this.mContext = context;
        this.mDeviceList = list;
        mIcon = icon;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View childView = inflater.inflate(R.layout.device_card_layout, parent, false);
        DeviceViewHolder viewHolder = new DeviceViewHolder(childView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        DB.Device device = mDeviceList.get(position);
        if(mIcon != 0) {
            Drawable img = mContext.getResources().getDrawable(mIcon);
            holder.iv_icon.setImageDrawable(img);
        }
        holder.tv_title.setText(device.getSid());
        if(mIcon == R.drawable.ic_contacts || mIcon == R.drawable.ic_videocam || mIcon == R.drawable.ic_cloud) {
            holder.tv_detail.setText((device.getStatus() == 1) ? "在线" : ("离线[" + device.getStatus() + "]"));
        }else{
            holder.tv_detail.setText((device.getStatus() == 1) ? "开" : "关");
        }
    }

    @Override
    public int getItemCount() {
        if (mDeviceList == null) {
            return 0;
        }
        return mDeviceList.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_icon;
        TextView tv_title;
        TextView tv_detail;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            iv_icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_detail = (TextView) itemView.findViewById(R.id.tv_detail);
        }
    }
}
