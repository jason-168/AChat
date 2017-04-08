package com.sl.achat;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.sl.media.AVideoRenderer;
import com.sl.media.MView;

public class VideoView {

	protected int order = 0;
	protected RelativeLayout videoLayout = null;
	protected Rect rect = null;
	
	protected AVideoRenderer mRenderer = null;
	protected MView mView = null;
	
	protected int videoWidth = 0;
	protected int videoHeight = 0;
	
	protected ViewTouchListener viewTouchListener = null;
	
	public VideoView(Context context, int order, RelativeLayout layout){
		this.videoLayout = layout;
		setOrder(order);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.videoLayout.getLayoutParams();
		if(params == null){
			params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		params.leftMargin = 0;
		params.topMargin = 0;
		this.videoLayout.setLayoutParams(params);
		
		RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		viewParams.leftMargin = 0;
		viewParams.topMargin = 0;
		
		mView = new MView(context);
		mView.setBackgroundColor(0xff000000);

		layout.addView(mView, viewParams);

		layout.setTag(this);
	}
	
//	public Bitmap getFrame(){
//		return mRenderer.getFrame();
//	}
	
//	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener){
//		mRenderer.setOnVideoSizeChangedListener(listener);
//	}
//	
//	public void clearScreen(){
//		mRenderer.clearScreen();
//	}
	
//	public boolean isPlaying(){
//		return playing;
//	}
	
	public MView getMView(){
		return mView;
	}
	
	public View getLayout(){
		return videoLayout;
	}
	
	public void setViewTouchListener(AVideoRenderer renderer, ViewTouchListener l) {
		this.mRenderer = renderer;
		this.viewTouchListener = l;
		videoLayout.setOnTouchListener(l);
	}
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isDragable(){
		//return order != 0;
		return false;
	}
	
	public boolean contains(int left, int top){
		return rect.contains(left, top);
	}
	
	public LayoutParams getLayoutParams(){
		return videoLayout.getLayoutParams();
	}
	
	public void setLayoutParams(Rect r, int order){
		setRect(r);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)videoLayout.getLayoutParams();
		params.leftMargin = r.left;
		params.topMargin = r.top;
		params.width = r.right;
		params.height = r.bottom;
		videoLayout.setLayoutParams(params);
		
		setOrder(order);
		viewLocateTo(r.right, r.bottom);
	}
	
	public void moveTo(int left, int top){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)videoLayout.getLayoutParams();
		params.leftMargin = left;
		params.topMargin = top;
		videoLayout.setLayoutParams(params);
	}
	
	private void viewLocateTo(int width, int height){
		if(width <= 0 || height <= 0){
			return;
		}
		
		/*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)surfaceView.getLayoutParams();
		params.leftMargin = 1;
		params.topMargin = 1;
		params.width = width -2;
		params.height = height -2;
		surfaceView.setLayoutParams(params);*/
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mView.getLayoutParams();
		params.leftMargin = 0;
		params.topMargin = 0;
		params.width = width;
		params.height = height;
		mView.setLayoutParams(params);
		
		if(!this.isDragable() && viewTouchListener != null){
			viewTouchListener.reset(this, rect.right, rect.bottom, videoWidth, videoHeight);
		}else if(videoWidth > 0 && videoHeight > 0 && mRenderer != null){
			Matrix matrix = new Matrix();
			initMatrix(matrix, videoWidth, videoHeight);
			mRenderer.matrixChangeTo(matrix);
		}
	}
	
	public Rect getRect() {
		return rect;
	}

	public void setRect(Rect r) {
		this.rect = r;
	}
	
	public void moveStart(boolean needsLayer){
//		videoLayout.bringToFront();
	}
	
	public void moveStop(boolean moved){
//		Message m = winHandler.obtainMessage(SURFACE_RESUME, moved);
//		winHandler.sendMessageDelayed(m, (moved == true ? 100 : 0));
	}

	public Matrix scaleTo(Matrix to){
		Matrix m = mRenderer.matrixChangeTo(to);
		return m;
	}
	
	public void winSizeReset(int width, int height) {
		this.videoWidth = width;
		this.videoHeight = height;
		
		if(!this.isDragable() && viewTouchListener != null){
			viewTouchListener.reset(this, rect.right, rect.bottom, width, height);
		}else{
			initMatrix(mRenderer.getMatrix(), width, height);
		}
//		playing = true;
	}
	
	public boolean onViewClick(){
		return false;
	}
	
	/**
	 * 对图片进行初始化操作，包括让图片居中，以及当图片大于屏幕宽高时对图片进行压缩
	 */
	private void initMatrix(Matrix matrix, int videoWidth, int videoHeight) {
		matrix.reset();
		
		int winWidth = rect.right;
		int winHeight = rect.bottom;
		
		if (videoWidth > winWidth || videoHeight > winHeight) {
			if (videoWidth - winWidth > videoHeight - winHeight) {
				// 当图片宽度大于屏幕宽度时，将图片等比例压缩，使它可以完全显示出来
				float ratio = winWidth / (videoWidth * 1.0f);
				matrix.postScale(ratio, ratio);
				float translateY = (winHeight - (videoHeight * ratio)) / 2f;
				// 在纵坐标方向上进行偏移，以保证图片居中显示
				matrix.postTranslate(0, translateY);
			} else {
				// 当图片高度大于屏幕高度时，将图片等比例压缩，使它可以完全显示出来
				float ratio = winHeight / (videoHeight * 1.0f);
				matrix.postScale(ratio, ratio);
				float translateX = (winWidth - (videoWidth * ratio)) / 2f;
				// 在横坐标方向上进行偏移，以保证图片居中显示
				matrix.postTranslate(translateX, 0);
			}
		} else {
			// 当图片的宽高都小于屏幕宽高时，直接让图片居中显示
			float translateX = (winWidth - videoWidth) / 2f;
			float translateY = (winHeight - videoHeight) / 2f;
			matrix.postTranslate(translateX, translateY);
		}
	}
	
}
