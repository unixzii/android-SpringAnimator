/*
 * Copyright (C) 2017 Cyandev
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

package me.cyandev.springanimatordemo.preview;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import me.cyandev.springanimator.AbsSpringAnimator;
import me.cyandev.springanimatordemo.R;

public class TranslationPreviewFragment extends BasePreviewFragment {

    private CardView mCardView;

    private AbsSpringAnimator mXAnimator;
    private AbsSpringAnimator mYAnimator;

    private boolean mOriLocationRecorded = false;
    private int mOriX = 0;
    private int mOriY = 0;
    private float mCardZ = 0;

    private ViewDragHelper.Callback mViewDragHelperCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mCardView;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            capturedChild.getParent().requestDisallowInterceptTouchEvent(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                capturedChild.animate().z(mCardZ * 5).setDuration(150).start();
            }
            stopAnimators();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            releasedChild.getParent().requestDisallowInterceptTouchEvent(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                releasedChild.animate().z(mCardZ).start();
            }
            onStartAnimation();
        }

    };

    @Override
    public void onResetView() {
        stopAnimators();

        mCardView.getParent().requestLayout();
        recordOriginalLocation(mCardView);
    }

    @Override
    public void onStartAnimation() {
        stopAnimators();

        if (!mOriLocationRecorded) {
            onResetView();
            return;
        }

        AbsSpringAnimator animator = createNewAnimator();
        animator.setStartValue(mCardView.getLeft());
        animator.setEndValue(mOriX);
        animator.addUpdateListener(new AbsSpringAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(AbsSpringAnimator animation) {
                ViewCompat.offsetLeftAndRight(mCardView, -mCardView.getLeft() + (int) animation.getAnimatedValue());
            }
        });
        animator.start();
        mXAnimator = animator;

        animator = createNewAnimator();
        animator.setStartValue(mCardView.getTop());
        animator.setEndValue(mOriY);
        animator.addUpdateListener(new AbsSpringAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(AbsSpringAnimator animation) {
                ViewCompat.offsetTopAndBottom(mCardView, -mCardView.getTop() + (int) animation.getAnimatedValue());
            }
        });
        animator.start();
        mYAnimator = animator;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_square, container, false);
        mCardView = (CardView) view.findViewById(R.id.card_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCardZ = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.f, getResources().getDisplayMetrics());
            mCardView.setZ(mCardZ);
        }

        final ViewDragHelper viewDragHelper = ViewDragHelper.create(view, mViewDragHelperCallback);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewDragHelper.processTouchEvent(event);
                return false;
            }
        });

        recordOriginalLocation(mCardView);

        return view;
    }

    private void stopAnimators() {
        if (mXAnimator != null) {
            mXAnimator.cancel();
            mXAnimator = null;
        }

        if (mYAnimator != null) {
            mYAnimator.cancel();
            mYAnimator = null;
        }
    }

    private void recordOriginalLocation(final View target) {
        mOriLocationRecorded = false;
        target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver observer = target.getViewTreeObserver();
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }

                mOriX = target.getLeft();
                mOriY = target.getTop();
                mOriLocationRecorded = true;
            }
        });
    }

}
