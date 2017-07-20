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

package me.cyandev.springanimatordemo;

import android.os.Bundle;

import me.cyandev.springanimator.AbsSpringAnimator;
import me.cyandev.springanimator.DhoSpringAnimator;
import me.cyandev.springanimator.Rk4SpringAnimator;

public final class ConfigurationResolver {

    private static final String KEY_TYPE = "type";
    private static final String KEY_STIFFNESS = "stiffness";
    private static final String KEY_DAMPING = "damping";
    private static final String KEY_MASS = "mass";
    private static final String KEY_VELOCITY = "velocity";
    private static final String KEY_TENSION = "tension";
    private static final String KEY_FRICTION = "friction";

    private static final String TYPE_DHO = "DHO";
    private static final String TYPE_RK4 = "RK4";

    public static Bundle buildDhoConfiguration(int stiffness, int damping, int mass, int velocity) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TYPE, TYPE_DHO);
        bundle.putInt(KEY_STIFFNESS, stiffness);
        bundle.putInt(KEY_DAMPING, damping);
        bundle.putInt(KEY_MASS, mass);
        bundle.putInt(KEY_VELOCITY, velocity);

        return bundle;
    }

    public static Bundle buildRk4Configuration(int tension, int friction, int velocity) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TYPE, TYPE_RK4);
        bundle.putInt(KEY_TENSION, tension);
        bundle.putInt(KEY_FRICTION, friction);
        bundle.putInt(KEY_VELOCITY, velocity);

        return bundle;
    }

    public static AbsSpringAnimator resolveConfiguration(Bundle confBundle) {
        String type = confBundle.getString(KEY_TYPE, "");

        if (TYPE_DHO.equals(type)) {
            DhoSpringAnimator animator = new DhoSpringAnimator();
            animator.setStiffness(confBundle.getInt(KEY_STIFFNESS));
            animator.setDamping(confBundle.getInt(KEY_DAMPING));
            animator.setMass(confBundle.getInt(KEY_MASS));
            animator.setVelocity(confBundle.getInt(KEY_VELOCITY));

            return animator;
        }

        if (TYPE_RK4.equals(type)) {
            Rk4SpringAnimator animator = new Rk4SpringAnimator();
            animator.setTension(confBundle.getInt(KEY_TENSION));
            animator.setFriction(confBundle.getInt(KEY_FRICTION));
            animator.setVelocity(confBundle.getInt(KEY_VELOCITY));

            return animator;
        }

        return null;
    }

}
