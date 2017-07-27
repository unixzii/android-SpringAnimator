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
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import me.cyandev.springanimator.AbsSpringAnimator;
import me.cyandev.springanimatordemo.R;
import me.cyandev.springanimatordemo.util.SimpleAnimatorListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecyclerViewPreviewFragment extends BasePreviewFragment {

    private static final String TAG = RecyclerViewPreviewFragment.class.getName();
    private static final boolean DEBUG = true;

    private static final int MAX_PREFETCH_ITEM_COUNT = 20;
    private static final String IMAGE_SOURCE_API_ENDPOINT = "https://source.unsplash.com/random/800x600";

    private RecyclerView mRecyclerView;

    private OkHttpClient mHttpClient;

    private List<AbsSpringAnimator> mAnimators = new ArrayList<>();
    private List<Pair<String, Boolean>> mImageSource = new ArrayList<>();
    private ImageSourcePumper mImageSourcePumper;
    private volatile boolean mIsStarted = false;
    private int mUnvisitedItemCount = 0;
    private int mLastScrollOffsetY = 0;

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
            performAnimation(child, i);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHttpClient = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new SimpleAdapter());
        mRecyclerView.setClipToPadding(false);
        mRecyclerView.setPadding(0, 0, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // track for determining animation direction.
                mLastScrollOffsetY = dy;
            }
        });

        return mRecyclerView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mIsStarted = true;

        mImageSourcePumper = new ImageSourcePumper();
        mImageSourcePumper.start();
    }

    @Override
    public void onStop() {
        super.onStop();

        mIsStarted = false;

        mImageSourcePumper.interrupt();
        try {
            mImageSourcePumper.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Simply ignore this.
        }
    }

    private AbsSpringAnimator performAnimation(View v, long staggeringIndex) {
        return performAnimation(v, staggeringIndex, false);
    }

    private AbsSpringAnimator performAnimation(final View v, boolean slideDown) {
        return performAnimation(v, 0, slideDown);
    }

    private AbsSpringAnimator performAnimation(final View v, long staggeringIndex, boolean slideDown) {
        final float dy = (slideDown ? -1 : 1) * mRecyclerView.getHeight();
        v.setTranslationY(dy);

        AbsSpringAnimator animator = createNewAnimator();
        animator.setStartValue(dy);
        animator.setEndValue(0);
        animator.setStartDelay(50 * staggeringIndex);
        animator.addUpdateListener(new AbsSpringAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(AbsSpringAnimator animation) {
                v.setTranslationY(animation.getAnimatedValue());
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

        return animator;
    }

    private void stopAnimators() {
        ArrayList<AbsSpringAnimator> tmpAnimators = (ArrayList<AbsSpringAnimator>) ((ArrayList) mAnimators).clone();
        for (AbsSpringAnimator animator : tmpAnimators) {
            animator.cancel();
        }

        mAnimators.clear();
    }

    private class SimpleAdapter extends RecyclerView.Adapter<SimpleViewHolder> {

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_simple, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            // No-op
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        @Override
        public void onViewAttachedToWindow(SimpleViewHolder holder) {
            if (holder.attachedAnimator != null && holder.attachedAnimator.isRunning()) {
                holder.attachedAnimator.cancel();
            }
            holder.attachedAnimator = performAnimation(holder.itemView, mLastScrollOffsetY < 0);
        }

    }

    private class SimpleViewHolder extends RecyclerView.ViewHolder {

        AbsSpringAnimator attachedAnimator;

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

    private class ImageSourcePumper extends Thread {

        private Semaphore mCallSem = new Semaphore(0);

        @Override
        public void run() {
            while (mIsStarted) {
                while (mUnvisitedItemCount >= MAX_PREFETCH_ITEM_COUNT) {
                    try {
                        synchronized (mImageSource) {
                            mImageSource.wait();
                        }
                    } catch (InterruptedException ignored) {}
                    if (!mIsStarted) {
                        return;
                    }
                }

                if (DEBUG) {
                    Log.d(TAG, "Starting a new request!");
                }

                Request req = new Request.Builder()
                        .url(IMAGE_SOURCE_API_ENDPOINT)
                        .build();
                Call call = mHttpClient.newCall(req);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mCallSem.release();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        List<String> maybeLocation = response.headers().values("Location");
                        if (maybeLocation != null && maybeLocation.size() >= 1) {
                            Pair<String, Boolean> src = new Pair<>(maybeLocation.get(0), false);
                            mImageSource.add(src);
                            ++mUnvisitedItemCount;
                        }

                        mCallSem.release();
                    }
                });

                try {
                    mCallSem.acquire();
                } catch (InterruptedException ignored) {
                    if (!mIsStarted) {
                        return;
                    }
                }
            }
        }

    }

}
