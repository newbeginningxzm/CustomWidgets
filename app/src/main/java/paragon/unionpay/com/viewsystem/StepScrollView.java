package paragon.unionpay.com.viewsystem;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by Paragon-xzm on 2017/5/10.
 */
public class StepScrollView extends LinearLayout{
    private static final String TAG = "StepScrollView";

    private float mInitMotionX;
    private float mInitMotionY;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mPointerId;
    private int mTouchSlop;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mVXFlingLimit;
    private int mVYFlingLimit;
    private int mXFlingLimit;
    private int mYFlingLimit;
    private boolean mIsBeingDraged;

    public StepScrollView(Context context) {
        super(context);
        init();
    }

    public StepScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StepScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mScroller = new Scroller(getContext());
        mVXFlingLimit = configuration.getScaledMinimumFlingVelocity();
        mVYFlingLimit = 2;
        mXFlingLimit = 500;
        mYFlingLimit = 200;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float currentX = ev.getRawX();
        float currentY = ev.getRawY();
        int xDiff = (int) (currentX - mLastMotionX);
        int yDiff = (int) (currentY - mLastMotionY);
//        Log.d(TAG, "onInterceptTouchEvent action:"+ev.getAction()+" currentX:"+currentX+" currentY:"+currentY);
        switch (ev.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                // for child can receive MotionEvent
                mPointerId = MotionEventCompat.getPointerId(ev, 0);
                mVelocityTracker = VelocityTracker.obtain();
                mInitMotionX = ev.getRawX();
                mInitMotionY = ev.getRawY();
                mIsBeingDraged =false;
//                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                return false;
            case MotionEvent.ACTION_MOVE:
                mIsBeingDraged = isEventInOrientation(currentX, currentY);
//                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE isEventInOrientation:" + mIsBeingDraged + " xDiff:" + xDiff + " yDiff:" + yDiff);
                return mIsBeingDraged;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // for child can perform click;
//                Log.d(TAG, "onInterceptTouchEvent ACTION_UP");
                return mIsBeingDraged;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getRawX();
        float currentY = event.getRawY();
//        Log.d(TAG, "onTouchEvent action:" + event.getAction() + " lastX:" + mLastMotionX + " currentX:" + currentX + " lastY:" + mLastMotionY + " currentY:" + currentY + " mTouchSlop:" + mTouchSlop);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mScroller.abortAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                if(isEventInOrientation(currentX, currentY)){
                    mVelocityTracker.addMovement(event);
                    scrollInternal(currentX, currentY);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                flingInternal(currentX, currentY);
                scrollToDefault();
                mVelocityTracker.recycle();
                break;
        }
        mLastMotionX = event.getRawX();
        mLastMotionY = event.getRawY();
        boolean result = super.onTouchEvent(event);
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(isEventInOrientation(currentX, currentY) &&!result){
                return true;
            }
        }
        return result;
    }

    protected boolean isEventInOrientation(float currentX, float currentY){
        float xDiff = currentX - mLastMotionX;
        float yDiff = currentY - mLastMotionY;
        if(getOrientation() == HORIZONTAL){
            return Math.abs(xDiff) * 0.5f >= Math.abs(yDiff);
        }else{
            return Math.abs(yDiff) * 0.5f >= Math.abs(xDiff);
        }
    }

    protected void scrollInternal(float currentX, float currentY){
        if(getOrientation() == HORIZONTAL){
            scrollBy(getScrollDistance(currentX, currentY), 0);
        }else{
            scrollBy(0, getScrollDistance(currentX, currentY));
        }
        invalidate();
    }

    protected void flingInternal(float currentX, float currentY){
        mScroller.forceFinished(false);
        mVelocityTracker.computeCurrentVelocity(1000);
        int Vx = (int) mVelocityTracker.getXVelocity();
        int Vy = (int) mVelocityTracker.getYVelocity();
        Log.d(TAG, "flingInternal vx:" + Vx + " vy:" + Vy);
        if(getOrientation() == HORIZONTAL){
            if(Math.abs(Vx) >= mVXFlingLimit){
                Log.d(TAG, "flingInternal vx:"+Vx+" vy:"+Vy);
                mScroller.fling(getScrollX(), 0, -Vx, 0, -mXFlingLimit, mXFlingLimit, 0, 0);
                invalidate();
            }
        }else{
            if(Math.abs(Vy) >= mYFlingLimit){
                mScroller.fling(0, getScrollY(), 0, -Vy, 0, 0, -mYFlingLimit, mYFlingLimit);
                invalidate();
            }
        }
    }

    protected void scrollToDefault(){
        int childCount = getChildCount();
        if(childCount < 2){
            scrollTo(0, 0);
            return;
        }
        int scrollDistance = getToDefaultDistance();
        if(getOrientation() == HORIZONTAL){
            mScroller.startScroll(getScrollX(), 0 , scrollDistance, 0);
        }else{
            mScroller.startScroll(0, getScrollY(), 0, scrollDistance);
        }
        invalidate();
    }

   protected int getScrollDistance(float currentX, float currentY){
       return (int) (getOrientation() == HORIZONTAL ? mLastMotionX - currentX : mLastMotionY - currentY);
   }

   protected int getToDefaultDistance(){
       int scrollX = getScrollX();
       int scrollY = getScrollY();
       int absDistance = getOrientation() == HORIZONTAL ? (scrollX >= 0 ? scrollX : getWidth() + scrollX) : ( scrollY >= 0 ? scrollY : getHeight() + scrollY);
       int scrollDistance = 0;
       int childCount = getChildCount();
       if(getOrientation() == HORIZONTAL){
           if(scrollX > 0 ){
               View child = getChildAt(childCount - 1);
               if(child != null){
                   if(childCount > 0 && isChildVisible(getChildAt(0)) && isChildVisible(getChildAt(childCount - 1))){
                        return -scrollX;
                   }
                   int margin = 0;
                   if(child.getLayoutParams() instanceof MarginLayoutParams){
                       margin = ((MarginLayoutParams) child.getLayoutParams()).rightMargin;
                   }
                   int realRight = (int) (child.getX() + child.getWidth() - scrollX);
                   Log.d(TAG, "child" + (childCount-1) + " right:" + realRight + " marginRight:" + (getWidth() - margin));
                   if(realRight < getWidth() - margin - getPaddingRight()){
                       return - getWidth() + margin + realRight;
                   }
               }
           }else if(scrollX == 0){
                return 0;
           }else{
               View child = getChildAt(0);
               int margin = 0;
               if(child != null){
                   if(child.getLayoutParams() instanceof MarginLayoutParams){
                       margin = ((MarginLayoutParams) child.getLayoutParams()).leftMargin;
                   }
                   int realLeft = (int) (child.getX() - scrollX);
                   Log.d(TAG, "child0 x:"+realLeft+" marginLeft:"+margin);
                   if(realLeft > margin + getPaddingLeft()){
                        return (int) (child.getX() - scrollX - margin);
                   }
               }
           }
       }else{
           if(scrollY > 0 ){
               View child = getChildAt(childCount - 1);
               if(child != null){
                   if(childCount > 0 && isChildVisible(getChildAt(0)) && isChildVisible(child)){
                       return -scrollY;
                   }
                   int margin = 0;
                   if(child.getLayoutParams() instanceof MarginLayoutParams){
                       margin = ((MarginLayoutParams) child.getLayoutParams()).bottomMargin;
                   }
                   int realBottom = (int) (child.getY() + child.getHeight() - scrollY);
                   Log.d(TAG, "child" + (childCount-1) + " bottom:" + realBottom + " marginBottom:" + (getWidth() - margin - getPaddingBottom()));
                   if(realBottom < getHeight() - margin - getPaddingBottom()){
                       return - getHeight() + margin + getPaddingBottom() + realBottom;
                   }
               }
           }else if(scrollY == 0){
               return 0;
           }else{
               View child = getChildAt(0);
               int margin = 0;
               if(child != null){
                   if(child.getLayoutParams() instanceof MarginLayoutParams){
                       margin = ((MarginLayoutParams) child.getLayoutParams()).topMargin;
                   }
                   int realTop = (int) (child.getY() - scrollY);
                   Log.d(TAG, "child0 y:"+realTop+" marginTop:"+margin);
                   if(realTop > margin + getPaddingTop()){
                       return realTop - margin - getPaddingTop();
                   }
               }
           }
       }
       for(int i = 0; i < childCount; i++){
           View child = getChildAt(i);
           if(child != null){
               int childLength = getSingeCellLength(child);
               if(absDistance > childLength){
                   absDistance -= childLength;
               }else if(childLength == absDistance){
                   scrollDistance = 0;
                   break;
               }else{
                   if(absDistance/(childLength + 0.0f) >= 0.5f){
                       scrollDistance = childLength - absDistance;
                   }else{
                       scrollDistance = -absDistance;
                   }
                   break;
               }
           }
       }
       return scrollDistance;
   }

   private int getSingeCellLength(View child){
       if(getOrientation() == HORIZONTAL){
           int margin = 0;
           if(child.getLayoutParams() instanceof MarginLayoutParams){
               MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
               margin = params.leftMargin + params.rightMargin;
           }
           return child.getWidth() + margin;
       }else{
           int margin = 0;
           if(child.getLayoutParams() instanceof MarginLayoutParams){
               MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
               margin = params.topMargin + params.bottomMargin;
           }
           return child.getHeight() + margin;
       }
   }

    private boolean isChildVisible(View child){
        if(child != null){
            int realLeft;
            int realRight;
            int width;
            int paddingLeft;
            int paddingRight;
            if(getOrientation() == HORIZONTAL){
                realLeft = (int) (child.getX() - getScrollX());
                realRight = realLeft + child.getWidth();
                width = getWidth();
                paddingLeft = getPaddingLeft();
                paddingRight = getPaddingRight();
            }else{
                realLeft = (int) (child.getY() - getScrollY());
                realRight = realLeft + child.getHeight();
                width = getHeight();
                paddingLeft = getPaddingTop();
                paddingRight = getPaddingBottom();
            }
            return !(realRight <= paddingLeft || realLeft >= width - paddingRight);
        }
        return false;
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            if(getOrientation() == HORIZONTAL){
                int x = mScroller.getCurrX();
                scrollTo(x, 0);
            }else{
                scrollTo(0, mScroller.getCurrY());
            }
            postInvalidate();
        }
    }
}
