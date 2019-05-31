/*
 * Copyright (C) 2016 Frederik Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package link.fls.swipestack;

import android.animation.Animator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import link.fls.swipestack.util.AnimationUtils;
import link.fls.swipestack.util.DisplayUtils;

public class SwipeHelper implements View.OnTouchListener {

    private final SwipeStack mSwipeStack;
    private View mObservedView;

    private boolean mListenForTouchEvents;
    private float mDownX;
    private float mDownY;
    private float mInitialX;
    private float mInitialY;
    private int mPointerId;
    /**
     * 勾股数 距离
     */
    private double sqrt;
    /**
     * 滑出方向
     * 0：左侧滑出；1：上方滑出；2：右侧滑出；3：下方滑出
     */
    private int slideOutDirection;

    private float mRotateDegrees = SwipeStack.DEFAULT_SWIPE_ROTATION;
    private float mOpacityEnd = SwipeStack.DEFAULT_SWIPE_OPACITY;
    private int mAnimationDuration = SwipeStack.DEFAULT_ANIMATION_DURATION;

    private int screenHeight;
    private int screenWith;
    private boolean isHorizontal;

    public SwipeHelper(SwipeStack swipeStack, int screenHeight, int screenWith) {
        mSwipeStack = swipeStack;
        this.screenHeight = screenHeight;
        this.screenWith = screenWith;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mListenForTouchEvents || !mSwipeStack.isEnabled()) {
                    return false;
                }
                sqrt = 0;
                v.getParent().requestDisallowInterceptTouchEvent(true);
                mSwipeStack.onSwipeStart();
                mPointerId = event.getPointerId(0);
                mDownX = event.getX(mPointerId);
                mDownY = event.getY(mPointerId);

                return true;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.findPointerIndex(mPointerId);
                if (pointerIndex < 0) return false;

                float dx = event.getX(pointerIndex) - mDownX;
                float dy = event.getY(pointerIndex) - mDownY;

                float newX = mObservedView.getX() + dx;
                float newY = mObservedView.getY() + dy;
                sqrt = Math.sqrt(newX * newX + newY * newY);
                mObservedView.setX(newX);
                mObservedView.setY(newY);
                Log.e("aaa", "newX   " + newX + "  newY  " + newY);
                //横向滑动
                isHorizontal = Math.abs(newX) > Math.abs(newY);
                if (isHorizontal) {
                    if (newX > 0) {
                        slideOutDirection = 2;
                    } else {
                        slideOutDirection = 0;
                    }
                } else {
                    //纵向滑动
                    if (newY > 0) {
                        slideOutDirection = 3;
                    } else {
                        slideOutDirection = 1;
                    }
                }

                float dragDistanceX = newX - mInitialX;
                float swipeProgress = Math.min(Math.max(dragDistanceX / mSwipeStack.getWidth(), -1), 1);

                mSwipeStack.onSwipeProgress(swipeProgress);

                if (mRotateDegrees > 0) {
                    float rotation = mRotateDegrees * swipeProgress;
                    mObservedView.setRotation(rotation);
                }

                if (mOpacityEnd < 1f) {
                    float alpha = 1 - Math.min(Math.abs(swipeProgress * 2), 1);
                    mObservedView.setAlpha(alpha);
                }

                return true;

            case MotionEvent.ACTION_UP:
                v.getParent().requestDisallowInterceptTouchEvent(false);
                mSwipeStack.onSwipeEnd();
                if (sqrt < 5) {
                    mSwipeStack.onClickListener();
                } else {
                    checkViewPosition();
                }
                return true;
            default:
                break;
        }

        return false;
    }

    private void checkViewPosition() {
        if (!mSwipeStack.isEnabled()) {
            resetViewPosition();
            return;
        }

        float viewCenterHorizontal = mObservedView.getX() + (mObservedView.getWidth() / 2);
        float parentFirstThird = mSwipeStack.getWidth() / 3f;
        float parentLastThird = parentFirstThird * 2;
        /**
         * 原有逻辑，产品需求为：上下左右都可滑出删除
         */
//        if (viewCenterHorizontal < parentFirstThird && mSwipeStack.getAllowedSwipeDirections() != SwipeStack.SWIPE_DIRECTION_ONLY_RIGHT) {
//            swipeViewToLeft(mAnimationDuration / 2);
//        } else if (viewCenterHorizontal > parentLastThird && mSwipeStack.getAllowedSwipeDirections() != SwipeStack.SWIPE_DIRECTION_ONLY_LEFT) {
//            swipeViewToRight(mAnimationDuration / 2);
//        } else {
//            resetViewPosition();
//        }


        if (sqrt > screenWith * 1 / 2 || sqrt > screenHeight * 1 / 2) {
            slidOut(slideOutDirection);
//            swipeViewToRight(mAnimationDuration / 2);
        } else {
            resetViewPosition();
        }
    }

    public void resetViewPosition() {
        mObservedView.animate()
                .x(mInitialX)
                .y(mInitialY)
                .rotation(0)
                .alpha(1)
                .setDuration(mAnimationDuration)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .setListener(null);
    }


    private void swipeViewToLeft(int duration) {
        if (!mListenForTouchEvents) return;
        mListenForTouchEvents = false;
        mObservedView.animate().cancel();
        mObservedView.animate()
                .x(-mSwipeStack.getWidth() + mObservedView.getX())
                .rotation(-mRotateDegrees)
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimationUtils.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSwipeStack.onViewSwipedToLeft();
                    }
                });
    }

    private void swipeViewToRight(int duration) {
        if (!mListenForTouchEvents) return;
        mListenForTouchEvents = false;
        mObservedView.animate().cancel();
        mObservedView.animate()
                .x(mSwipeStack.getWidth() + mObservedView.getX())
                .rotation(mRotateDegrees)
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimationUtils.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSwipeStack.onViewSwipedToRight();
                    }
                });
    }

    private void swipeViewToTop(int duration) {
        if (!mListenForTouchEvents) return;
        mListenForTouchEvents = false;
        mObservedView.animate().cancel();
        mObservedView.animate()
                .y(-mSwipeStack.getHeight() + mObservedView.getY())
                .rotation(0)
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimationUtils.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSwipeStack.onViewSwipedToTop();
                    }
                });
    }

    private void swipeViewToBottom(int duration) {
        if (!mListenForTouchEvents) return;
        mListenForTouchEvents = false;
        mObservedView.animate().cancel();
        mObservedView.animate()
                .y(mSwipeStack.getHeight() + mObservedView.getY())
                .rotation(0)
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimationUtils.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSwipeStack.onViewSwipedToBottom();
                    }
                });
    }

    private void slidOut(int slideOutDirection) {
        switch (slideOutDirection) {
            case 0:
                //左侧滑出
                swipeViewToLeft(mAnimationDuration / 2);
                break;
            case 1:
//                上方滑出
                swipeViewToTop(mAnimationDuration / 2);
                break;
            case 2:
//                右侧滑出
                swipeViewToRight(mAnimationDuration / 2);
                break;
            case 3:
//                下方滑出
                swipeViewToBottom(mAnimationDuration / 2);
                break;
            default:
                break;
        }
    }

    public void registerObservedView(View view, float initialX, float initialY) {
        if (view == null) return;
        mObservedView = view;
        mObservedView.setOnTouchListener(this);
        mInitialX = initialX;
        mInitialY = initialY;
        mListenForTouchEvents = true;
    }

    public void unregisterObservedView() {
        if (mObservedView != null) {
            mObservedView.setOnTouchListener(null);
        }
        mObservedView = null;
        mListenForTouchEvents = false;
    }

    public void setAnimationDuration(int duration) {
        mAnimationDuration = duration;
    }

    public void setRotation(float rotation) {
        mRotateDegrees = rotation;
    }

    public void setOpacityEnd(float alpha) {
        mOpacityEnd = alpha;
    }

    public void swipeViewToLeft() {
        swipeViewToLeft(mAnimationDuration);
    }

    public void swipeViewToRight() {
        swipeViewToRight(mAnimationDuration);
    }

}
