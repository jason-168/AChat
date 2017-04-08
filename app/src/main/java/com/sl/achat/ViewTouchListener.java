package com.sl.achat;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ViewTouchListener implements OnTouchListener{
	
	public static final int MOVE_DISTANCE = 100;
	
	/**
	 * 图片放大状态常量
	 */
	public static final int STATUS_ZOOM_IN = 2;

	/**
	 * 图片缩小状态常量
	 */
	public static final int STATUS_ZOOM_OUT = 3;
	
	/**
	 * 用于对图片进行移动和缩放变换的矩阵
	 */
	protected Matrix matrix = new Matrix();
	
	/**
	 * 屏幕的宽度,高度
	 */
	protected int screenWidth, screenHeight;
	
	/**
	 * 记录当前操作的状态，可选值为STATUS_ZOOM_OUT,STATUS_ZOOM_IN
	 */
	protected int currentStatus;
	
	/**
	 * View控件的宽,高度
	 */
	protected int viewWidth, viewHeight;
	
	/**
	 * 图片的宽,高度
	 */
	protected int videoWidth, videoHeight;

	/**
	 * 记录两指同时放在屏幕上时，中心点的横坐标  x:centerPoint[0], y:centerPoint[1]
	 */
	protected float centerPoint[] = {0, 0};

	/**
	 * 记录当前图片的宽,高度 图片被缩放时, 这个值会一起变动
	 */
	protected float currentBitmapWidth, currentBitmapHeight;

	/**
	 * 记录上次手指移动时的坐标, x: lastMovePoint[0], y: lastMovePoint[1]
	 */
	protected float lastMovePoint[] = {-1, -1};

	/**
	 * 记录手指横坐标方向上的移动距 movedDistance[0], 纵坐标方向上的移动距 movedDistance[1]:
	 */
	protected float movedDistance[] = {0, 0};

	/**
	 * 记录图片在矩阵上的横向偏移:totalTranslate[0], 纵向偏移totalTranslate[1]
	 */
	protected float totalTranslate[] = {0, 0};

	/**
	 * 记录图片在矩阵上的缩放比例
	 */
	protected float totalRatio;

	/**
	 * 记录手指移动的距离所造成的缩放比例
	 */
	protected float scaledRatio;

	/**
	 * 记录图片初始化时的缩放比例
	 */
	protected float initedRatio;
	
	/**
	 * 记录图片最小缩放比例
	 */
	protected float minRatio;

	/**
	 * 记录上次两指之间的距离
	 */
	protected double lastFingerDis;
	
	protected VideoView wins[] = null;
	
	public ViewTouchListener(VideoView vw[]){
		this.wins = vw;
	}
	
	/**
	 * 计算两个手指之间的距离
	 * 
	 * @param event
	 * @return 两个手指之间的距离
	 */
	protected double distanceBetweenFingers(MotionEvent event) {
		float disX = Math.abs(event.getX(0) - event.getX(1));
		float disY = Math.abs(event.getY(0) - event.getY(1));
		return Math.sqrt(disX * disX + disY * disY);
	}

	/**
	 * 计算两个手指之间中心点的坐标
	 * 
	 * @param event
	 */
	protected void centerPointBetweenFingers(MotionEvent event) {
		centerPoint[0] = (event.getX(0) + event.getX(1)) / 2;
		centerPoint[1] = (event.getY(0) + event.getY(1)) / 2;
	}
	
	protected boolean hasZoomIn() {
		return (totalRatio > initedRatio);
	}
	
	/**
	 * 对图片进行缩放处理
	 * 
	 * @param win
	 */
	protected void zoom(VideoView win) {
		matrix.reset();
		// 将图片按总缩放比例进行缩放
		matrix.postScale(totalRatio, totalRatio);
		float scaledWidth = videoWidth * totalRatio;
		float scaledHeight = videoHeight * totalRatio;
		float translateX = 0f;
		float translateY = 0f;
		// 如果当前图片宽度小于屏幕宽度，则按屏幕中心的横坐标进行水平缩放.否则按两指的中心点的横坐标进行水平缩放
		if (currentBitmapWidth < viewWidth) {
			translateX = (viewWidth - scaledWidth) / 2f;
		} else {
			translateX = totalTranslate[0] * scaledRatio + centerPoint[0] * (1 - scaledRatio);
			// 进行边界检查，保证图片缩放后在水平方向上不会偏移出屏幕
			if (translateX > 0) {
				translateX = 0;
			} else if (viewWidth - translateX > scaledWidth) {
				translateX = viewWidth - scaledWidth;
			}
		}
		// 如果当前图片高度小于屏幕高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
		if (currentBitmapHeight < viewHeight) {
			translateY = (viewHeight - scaledHeight) / 2f;
		} else {
			translateY = totalTranslate[1] * scaledRatio + centerPoint[1] * (1 - scaledRatio);
			// 进行边界检查，保证图片缩放后在垂直方向上不会偏移出屏
			if (translateY > 0) {
				translateY = 0;
			} else if (viewHeight - translateY > scaledHeight) {
				translateY = viewHeight - scaledHeight;
			}
		}
		// 缩放后对图片进行偏移，以保证缩放后中心点位置不变
		matrix.postTranslate(translateX, translateY);
		totalTranslate[0] = translateX;
		totalTranslate[1] = translateY;
		currentBitmapWidth = scaledWidth;
		currentBitmapHeight = scaledHeight;
		
		matrix = win.scaleTo(matrix);
	}
	
	/**
	 * 对图片进行平移处理
	 * 
	 * @param win
	 */
	protected void move(VideoView win) {
		matrix.reset();
		// 根据手指移动的距离计算出总偏移值
		float translateX = totalTranslate[0] + movedDistance[0];
		float translateY = totalTranslate[1] + movedDistance[1];
		// 先按照已有的缩放比例对图片进行缩放
		matrix.postScale(totalRatio, totalRatio);
		// 再根据移动距离进行偏移
		matrix.postTranslate(translateX, translateY);
		totalTranslate[0] = translateX;
		totalTranslate[1] = translateY;
		matrix = win.scaleTo(matrix);
	}
	
	public void reset(VideoView win, int viewWidth, int viewHeight, int videoWidth, int videoHeight){
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;
		
		matrix.reset();
		
		float ratio = 1f;
		int right, bottom;
		
		if (videoWidth > viewWidth || videoHeight > viewHeight) {
			float videoRatio = (float)videoWidth / (float)videoHeight;
			if(videoWidth > viewWidth){
				right = viewWidth;
				bottom = (int)(right / videoRatio);
				ratio = viewWidth / (videoWidth * 1.0f);
			}else{
				right = videoWidth;
				bottom = videoHeight;
			}
			if(bottom > viewHeight){
				if(videoHeight > viewHeight){
					bottom = viewWidth;
					right = (int)(bottom * videoRatio);
					ratio = viewHeight / (videoHeight * 1.0f);
				}else{
					bottom = videoHeight;
					right = videoWidth;
				}
			}
			minRatio = ratio;
		} else {
			// 当图片的宽高都小于屏幕宽高时，直接让图片居中显示
			float widthRatio = (float)viewWidth / (float)videoWidth;
			float heightRatio = (float)viewHeight / (float)videoHeight;
			
			if(widthRatio <= heightRatio){
				ratio = widthRatio;
				right = viewWidth;
				bottom = (int)(videoHeight * ratio);
			}else{
				ratio = heightRatio;
				bottom = viewHeight;
				right = (int)(videoWidth * ratio);
			}
			
			minRatio = 1f;
		}
		
		matrix.postScale(ratio, ratio);
		totalTranslate[0] = (viewWidth - (videoWidth * ratio)) / 2f;
		totalTranslate[1] = (viewHeight - (videoHeight * ratio)) / 2f;
		matrix.postTranslate(totalTranslate[0], totalTranslate[1]);
		currentBitmapWidth = right;
		currentBitmapHeight = bottom;
		
		totalRatio = initedRatio = ratio;
		
//		System.out.println("ratio: " + ratio + ",totalTranslate[0]: " + totalTranslate[0] + ", totalTranslate[1]: " + totalTranslate[1]);
//		System.out.println("viewWidth: " + viewWidth + ", viewHeight: " + viewHeight);
//		System.out.println("currentBitmapWidth: " + currentBitmapWidth + ", currentBitmapHeight: " + currentBitmapHeight);
		
		matrix = win.scaleTo(matrix);
	}
	
	public void setScreenSize(int width, int height){
		this.screenWidth = width;
		this.screenHeight = height;
	}
	
	protected void onActionDown(View v, MotionEvent event){
		VideoView win = (VideoView) v.getTag();
		if(win.isDragable()){
			lastMovePoint[0] = (int) event.getRawX();
			lastMovePoint[1] = (int) event.getRawY();
			win.moveStart(true);
		}
	}
	
	protected void onActionPointerDown(View v, MotionEvent event){
		VideoView win = (VideoView) v.getTag();
		if (!win.isDragable() && event.getPointerCount() == 2) {
			// 当有两个手指按在屏幕上时，计算两指之间的距离
			lastFingerDis = distanceBetweenFingers(event);
		}
	}
	
	protected void onActionMove(View v, MotionEvent event){
		VideoView win = (VideoView) v.getTag();
		if(!win.isDragable()){
			if (event.getPointerCount() == 1) {
				// 只有单指按在屏幕上移动时，为拖动状态
				float xMove = event.getX();
				float yMove = event.getY();
				if (lastMovePoint[0] == -1 && lastMovePoint[1] == -1) {
					lastMovePoint[0] = xMove;
					lastMovePoint[1] = yMove;
				}
				movedDistance[0] = xMove - lastMovePoint[0];
				movedDistance[1] = yMove - lastMovePoint[1];
				// 进行边界，不允许将图片拖出边界
				if (totalTranslate[0] + movedDistance[0] > 0) {
					movedDistance[0] = 0;
				} else if (viewWidth - (totalTranslate[0] + movedDistance[0]) > currentBitmapWidth) {
					movedDistance[0] = 0;
				}
				if (totalTranslate[1] + movedDistance[1] > 0) {
					movedDistance[1] = 0;
				} else if (viewHeight - (totalTranslate[1] + movedDistance[1]) > currentBitmapHeight) {
					movedDistance[1] = 0;
				}
				// 调用Draw()方法绘制图片
				move(win);
				lastMovePoint[0] = xMove;
				lastMovePoint[1] = yMove;
			} else if (event.getPointerCount() == 2) {
				// 有两个手指按在屏幕上移动时，为缩放状态
				centerPointBetweenFingers(event);
				double fingerDis = distanceBetweenFingers(event);
				if (fingerDis > lastFingerDis) {
					currentStatus = STATUS_ZOOM_IN;
				} else {
					currentStatus = STATUS_ZOOM_OUT;
				}
				// 进行缩放倍数，最大只允许将图片放4倍，可以缩小到初始化比例
				if ((currentStatus == STATUS_ZOOM_IN && totalRatio < 4f) || (currentStatus == STATUS_ZOOM_OUT && totalRatio > minRatio)) {
					scaledRatio = (float) (fingerDis / lastFingerDis);
					totalRatio = totalRatio * scaledRatio;
					if (totalRatio > 4f) {
						totalRatio = 4f;
					} else if (totalRatio < minRatio) {
						totalRatio = minRatio;
					}
					// 调用Draw()方法绘制图片
					zoom(win);
					lastFingerDis = fingerDis;
				}
			}
		}else{
			int dx = (int) event.getRawX() - (int)lastMovePoint[0];
			int dy = (int) event.getRawY() - (int)lastMovePoint[1];
			int left = v.getLeft() + dx;
			int top = v.getTop() + dy;
			int right = v.getRight() + dx;
			int bottom = v.getBottom() + dy;

			if (left < 0) {
				left = 0;
				right = left + v.getWidth();
			}else if (right > screenWidth) {
				right = screenWidth;
				left = right - v.getWidth();
			}

			if (top < 0) {
				top = 0;
				bottom = top + v.getHeight();
			}else if (bottom > screenHeight) {
				bottom = screenHeight;
				top = bottom - v.getHeight();
			}

			win.moveTo(left, top);
			lastMovePoint[0] = event.getRawX();
			lastMovePoint[1] = event.getRawY();
		}
	}
	
	protected void onActionPointerUp(View v, MotionEvent event){
		VideoView win = (VideoView) v.getTag();
		if (!win.isDragable() && (event.getPointerCount() == 2)) {
			// 手指离开屏幕时将临时值还
			lastMovePoint[0] = -1;
			lastMovePoint[1] = -1;
		}
	}
	
	protected void onActionUp(View v, MotionEvent event){
		VideoView win = (VideoView) v.getTag();
		if(win.isDragable()){
			VideoView vw_temp = null;
			for (int i = 0; i < wins.length; i++) {
				if (win != wins[i]) {
					if (!wins[i].isDragable() && wins[i].contains(v.getLeft() + v.getWidth() / 2,
							v.getTop() + v.getHeight() / 2)) {
						vw_temp = wins[i];
						break;
					}
				}
			}

			if (vw_temp != null) {
				Rect t = vw_temp.getRect();
				int order_temp = vw_temp.getOrder();
				vw_temp.moveStart(false);
				vw_temp.setLayoutParams(win.getRect(), win.getOrder());
				
				win.setLayoutParams(t, order_temp);
				vw_temp.moveStop(true);
			} else {
				win.setLayoutParams(win.getRect(), win.getOrder());
			}

			win.moveStop(vw_temp != null);
		}
		
		// 手指离开屏幕时将临时值还
		lastMovePoint[0] = -1;
		lastMovePoint[1] = -1;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:{
			onActionDown(v, event);
			break;
		}
		case MotionEvent.ACTION_POINTER_DOWN:
			onActionPointerDown(v, event);
			break;
		case MotionEvent.ACTION_MOVE:
			onActionMove(v, event);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			onActionPointerUp(v, event);
			break;
		case MotionEvent.ACTION_UP:
			onActionUp(v, event);
			break;
		default:
			return false;
		}
		return true;
	}

}
