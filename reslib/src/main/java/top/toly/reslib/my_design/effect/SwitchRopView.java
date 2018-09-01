package top.toly.reslib.my_design.effect;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Date;

import top.toly.zutils.canvas.MyCanvas;
import top.toly.zutils.core.base.BaseView;
import top.toly.zutils.core.domain.DrawInfo;
import top.toly.zutils.core.domain.Pos;
import top.toly.zutils.core.ui.common.ColUtils;
import top.toly.zutils.logic.Logic;
import top.toly.zutils.num_go.NumGo;


/**
 * 作者：张风捷特烈
 * 时间：2018/7/7:9:04
 * 邮箱：1981462002@qq.com
 * 说明：
 */
public class SwitchRopView extends BaseView {
    private static final String TAG = "SwitchRopView";
    private NumGo mRunNum;
    private float mRate;

    public SwitchRopView(Context context) {
        super(context);
        init();
    }

    public SwitchRopView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchRopView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void init() {
        mRunNum = new NumGo(false, -1, 2000).setOnUpdate(new NumGo.OnUpdate() {
            @Override
            public void onUpdate(float rate) {
                mRate = rate;
                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MyCanvas myCanvas = new MyCanvas(canvas);


        drawRop(myCanvas);
        drawRopXX(myCanvas);
        drawCircle(myCanvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //MeasureSpec 是一个32位的int值，其中int值的高两位为测量模式，低30位为测量的大小。
        //测量模式:
        //1.** EXACTLY--10 :具体数值/match_parent 精确模式
        //2.AT_MOST--11 :wrap_content 此时控件大小是随着子控件或者内容的变化而变化的，只要不超出父控件允许的最大范围即可
        //3.UNSPECIFIED : 它代表没有具体的测量模式，view自己想多大就多大。
    }

    //measureWidth 和measureHeight 是我们自己定义的方法
    //参数则是对应的MeasureSpec对象，包含测量模式和测量值
    public int measuredWidth(int measureSpec) {
        int result = 0;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;//这里的200是自己设置的默认值，单位为px
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);//如果我们的模式是AAT_MOST就取测量值和默认值中的较小一个。
            }
        }
        return result;
    }


    //线的属性
    private float mRopWidth = dip2px(8);
    private float mRopHeight = dip2px(60);
    private float mRopLeft = (float) (mWinSize.x - dip2px(30));
    private int mRopColor = 0xff76C5F5;
    //斜线的属性
    private float mXXWidth = dip2px(8.5f);
    private float mXXHeight = dip2px(5);
    private float mXXCount = 5;
    private int mXXColor = 0xffFBF579;
    //圆圈的属性
    private float mR = dip2px(10);
    private float mRb = dip2px(6);
    private float mRLeft = (float) (mRopLeft + mRopWidth / 2);
    private int mRColor = 0xffC4C4BA;

    //小圈的属性
    private float mR2 = dip2px(7);
    private float mRb2 = dip2px(2);
    private int mR2Color = 0xffffffff;

    private void drawRop(MyCanvas myCanvas) {

        Pos p0 = new Pos(0, 0);
        Pos p1 = new Pos(0, mRopHeight);
        Pos p2 = new Pos(0 + mRopWidth, mRopHeight);
        Pos p3 = new Pos(0 + mRopWidth, 0);


        DrawInfo drawLine = new DrawInfo();
        drawLine.fs = mRopColor;
        drawLine.p = new Pos(mRopLeft, 0);

        myCanvas.drawLines(drawLine, p0, p1, p2, p3);
    }

    private void drawRopXX(MyCanvas myCanvas) {
        DrawInfo drawXX = new DrawInfo();
        Pos p0 = new Pos(0, 0);
        Pos p1 = new Pos(0, mXXHeight);
        Pos p2 = new Pos(mXXWidth, mXXWidth + mXXHeight);
        Pos p3 = new Pos(mXXWidth, mXXWidth);
        drawXX.fs = mXXColor;
        for (int i = 0; i < mXXCount; i++) {

            drawXX.p = new Pos(mRopLeft, mRopHeight / 5 * i);
            myCanvas.drawLines(drawXX, p0, p1, p2, p3);
        }

    }

    private void drawCircle(MyCanvas myCanvas) {
        DrawInfo drawC = new DrawInfo();
        drawC.b = (float) mRb;
        drawC.r = mR;
        drawC.ss = mRColor;


        DrawInfo drawC2 = new DrawInfo();
        drawC2.b = (float) mRb2;
        drawC2.r = mR2;
        drawC2.ang = 45.f;


        drawC2.rot = (float) (360 * mRate);
        drawC2.ss = mR2Color;
        myCanvas.groupMove((float) mRLeft, mRopHeight + mR, drawC, drawC2);

        myCanvas.drawCircle(drawC);
        myCanvas.drawArc(drawC2);
    }


    Pos lastPos = new Pos(0, 0);//最后一次坐标点
    long lastTimestamp = 0L;//最后一次的时间戳
    float downHeight = 0;//下拉总量
    boolean isMove = false;
    boolean isDown = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://0
                mRunNum.go();

                float x0 = event.getX();
                float y0 = event.getY();
                lastPos.x = (float) x0;
                lastPos.y = (float) y0;
                lastTimestamp = new Date().getTime();

                mRColor = ColUtils.randomRGB();
                isDown = true;
                break;
            case MotionEvent.ACTION_UP://1
                mRopHeight = dip2px(60);
                mRunNum.end();
                if (mOnRopUPListener != null && isMove) {
                    mOnRopUPListener.ropUp();
                    isMove = false;
                }
                downHeight = 0;
                isDown = false;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE://2

                float x = event.getX();
                float y = event.getY();
                Pos curPos = new Pos(x, y);//最后一次坐标点

                long curTimestamp = new Date().getTime();
                long t = curTimestamp - lastTimestamp;
                float s = Logic.disPos2d(lastPos, curPos);

                float dataX = (float) (curPos.x - lastPos.x);
                float dataY = (float) (curPos.y - lastPos.y);
                mRopHeight += dataY;
                downHeight += dataY;
                if (downHeight > 50) {
                    isMove = true;
                    if (t > 50) {
                        mRopColor = ColUtils.randomRGB();
                    }
                }

                if (mOnRopDownListener != null) {
                    mOnRopDownListener.ropDown(downHeight);
                }
                lastPos = curPos;//更新位置
                lastTimestamp = curTimestamp;//更新时间
                break;
        }
        return true;
    }


    /////////////////////////////////////////////拉绳拉下监听
    public interface OnRopDownListener {
        void ropDown(float dataY);
    }

    private OnRopDownListener mOnRopDownListener;

    public void setOnRopDownListener(OnRopDownListener onRopDownListener) {
        mOnRopDownListener = onRopDownListener;
    }

    /////////////////////////////////////////////拉绳松开监听
    public interface OnRopUPListener {
        void ropUp();
    }

    private OnRopUPListener mOnRopUPListener;

    public void setOnRopUPListener(OnRopUPListener onRopUPListener) {
        mOnRopUPListener = onRopUPListener;
    }
}
