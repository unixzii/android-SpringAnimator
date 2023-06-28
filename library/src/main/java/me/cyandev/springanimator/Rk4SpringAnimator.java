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

import androidx.annotation.NonNull;
import android.util.SparseArray;

/**
 * A spring animator using Rk4 algorithm.
 * <br>
 * Reference:
 * See <a href="https://github.com/koenbok/Framer/blob/master/framer/Animators/SpringRK4Animator.coffee">
 *     https://github.com/koenbok/Framer/blob/master/framer/Animators/SpringRK4Animator.coffee</a>
 */
public class Rk4SpringAnimator extends AbsSpringAnimator {

    private static SparseArray<float[]> sStatePool;

    private float mTension = 250;
    private float mFriction = 25;
    private float mVelocity = 0;
    private float mTolerance = 1 / 10000.f;

    private float mValue = 0;
    private float mCurrentVelocity = 0;
    private boolean mStopSpring = false;

    private Integrator mIntegrator = new Integrator(new Integrator.AccelerationForStateEvaluator() {
        @Override
        public float evaluate(float[] state) {
            return - mTension * state[0] - mFriction * state[1];
        }
    });

    public float getTension() {
        return mTension;
    }

    public void setTension(float tension) {
        mTension = tension;
    }

    public float getFriction() {
        return mFriction;
    }

    public void setFriction(float friction) {
        mFriction = friction;
    }

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

    @Override
    protected long computeSettleDuration() {
        // TODO: Not implemented.
        return 0;
    }

    @Override
    protected void resetState() {
        mValue = 0;
        mCurrentVelocity = mVelocity;
        mStopSpring = false;
    }

    @Override
    protected float enterFrame(long frameTime) {
        final float delta = Math.max(frameTime / 1000.f, 0.016f);

        if (isFinished()) {
            return 1.f;
        }

        float[] stateBefore = getState(5);
        float[] stateAfter = getState(6);

        stateBefore[0] = mValue - 1;
        stateBefore[1] = mCurrentVelocity;

        mIntegrator.evaluateIntegrateState(stateBefore, delta);
        stateAfter[0] = stateBefore[0];
        stateAfter[1] = stateBefore[1];

        mValue = 1 + stateAfter[0];

        final float finalVelocity = stateAfter[1];
        final float netFloat = stateAfter[0];
        final float net1DVelocity = stateAfter[1];

        final boolean netValueIsLow = Math.abs(netFloat) < mTolerance;
        final boolean netVelocityIsLow = Math.abs(net1DVelocity) < mTolerance;

        mStopSpring = netValueIsLow && netVelocityIsLow;
        mCurrentVelocity = finalVelocity;

        return mValue;
    }

    @Override
    protected boolean isFinished() {
        return mStopSpring;
    }

    @NonNull
    private static float[] getState(int key) {
        if (sStatePool == null) {
            return new float[2];
        }

        float[] state = sStatePool.get(key);
        if (state == null) {
            state = new float[2];
            sStatePool.put(key, state);
        }

        return state;
    }

    private static class Integrator {

        private AccelerationForStateEvaluator mAccelerationForStateEvaluator;

        Integrator(AccelerationForStateEvaluator evaluator) {
            mAccelerationForStateEvaluator = evaluator;

            if (sStatePool == null) {
                sStatePool = new SparseArray<>();
            }
        }

        void evaluateIntegrateState(float[] state, float dt) {
            float[] a = getState(1);
            float[] b = getState(2);
            float[] c = getState(3);
            float[] d = getState(4);

            evaluateState(state, a);
            evaluateStateWithDerivative(state, dt * 0.5f, a, b);
            evaluateStateWithDerivative(state, dt * 0.5f, b, c);
            evaluateStateWithDerivative(state, dt, c, d);

            final float dxdt = 1.f / 6.f * (a[0] + 2.f * (b[0] + c[0]) + d[0]);
            final float dvdt = 1.f / 6.f * (a[1] + 2.f * (b[1] + c[1]) + d[1]);

            state[0] += dxdt * dt;
            state[1] += dvdt * dt;
        }

        private void evaluateState(float[] initialState, float[] output) {
            output[0] = initialState[1];
            output[1] = mAccelerationForStateEvaluator.evaluate(initialState);
        }

        private void evaluateStateWithDerivative(float[] initialState, float dt, float[] derivative, float[] output) {
            float[] state = getState(7);
            state[0] = initialState[0] + derivative[0] * dt;
            state[1] = initialState[1] + derivative[1] * dt;

            output[0] = state[1];
            output[1] = mAccelerationForStateEvaluator.evaluate(state);
        }

        interface AccelerationForStateEvaluator {
            float evaluate(float[] state);
        }

    }

}
