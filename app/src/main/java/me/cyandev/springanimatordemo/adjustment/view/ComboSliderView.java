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

package me.cyandev.springanimatordemo.adjustment.view;

import android.animation.Animator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import me.cyandev.springanimatordemo.R;
import me.cyandev.springanimatordemo.adjustment.BaseAdjustmentFragment;
import me.cyandev.springanimatordemo.util.SimpleAnimatorListener;

public class ComboSliderView extends FrameLayout {

    private ViewGroup mSliderLayout;
    private ViewGroup mInputViewLayout;
    private TextView mLabelTextView;
    private TextView mValueTextView;
    private AppCompatSeekBar mSeekBar;
    private TextInputEditText mEditText;
    private AppCompatImageButton mEditButton;

    private BaseAdjustmentFragment.AdjustmentInfo mAdjustmentInfo;
    private boolean mIsInputViewVisible = false;

    private InputMethodManager mImm;

    private Interactor mInteractor;

    public static ComboSliderView create(ViewGroup root, boolean attachToRoot) {
        return (ComboSliderView) LayoutInflater.from(root.getContext())
                .inflate(R.layout.combo_slider, root, attachToRoot);
    }

    public ComboSliderView(Context context) {
        this(context, null);
    }

    public ComboSliderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComboSliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void setOnChangeListener(Interactor listener) {
        mInteractor = listener;
    }

    public void setValue(int value) {
        mSeekBar.setProgress(value - mAdjustmentInfo.minValue);
        mValueTextView.setText(String.valueOf(value));
        mEditText.setText(mValueTextView.getText());
    }

    public int getValue() {
        return mSeekBar.getProgress() + mAdjustmentInfo.minValue;
    }

    public void bindAdjustmentInfo(BaseAdjustmentFragment.AdjustmentInfo info) {
        mAdjustmentInfo = info;

        mLabelTextView.setText(info.propertyName);
        ((TextInputLayout) mInputViewLayout).setHint(info.propertyName);
        mSeekBar.setMax(info.maxValue - info.minValue);
        mSeekBar.setProgress(info.defaultValue - info.minValue);
        mValueTextView.setText(String.valueOf(info.defaultValue));
        mEditText.setText(mValueTextView.getText());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSliderLayout = (ViewGroup) findViewById(R.id.slider_layout);
        mInputViewLayout = (ViewGroup) findViewById(R.id.input_view_layout);
        mLabelTextView = (TextView) findViewById(R.id.text);
        mValueTextView = (TextView) findViewById(R.id.text_value);
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.seek_bar);
        mEditText = (TextInputEditText) findViewById(R.id.edit);
        mEditButton = (AppCompatImageButton) findViewById(R.id.btn_edit);

        mInputViewLayout.setVisibility(INVISIBLE);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValueTextView.setText(String.valueOf(progress + mAdjustmentInfo.minValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                notifyFocusChange(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                notifyFocusChange(false);
                commitValue(getValue());
            }
        });

        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK) {
                    try {
                        commitValue(Integer.parseInt(mEditText.getText().toString()));
                    } catch (NumberFormatException ignored) {}

                    // If the input view is still visible, press back key to hide it.
                    if (keyCode == KeyEvent.KEYCODE_BACK && mInputViewLayout.getVisibility() == VISIBLE) {
                        return true;
                    }
                }
                return false;
            }
        });
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }

                try {
                    commitValue(Integer.parseInt(mEditText.getText().toString()));
                } catch (NumberFormatException ignored) {}
            }
        });

        mEditButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsInputViewVisible) {
                    commitValue(Integer.parseInt(mEditText.getText().toString()));
                } else {
                    showInputView();
                }
            }
        });
    }

    private void commitValue(int value) {
        // Clamp the value.
        value = Math.min(Math.max(value, mAdjustmentInfo.minValue), mAdjustmentInfo.maxValue);

        mSeekBar.setProgress(value - mAdjustmentInfo.minValue);
        mValueTextView.setText(String.valueOf(value));
        mEditText.setText(mValueTextView.getText());

        if (mInteractor != null) {
            mInteractor.onValueChange(value);
        }

        if (mIsInputViewVisible) {
            hideInputView();
        }
    }

    private void notifyFocusChange(boolean focus) {
        if (mInteractor != null) {
            mInteractor.onFocusModeChange(this, focus);
        }
    }

    private void showInputView() {
        if (mIsInputViewVisible) {
            return;
        }

        mIsInputViewVisible = true;

        mEditButton.setImageResource(R.drawable.ic_check_black_16dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(new int[] { R.attr.colorAccent });
            mEditButton.setImageTintList(ColorStateList.valueOf(a.getColor(0, 0)));
            a.recycle();
        }

        mSliderLayout
                .animate()
                .alpha(0)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSliderLayout.setVisibility(INVISIBLE);
                    }
                })
                .setDuration(200)
                .start();

        mInputViewLayout.setVisibility(VISIBLE);
        mInputViewLayout.setAlpha(0);
        mInputViewLayout
                .animate()
                .alpha(1)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mEditText.requestFocus();
                        mEditText.setSelection(mEditText.getText().length());
                        mImm.showSoftInput(mEditText, 0, null);
                        notifyFocusChange(true);
                    }
                })
                .setDuration(200)
                .start();
    }

    @SuppressWarnings("deprecation")
    private void hideInputView() {
        if (!mIsInputViewVisible) {
            return;
        }

        mIsInputViewVisible = false;

        mEditButton.setImageResource(R.drawable.ic_mode_edit_black_16dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mEditButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryGray)));
        }

        mSliderLayout.setVisibility(VISIBLE);
        mSliderLayout
                .animate()
                .alpha(1)
                .setListener(null)
                .setDuration(200)
                .start();

        mInputViewLayout
                .animate()
                .alpha(0)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mInputViewLayout.setVisibility(INVISIBLE);
                    }
                })
                .setDuration(200)
                .start();

        mImm.hideSoftInputFromWindow(getWindowToken(), 0, null);
        notifyFocusChange(false);
    }

    public interface Interactor {
        void onValueChange(int value);
        void onFocusModeChange(View view, boolean focused);
    }

}
