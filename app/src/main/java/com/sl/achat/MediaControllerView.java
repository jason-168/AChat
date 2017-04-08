package com.sl.achat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MediaControllerView extends FrameLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener{
	
	private static final int MAX_PROGRESS = 1000;
	
    private ImageView mPlayImg;//播放按钮
    private ImageView mExpandImg;//最大化播放按钮
    private ImageView mShrinkImg;//缩放播放按钮
    
    private SeekBar mSeekBar;//播放进度条
    private TextView mTimeStart;//播放时间
    private TextView mTimeEnd;//播放时间
    
    private int mPosition = 0;
    private int mDuration = 0;
    
    private boolean mTimerStarted = false;
    private boolean mPlaying = false;
    
    public MediaControllerView(Context context) {
        super(context);
        initView(context);
    }

    public MediaControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public MediaControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    
    private void initView(Context context) {
        View.inflate(context, R.layout.media_controller_view, this);
        mPlayImg = (ImageView) findViewById(R.id.mc_view_pause);
        mSeekBar = (SeekBar) findViewById(R.id.mc_view_progress);
        mTimeStart = (TextView) findViewById(R.id.mc_view_time_start);
        mTimeEnd = (TextView) findViewById(R.id.mc_view_time_end);
        mExpandImg = (ImageView) findViewById(R.id.mc_view_expand);
        mShrinkImg = (ImageView) findViewById(R.id.mc_view_shrink);
        initData();
    }

    private void initData() {
        mSeekBar.setOnSeekBarChangeListener(this);
        mPlayImg.setOnClickListener(this);
        mShrinkImg.setOnClickListener(this);
        mExpandImg.setOnClickListener(this);
        setPageType(false);
        setPlayState(false);
        
        mSeekBar.setMax(MAX_PROGRESS);
    }
    
    public void setPlayState(boolean isPlaying) {
    	mPlaying = isPlaying;
        mPlayImg.setImageResource(isPlaying ? R.drawable.mc_view_pause : R.drawable.mc_view_play);
    }

    public void setPageType(boolean isExpand) {
        mExpandImg.setVisibility(isExpand ? GONE : VISIBLE);
        mShrinkImg.setVisibility(isExpand ? VISIBLE : GONE);
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
        if (isFromUser){
        	System.out.println("onProgressChanged:" + progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    	System.out.println("onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    	int progress = seekBar.getProgress();
//    	System.out.println("onStopTrackingTouch:" + progress);
    	
    	if(mMediaControllerListener != null && progress > 0){
    		mMediaControllerListener.onMCSeekTo(mDuration * progress / MAX_PROGRESS);
    	}
    }
    
    @Override
    public void onClick(View view) {
    	System.out.println("onClick");
        if (view.getId() == R.id.mc_view_pause) {
        	if(mMediaControllerListener == null){
        		return;
        	}
        	if(mPlaying){
        		if(mMediaControllerListener.onMCPause()){
        			setPlayState(false);
        		}
        	}else{
        		if(mMediaControllerListener.onMCResume()){
        			setPlayState(true);
        		}
        	}
        } 
//        else if (view.getId() == R.id.expand) {
//            mMediaControl.onPageTurn();
//        } else if (view.getId() == R.id.shrink) {
//            mMediaControl.onPageTurn();
//        }
    }
    
    public void setMediaControllerListener(MediaControllerListener l){
    	mMediaControllerListener = l;
    }
    
    public void start(){
    	setPlayState(true);
    }
    
    public void stop(){
    	mTimerStarted = false;
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}
		setPlayState(false);
    }
    
    public void setTime(int start_ts, int total_ms){
    	mPosition = start_ts > 0 ? start_ts : 0;
    	mDuration = mPosition + (total_ms > 0 ? total_ms : 0);
    	
    	if(!mTimerStarted){
    		mTimerStarted = true;
    		if(!handler.hasMessages(MC_TIMER)){
    			handler.sendEmptyMessage(MC_TIMER);
    		}
    	}
    	
    	setDuration(total_ms);
    }
    
    private void timerRun(){
    	if(!mTimerStarted){
    		return;
    	}
    	MediaControllerListener l = mMediaControllerListener;
    	if(l == null){
    		return;
    	}
    	int position = l.onMCGetPosition();
    	if(position > 0){
    		setCurrentTime(position - mPosition);
    		
    		int progress = position * MAX_PROGRESS / mDuration;
    		if(progress > 0){
    			if (progress > MAX_PROGRESS) progress = MAX_PROGRESS;
    			
    			mSeekBar.setProgress(progress);
    		}
    	}
    	
    	handler.sendEmptyMessageDelayed(MC_TIMER, 1000);
    }
    
    private void setCurrentTime(int total){
    	if(total > 0){
	    	int min = total / 60;
	    	int sec = total - min * 60;
	    	mTimeStart.setText(String.format("%02d:%02d", min, sec));
    	}
    }
    
    private void setDuration(int total){
    	if(total > 0){
	    	int min = total / 60;
	    	int sec = total - min * 60;
	    	mTimeEnd.setText(String.format("%02d:%02d", min, sec));
    	}else{
    		mTimeEnd.setText("");
    	}
    }
    
    private static final int MC_TIMER = 1;
    
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MC_TIMER: {
				timerRun();
				break;
			}
			}
		}
	};
	
	public interface MediaControllerListener{
		int onMCGetPosition();
		
		boolean onMCPause();
		boolean onMCResume();
		
		boolean onMCSeekTo(int sec);
	}
	
	private MediaControllerListener mMediaControllerListener = null;
}
