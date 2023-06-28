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

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.cyandev.springanimator.AbsSpringAnimator;

public abstract class BasePreviewFragment extends Fragment {

    private SpringAnimatorProvider mProvider;

    public abstract void onResetView();

    public abstract void onStartAnimation();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() instanceof SpringAnimatorProvider) {
            mProvider = (SpringAnimatorProvider) getActivity();
        }
    }

    @Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected final AbsSpringAnimator createNewAnimator() {
        if (mProvider == null) {
            throw new IllegalStateException("Provider is not presented");
        }

        return mProvider.provideAnimator();
    }

    public interface SpringAnimatorProvider {
        AbsSpringAnimator provideAnimator();
    }

}
