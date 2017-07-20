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
import me.cyandev.springanimator.Rk4SpringAnimator;
import me.cyandev.springanimatordemo.ConfigurationResolver;

public class Rk4AdjustmentFragment extends BaseAdjustmentFragment {

    @Override
    protected String getTypeName() {
        return "RK4";
    }

    @Override
    protected ArrayList<AdjustmentInfo> onCreateAdjustmentInfo() {
        int tension = 200;
        int friction = 10;
        int velocity = 0;

        Bundle args = getArguments();
        if (args != null) {
            AbsSpringAnimator animator = ConfigurationResolver.resolveConfiguration(args);
            if (animator != null && animator instanceof Rk4SpringAnimator) {
                Rk4SpringAnimator rk4SpringAnimator = (Rk4SpringAnimator) animator;
                tension = (int) rk4SpringAnimator.getTension();
                friction = (int) rk4SpringAnimator.getFriction();
                velocity = (int) rk4SpringAnimator.getVelocity();
            }
        }

        ArrayList<AdjustmentInfo> result = new ArrayList<>();

        result.add(new AdjustmentInfo("Tension", tension, 0, 1000));
        result.add(new AdjustmentInfo("Friction", friction, 0, 100));
        result.add(new AdjustmentInfo("Velocity", velocity, 0, 100));

        return result;
    }

    @Override
    protected Bundle onCreateConfigurationBundle() {
        int tension = getAdjustmentValue(0);
        int friction = getAdjustmentValue(1);
        int velocity = getAdjustmentValue(2);

        return ConfigurationResolver.buildRk4Configuration(tension, friction, velocity);
    }

}
