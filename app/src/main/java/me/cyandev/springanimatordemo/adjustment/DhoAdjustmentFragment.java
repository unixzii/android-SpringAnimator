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

package me.cyandev.springanimatordemo.adjustment;

import android.os.Bundle;

import java.util.ArrayList;

import me.cyandev.springanimator.AbsSpringAnimator;
import me.cyandev.springanimator.DhoSpringAnimator;
import me.cyandev.springanimatordemo.ConfigurationResolver;

public class DhoAdjustmentFragment extends BaseAdjustmentFragment {

    @Override
    protected String getTypeName() {
        return "DHO";
    }

    @Override
    protected ArrayList<AdjustmentInfo> onCreateAdjustmentInfo() {
        int stiffness = 200;
        int damping = 10;
        int mass = 1;
        int velocity = 0;

        Bundle args = getArguments();
        if (args != null) {
            AbsSpringAnimator animator = ConfigurationResolver.resolveConfiguration(args);
            if (animator != null && animator instanceof DhoSpringAnimator) {
                DhoSpringAnimator dhoSpringAnimator = (DhoSpringAnimator) animator;
                stiffness = (int) dhoSpringAnimator.getStiffness();
                damping = (int) dhoSpringAnimator.getDamping();
                mass = (int) dhoSpringAnimator.getMass();
                velocity = (int) dhoSpringAnimator.getVelocity();
            }
        }

        ArrayList<AdjustmentInfo> result = new ArrayList<>();

        result.add(new AdjustmentInfo("Stiffness", stiffness, 0, 1000));
        result.add(new AdjustmentInfo("Damping", damping, 0, 100));
        result.add(new AdjustmentInfo("Mass", mass, 1, 20));
        result.add(new AdjustmentInfo("Velocity", velocity, 0, 100));

        return result;
    }

    @Override
    protected Bundle onCreateConfigurationBundle() {
        int stiffness = getAdjustmentValue(0);
        int damping = getAdjustmentValue(1);
        int mass = getAdjustmentValue(2);
        int velocity = getAdjustmentValue(3);

        return ConfigurationResolver.buildDhoConfiguration(stiffness, damping, mass, velocity);
    }

}
