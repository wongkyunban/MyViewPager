package com.wong.support;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.List;

public class WonViewPager extends ViewGroup {

    /*要轮翻播放的图片*/
    private List<Integer> images;

    public WonViewPager(Context context) {
        super(context);
    }

    public WonViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WonViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WonViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /*批量设置轮播图片*/
    public void setImages(List<Integer> images) {
        this.images = images;
        updateViews();
    }

    /*将子视图添加到ViewGroup容器中*/
    private void updateViews() {
        for (int i = 0; i < images.size(); i++) {
            ImageView iv = new ImageView(getContext());
            iv.setBackgroundResource(images.get(i));
            this.addView(iv);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.layout(i * getWidth(), t, (i + 1) * getWidth(), b);
        }
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int startX = 0;
            switch (e1.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = (int) e1.getX();
                    break;
            }

            boolean noScroll = false;
            switch (e2.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    int endX = (int) e2.getX();
                    int dx = endX - startX;
                    if (dx < 0) {
                        if (scrollX >= getWidth() * (images.size() - 1)) {
                            noScroll = true;
                        }
                    }
                    if (dx > 0) {
                        if (scrollX <= 0) {
                            noScroll = true;
                        }
                    }
                    break;
                default:
                    break;
            }
            if(!noScroll) {
                scrollBy((int) distanceX, 0);
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    });

    /*记录当前视图的序号*/
    private int position;
    private Scroller scroller = new Scroller(getContext());
    private int scrollX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件传递手势识别器
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            /*按下*/
            case MotionEvent.ACTION_DOWN:

                break;
            /*移动，在ACTION_DOWN和ACTION_UP之间*/
            case MotionEvent.ACTION_MOVE:
                /*返回视图正在展示部分的左边滚动位置（即返回滚动的视图的左边位置）*/
                scrollX = getScrollX();
                /*加上父视图的一半*/
                int totalWidth = scrollX + getWidth() / 2;
                /*计算视图划过一半后的下一个视图的序号*/
                position = totalWidth / getWidth();
                /* scrollX >= getWidth() * (images.size() - 1)说明是最后一张，那么我们就不能让其出界，否则它是可以滑出界的*/
                if (scrollX >= getWidth() * (images.size() - 1)) {
                    position = images.size() - 1;
                }
                /*scrollX < 0说明左边滑入界了，即第一张视图的左边偏右，距离父视图左边之间的距离出现空白*/
                if (scrollX <= 0) {
                    position = 0;
                }
                break;
            /*抬起手指*/
            case MotionEvent.ACTION_UP:

                /*滑动到指定位置*/
                // scrollTo(position*getWidth(),0);
                /*平滑移动到指定位置*/
                scroller.startScroll(scrollX, 0, -(scrollX - position * getWidth()), 0);
                /*从UI线程触发视图更新*/
                invalidate();

        }
        return true;
    }

    /**
     * Called by a parent to request that a child update its values for mScrollX
     * and mScrollY if necessary. This will typically be done if the child is
     * animating a scroll using a {@link android.widget.Scroller Scroller}
     * object.
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            /**
             * 每次x轴有变化都会移动一点，那么要持续变化完，就要调用postInvalidate()持续刷新视图，
             * 而上面的invalidate()方法只负责第一次触发computeScroll()调用，剩下的都是postInvalidate()触发的
             */
            scrollTo(scroller.getCurrX(), 0);
            /*从非ＵＩ线程触发视图更新，只有调用*/
            postInvalidate();
        }
    }
}
