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

/**
 * A spring animator using Dho algorithm.
 * <br>
 * Reference:
 * See <a href="https://github.com/koenbok/Framer/blob/master/framer/Animators/SpringDHOAnimator.coffee">
 *     https://github.com/koenbok/Framer/blob/master/framer/Animators/SpringDHOAnimator.coffee</a>
 */
public class DhoSpringAnimator extends AbsSpringAnimator {

    private float mVelocity = 0;
    private float mTolerance = 1 / 10000.f;
    private float mStiffness = 120;
    private float mDamping = 5;
    private float mMass = 1;

    private boolean mIsFirstFrame = true;
    private float mValue = 0;
    private double mCurrentVelocity = 0;

    public float getVelocity() {
        return mVelocity;
    }

    public void setVelocity(float velocity) {
        mVelocity = velocity;
    }

    public float getTolerance() {
        return mTolerance;
    }

    public void setTolerance(float tolerance) {
        mTolerance = tolerance;
    }

    public float getStiffness() {
        return mStiffness;
    }

    public void setStiffness(float stiffness) {
        mStiffness = stiffness;
    }

    public float getDamping() {
        return mDamping;
    }

    public void setDamping(float damping) {
        mDamping = damping;
    }

    public float getMass() {
        return mMass;
    }

    public void setMass(float mass) {
        mMass = mass;
    }

    @Override
    protected long computeSettleDuration() {
        // TODO: Not implemented.
        return 0;
    }

    @Override
    protected void resetState() {
        mIsFirstFrame = true;
        mValue = 0;
        mCurrentVelocity = mVelocity;
    }

    @Override
    protected float enterFrame(long frameTime) {
        final float delta = Math.max(frameTime / 1000.f, 0.016f);

        // A trick to avoid jitter when frames dropped.
        // FIXME: Still encounter jitter sometimes...
        if (delta >= 0.024) {
            float last = 0;
            for (int i = 0, j = (int) Math.floor(delta / 0.016); i < j; i++) {
                last = enterFrame(16);
            }
            return last;
        }

        if (isFinished()) {
            return 1.f;
        }

        double k = 0 - mStiffness;
        double b = 0 - mDamping;

        double fSpring = k * (mValue - 1);
        double fDamper = b * mCurrentVelocity;

        mCurrentVelocity += ((fSpring + fDamper) / mMass) * delta;
        mValue += mCurrentVelocity * delta;

        mIsFirstFrame = false;

        return mValue;
    }

    protected boolean isFinished() {
        return (!mIsFirstFrame && Math.abs(mCurrentVelocity) < mTolerance);
    }

}
