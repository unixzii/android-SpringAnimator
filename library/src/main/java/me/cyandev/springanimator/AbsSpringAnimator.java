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

package me.cyandev.springanimator;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.os.Build;
import android.os.Looper;
import androidx.annotation.RequiresApi;
import android.util.AndroidRuntimeException;
import android.util.Log;

import java.util.ArrayList;

import me.cyandev.springanimator.internal.AnimationHandler;

/**
 * This abstract class provides the basic mechanism for each spring animator.
 */
public abstract class AbsSpringAnimator extends Animator implements AnimationHandler.AnimationFrameCallback {

    private static final String TAG = "AbsSpringAnimator";
    private static final boolean DEBUG = false;

    private ArrayList<AnimatorUpdateListener> mUpdateListeners = new ArrayList<>();

    private long mLastFrameTime = 0;
    private long mStartDelay = 0;
    private boolean mStarted = false;
    private boolean mRunning = false;
    private boolean mStartListenersCalled = false;
    private float mProgress = 0;
    private float mStartValue = 0;
    private float mEndValue = 0;

    /**
     * Sets the start value.
     */
    public void setStartValue(float startValue) {
        if (mRunning || mStarted) {
            throw new IllegalStateException("Animators that has been started cannot be changed");
        }

        mStartValue = startValue;
    }

    /**
     * Sets the end value.
     */
    public void setEndValue(float endValue) {
        if (mRunning || mStarted) {
            throw new IllegalStateException("Animators that has been started cannot be changed");
        }

        mEndValue = endValue;
    }

    /**
     * Gets current progress fraction of the animator.
     *
     * @return the progress
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * Gets current animated value of the animator.
     *
     * @return the value
     */
    public float getAnimatedValue() {
        return mStartValue + (mEndValue - mStartValue) * mProgress;
    }

    /**
     * Adds a listener to the set of listeners that are sent update events through the life of
     * an animation. This method is called on all listeners for every frame of the animation,
     * after the values for the animation have been calculated.
     *
     * @param listener the listener to be added to the current set of listeners for this animation
     */
    public void addUpdateListener(AnimatorUpdateListener listener) {
        if (!mUpdateListeners.contains(listener)) {
            mUpdateListeners.add(listener);
        }
    }

    /**
     * Removes a listener from the set listening to frame updates for this animation.
     *
     * @param listener the listener to be removed from the current set of update listeners
     * for this animation
     */
    public void removeUpdateListener(AnimatorUpdateListener listener) {
        mUpdateListeners.remove(listener);
    }

    /**
     * Removes all listeners from the set listening to frame updates for this animation.
     */
    public void removeAllUpdateListeners() {
        if (mUpdateListeners == null) {
            return;
        }
        mUpdateListeners.clear();
    }

    /** {@inheritDoc} */
    @Override
    public long getStartDelay() {
        return mStartDelay;
    }

    /** {@inheritDoc} */
    @Override
    public void setStartDelay(long startDelay) {
        // Clamp start delay to non-negative range.
        if (startDelay < 0) {
            Log.w(TAG, "Start delay should always be non-negative");
            startDelay = 0;
        }
        mStartDelay = startDelay;
    }

    @Override
    public Animator setDuration(long duration) {
        throw new UnsupportedOperationException("Duration should not be set manually");
    }

    /**
     * Gets a estimated duration based on current properties.
     *
     * @return the duration in milliseconds
     */
    @Override
    public long getDuration() {
        return computeSettleDuration();
    }

    @Override
    public void setInterpolator(TimeInterpolator value) {
        throw new UnsupportedOperationException("SpringAnimator does not support time interpolator");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
        return mRunning;
    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }

        mStarted = true;
        mRunning = false;
        mProgress = 0;

        mLastFrameTime = 0;
        AnimationHandler handler = AnimationHandler.getInstance();
        handler.addAnimationFrameCallback(this, mStartDelay);

        if (mStartDelay == 0) {
            startAnimation();
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }

        if ((mStarted || mRunning) && getListeners() != null) {
            if (!mRunning) {
                // If it's not yet running, then start listeners weren't called. Call them now.
                notifyStartListeners();
            }
            ArrayList<AnimatorListener> tmpListeners =
                    (ArrayList<AnimatorListener>) getListeners().clone();
            for (AnimatorListener listener : tmpListeners) {
                listener.onAnimationCancel(this);
            }
        }
        endAnimation();
    }

    /** {@inheritDoc} */
    @Override
    public void end() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        if (mProgress != 1.f) {
            mProgress = 1.f;
            notifyUpdateListeners();
        }
        endAnimation();
    }

    /** {@inheritDoc} */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void resume() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be resumed from the same " +
                    "thread that the animator was started on");
        }
        if (isPaused()) {
            if (!mRunning) {
                AnimationHandler handler = AnimationHandler.getInstance();
                handler.addAnimationFrameCallback(this, 0);
            }
        }
        super.resume();
    }

    /**
     * Override point for subclasses to compute an estimated settle duration.
     *
     * @return the duration
     */
    protected abstract long computeSettleDuration();

    /**
     * Override point for subclasses to reset internal state.
     */
    protected abstract void resetState();

    /**
     * Override point for subclasses to compute values for next frames.
     *
     * @param frameTime how long has been skipped since last call
     */
    protected abstract float enterFrame(long frameTime);

    /**
     * Override point for subclasses to report whether the animation is finished.
     *
     * @return whether the animation is finished
     */
    protected abstract boolean isFinished();

    private void startAnimation() {
        mRunning = true;
        resetState();
        notifyStartListeners();
    }

    @SuppressWarnings("unchecked")
    private void endAnimation() {
        AnimationHandler handler = AnimationHandler.getInstance();
        handler.removeCallback(this);

        if ((mStarted || mRunning) && getListeners() != null) {
            if (!mRunning) {
                // If it's not yet running, then start listeners weren't called. Call them now.
                notifyStartListeners();
            }
            ArrayList<AnimatorListener> tmpListeners =
                    (ArrayList<AnimatorListener>) getListeners().clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                tmpListeners.get(i).onAnimationEnd(this);
            }
        }
        mRunning = false;
        mStarted = false;
        mStartListenersCalled = false;
        mLastFrameTime = 0;
    }

    @SuppressWarnings("unchecked")
    private void notifyStartListeners() {
        if (getListeners() != null && !mStartListenersCalled) {
            ArrayList<AnimatorListener> tmpListeners =
                    (ArrayList<AnimatorListener>) getListeners().clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                tmpListeners.get(i).onAnimationStart(this);
            }
        }
        mStartListenersCalled = true;
    }

    @SuppressWarnings("unchecked")
    private void notifyUpdateListeners() {
        if (mUpdateListeners.size() > 0) {
            ArrayList<AnimatorUpdateListener> tmpListeners =
                    (ArrayList<AnimatorUpdateListener>) mUpdateListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                tmpListeners.get(i).onAnimationUpdate(this);
            }
        }
    }

    // ###################### AnimationFrameCallback ######################

    @Override
    public void doAnimationFrame(long frameTime) {
        AnimationHandler handler = AnimationHandler.getInstance();
        long skipped = 0;
        if (mLastFrameTime == 0) {
            if (getStartDelay() > 0) {
                startAnimation();
            }
        } else {
            skipped = frameTime - mLastFrameTime;
        }
        mLastFrameTime = frameTime;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isPaused()) {
                handler.removeCallback(this);
                mLastFrameTime = 0;
                mRunning = false;
            }
        }

        mProgress = enterFrame(skipped / 1000000L);
        notifyUpdateListeners();
        if (isFinished()) {
            endAnimation();
        }
    }

    public interface AnimatorUpdateListener {
        /**
         * <p>Notifies the occurrence of another frame of the animation.</p>
         *
         * @param animation The animation which was repeated.
         */
        void onAnimationUpdate(AbsSpringAnimator animation);

    }

}


