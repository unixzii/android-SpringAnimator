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

import android.animation.Animator;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.cyandev.springanimator.AbsSpringAnimator;
import me.cyandev.springanimatordemo.R;
import me.cyandev.springanimatordemo.util.SimpleAnimatorListener;

public class RecyclerViewPreviewFragment extends BasePreviewFragment {

    private RecyclerView mRecyclerView;

    private List<AbsSpringAnimator> mAnimators = new ArrayList<>();

    @Override
    public void onResetView() {
        stopAnimators();

        for (int i = 0, count = mRecyclerView.getChildCount(); i < count; i++) {
            View child = mRecyclerView.getChildAt(i);
            child.setTranslationY(0);
        }
    }

    @Override
    public void onStartAnimation() {
        stopAnimators();

        for (int i = 0, count = mRecyclerView.getChildCount(); i < count; i++) {
            final View child = mRecyclerView.getChildAt(i);
            final float dy = mRecyclerView.getHeight();

            child.setTranslationY(dy);

            AbsSpringAnimator animator = createNewAnimator();
            animator.setStartValue(dy);
            animator.setEndValue(0);
            animator.setStartDelay(50 * i);
            animator.addUpdateListener(new AbsSpringAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(AbsSpringAnimator animation) {
                    child.setTranslationY(animation.getAnimatedValue());
                }
            });
            animator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimators.remove(animation);
                }
            });
            animator.start();

            mAnimators.add(animator);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new SimpleAdapter());
        mRecyclerView.setClipToPadding(false);
        mRecyclerView.setPadding(0, 0, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));

        return mRecyclerView;
    }

    private void stopAnimators() {
        ArrayList<AbsSpringAnimator> tmpAnimators = (ArrayList<AbsSpringAnimator>) ((ArrayList) mAnimators).clone();
        for (AbsSpringAnimator animator : tmpAnimators) {
            animator.cancel();
        }

        mAnimators.clear();
    }

    private class SimpleAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_simple, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // No-op
        }

        @Override
        public int getItemCount() {
            return 20;
        }

    }

    private class SimpleViewHolder extends RecyclerView.ViewHolder {

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStartAnimation();
                }
            });
        }

    }

}
