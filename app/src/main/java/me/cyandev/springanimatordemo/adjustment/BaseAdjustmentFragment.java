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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import me.cyandev.springanimatordemo.adjustment.view.ComboSliderView;

public abstract class BaseAdjustmentFragment extends Fragment implements ComboSliderView.Interactor {

    private static final String TAG = "BaseAdjustmentFragment";
    private static final String KEY_VALUES = TAG + "_values";

    private ArrayList<ComboSliderView> mAdjustmentViews = new ArrayList<>();
    private OnAdjustmentCommitListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() instanceof OnAdjustmentCommitListener) {
            mListener = (OnAdjustmentCommitListener) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(container.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        ArrayList<AdjustmentInfo> adjustmentInfoList = onCreateAdjustmentInfo();
        int id = 0;
        for (AdjustmentInfo info : adjustmentInfoList) {
            ComboSliderView view = ComboSliderView.create(layout, false);
            view.bindAdjustmentInfo(info);
            view.setOnChangeListener(this);
            view.setId(id++);

            layout.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            mAdjustmentViews.add(view);
        }

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int[] values = new int[mAdjustmentViews.size()];
        for (int i = 0, l = values.length; i < l; i++) {
            values[i] = mAdjustmentViews.get(i).getValue();
        }

        outState.putIntArray(KEY_VALUES, values);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            int[] values = savedInstanceState.getIntArray(KEY_VALUES);
            if (values != null) {
                for (int i = 0, l = values.length; i < l; i++) {
                    mAdjustmentViews.get(i).setValue(values[i]);
                }
            }
        }
    }

    @Override
    public void onValueChange(int value) {
        Bundle confBundle = onCreateConfigurationBundle();

        if (mListener != null) {
            mListener.onAdjustmentCommit(confBundle);
        }
    }

    @Override
    public void onFocusModeChange(View view, boolean focused) {
        for (ComboSliderView comboView : mAdjustmentViews) {
            if (focused) {
                if (view != comboView) {
                    comboView.animate().alpha(0.4f).setDuration(250).start();
                }
            } else {
                comboView.animate().alpha(1).setDuration(300).start();
            }
        }
    }

    protected int getAdjustmentValue(int index) {
        return mAdjustmentViews.get(index).getValue();
    }

    protected abstract String getTypeName();

    protected abstract ArrayList<AdjustmentInfo> onCreateAdjustmentInfo();

    protected abstract Bundle onCreateConfigurationBundle();

    public static class AdjustmentInfo {
        public String propertyName;
        public int defaultValue;
        public int minValue;
        public int maxValue;

        AdjustmentInfo(String propertyName, int defaultValue, int minValue, int maxValue) {
            this.propertyName = propertyName;
            this.defaultValue = defaultValue;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
    }

    public interface OnAdjustmentCommitListener {
        void onAdjustmentCommit(Bundle bundle);
    }

}
