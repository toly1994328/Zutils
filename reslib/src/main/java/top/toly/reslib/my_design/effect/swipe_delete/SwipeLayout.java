package top.toly.reslib.my_design.effect.swipe_delete;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import top.toly.zutils.core.shortUtils.L;

/**
 * Created by Administrator on 2017/11/18.
 */
public class SwipeLayout extends FrameLayout {

    private View mContentView;
    private View mDeleteView;
    private int mDeleteH;
    private int mDeleteW;
    private int mContentW;
    private ViewDragHelper mViewDragHelper;

    enum SwipeState{
        Open,Close;
    }

    private SwipeState currentState = SwipeState.Close;

    public SwipeLayout(Context context) {
        super(context);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = getChildAt(0);
        mDeleteView = getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDeleteH = mDeleteView.getMeasuredHeight();
        mDeleteW = mDeleteView.getMeasuredWidth();
        mContentW = mContentView.getMeasuredWidth();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mContentView.layout(0, 0, mContentW, mDeleteH);
        mDeleteView.layout(mContentView.getRight(), 0, mContentView.getRight() + mDeleteW, mDeleteH);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = mViewDragHelper.shouldInterceptTouchEvent(ev);

        //*M4:如果当前有打开的，则需要直接拦截，交给onTouch处理
        if(!SwipeLayoutManager.getInstance().isShouldSwipe(this)) {
            //先关闭已经打开的layout
            SwipeLayoutManager.getInstance().closeCurrentLayout();
            result = true;
        }

        return result;
    }

    private float downX,downY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //*M3:如果当前有打开的，则下面的逻辑不能执行
        if(!SwipeLayoutManager.getInstance().isShouldSwipe(this)){
            requestDisallowInterceptTouchEvent(true);
            return true;
        }

        switch (event.getAction()) {
            //*T1:偏向于水平方向，请求listview不要拦截SwipeLayout
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                L.d("result:"+downX);
                break;
            case MotionEvent.ACTION_MOVE:
                //1.获取x和y方向移动的距离
                float moveX = event.getX();
                float moveY = event.getY();
                float delatX = moveX - downX;//x方向移动的距离
                float delatY = moveY - downY;//y方向移动的距离
                if(delatX<0&&Math.abs(delatX)>Math.abs(delatY)||currentState==SwipeState.Open){
                    //表示移动是偏向于水平方向，那么应该SwipeLayout应该处理，请求listview不要拦截
                        requestDisallowInterceptTouchEvent(true);

                }
                //更新downX，downY
                downX = moveX;
                downY = moveY;
                break;//*T1:
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private void init() {

        mViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override//D1:获取
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mContentView || child == mDeleteView;
            }

            @Override//D7:水平触区
            public int getViewHorizontalDragRange(View child) {
                return super.getViewHorizontalDragRange(child);
            }

            @Override//D3:水平移动
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                if (child == mContentView) {//滑动限制
                    if (left > 0) {
                        left = 0;
                    }
                    if (left < -mDeleteW) {
                        left = -mDeleteW;
                    }
                } else if (child == mDeleteView) {
                    if (left > mContentW) {
                        left = mContentW;
                    }
                    if (left < (mContentW - mDeleteW)) {
                        left = mContentW - mDeleteW;
                    }
                }
                return left;
            }

            @Override//D5:伴随移动
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                if (changedView == mContentView) {
                    mDeleteView.layout(mDeleteView.getLeft() + dx, mDeleteView.getTop() + dy,
                            mDeleteView.getRight() + dx, mDeleteView.getBottom() + dy);
                }
                if (changedView == mDeleteView) {
                    mContentView.layout(mContentView.getLeft() + dx, mContentView.getTop() + dy,
                            mContentView.getRight() + dx, mContentView.getBottom() + dy);
                }
                //判断开和关闭的逻辑
                if(mContentView.getLeft()==0 && currentState!=SwipeState.Close) {

                    currentState = SwipeState.Close;//说明应该将state更改为关闭

                    if(listener!=null){//*b3-2:回调接口关闭的方法
                        listener.onClose();
                    }
                    //*M2:说明当前的SwipeLayout已经关闭，需要让Manager清空一下
                    SwipeLayoutManager.getInstance().clearCurrentLayout();

                }else if (mContentView.getLeft()==-mDeleteW && currentState!=SwipeState.Open) {

                    currentState = SwipeState.Open; //说明应该将state更改为开
                    //*b3-1:回调接口打开的方法
                    if(listener!=null){
                        listener.onOpen();
                    }
                    //*M1:当前的Swipelayout已经打开，需要让Manager记录一下
                    SwipeLayoutManager.getInstance().setSwipeLayout(SwipeLayout.this);
                }

            }

            @Override//D6:释放
            public void onViewReleased(View releasedChild, float xvel, float yvel) {

                super.onViewReleased(releasedChild, xvel, yvel);
                if (mContentView.getLeft() < -mDeleteW / 2) {
                    open();
                } else {
                    close();
                }

                //处理用户的稍微滑动
                if(xvel<-500 && currentState!=SwipeState.Open){
                    open();
                }else if (xvel>500 && currentState!=SwipeState.Close) {
                    close();
                }

            }
        });

    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 关闭
     */
    public void open() {
        mViewDragHelper.smoothSlideViewTo(mContentView, -mDeleteW, mContentView.getTop());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 关闭
     */
    public void close() {
        mViewDragHelper.smoothSlideViewTo(mContentView, 0, mContentView.getTop());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * b2:回调监听方法
     */
    private OnSwipeStateChangeListener listener;
    public void setOnSwipeStateChangeListener(OnSwipeStateChangeListener listener){
        this.listener = listener;
    }
    /**
     * *b1:回调接口
     */
    public interface OnSwipeStateChangeListener{
        void onOpen();
        void onClose();
    }
}
