package me.cyandev.springanimatordemo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import android.transition.Slide;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import java.util.ArrayList;

import me.cyandev.springanimator.AbsSpringAnimator;
import me.cyandev.springanimatordemo.adjustment.BaseAdjustmentFragment;
import me.cyandev.springanimatordemo.adjustment.DhoAdjustmentFragment;
import me.cyandev.springanimatordemo.adjustment.Rk4AdjustmentFragment;
import me.cyandev.springanimatordemo.preview.BasePreviewFragment;
import me.cyandev.springanimatordemo.preview.RecyclerViewPreviewFragment;
import me.cyandev.springanimatordemo.preview.TranslationPreviewFragment;
import me.cyandev.springanimatordemo.util.SimpleAnimatorListener;

public class MainActivity extends AppCompatActivity
        implements BasePreviewFragment.SpringAnimatorProvider, BaseAdjustmentFragment.OnAdjustmentCommitListener {

    private static final String TAG = "MainActivity";
    private static final String KEY_CURRENT_CONFIGURATION = TAG + "_currentConfiguration";
    private static final String KEY_SELECTED_ADJUSTMENT = TAG + "_selectedAdjustment";

    private static final Class[] PREVIEW_FRAGMENT_CLASSES = {
            TranslationPreviewFragment.class,
            RecyclerViewPreviewFragment.class
    };

    private static final String[][] ADJUSTMENT_METADATA = {
            { "Spring - DHO", DhoAdjustmentFragment.class.getName() },
            { "Spring - RK4", Rk4AdjustmentFragment.class.getName() }
    };

    private FloatingActionButton mFab;
    private ViewPager mPager;
    private ViewGroup mPanelLayout;
    private AppCompatSpinner mAdjustmentTypeSpinner;
    private FrameLayout mPanelContainer;

    private Bundle mCurrentConfiguration;

    private boolean mPanelClosed = false;
    private int mSelectedAdjustment = 0;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPanelLayout = (ViewGroup) findViewById(R.id.panel_layout);
        mAdjustmentTypeSpinner = (AppCompatSpinner) findViewById(R.id.spinner);
        mPanelContainer = (FrameLayout) findViewById(R.id.container2);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAdjustmentPanel();
            }
        });

        mPager.setAdapter(new PreviewPagerAdapter(getSupportFragmentManager()));

        mPanelLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Eat the event to prevent touching through.
                return true;
            }
        });

        setupSpinner();

        if (savedInstanceState == null) {
            mCurrentConfiguration = ConfigurationResolver.buildDhoConfiguration(200, 10, 1, 0);
            mPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    peekPager();
                }
            }, 1500);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Hide the panel for a while if it's not opened.
        if (!isAdjustmentPanelVisible()) {
            mPanelLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0 /* placeholder */:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBundle(KEY_CURRENT_CONFIGURATION, mCurrentConfiguration);
        outState.putInt(KEY_SELECTED_ADJUSTMENT, mSelectedAdjustment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentConfiguration = savedInstanceState.getBundle(KEY_CURRENT_CONFIGURATION);
        mSelectedAdjustment = savedInstanceState.getInt(KEY_SELECTED_ADJUSTMENT);
    }

    @Override
    public void onBackPressed() {
        if (closeAdjustmentPanel()) {
            return;
        }

        super.onBackPressed();
    }

    private void setupSpinner() {
        ArrayList<String> types = new ArrayList<>();
        for (String[] metadata : ADJUSTMENT_METADATA) {
            types.add(metadata[0]);
        }

        mAdjustmentTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types));
        mAdjustmentTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Workaround for duplicated selecting when restored.
                if (mSelectedAdjustment == position) {
                    return;
                }

                mSelectedAdjustment = position;
                if (getSupportFragmentManager().findFragmentById(R.id.container2) != null) {
                    mAdjustmentTypeSpinner.setEnabled(false);
                    closeAdjustmentPanel(new Runnable() {
                        @Override
                        public void run() {
                            mAdjustmentTypeSpinner.setEnabled(true);
                            openAdjustmentPanel();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void peekPager() {
        final float peekDistance =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        ValueAnimator peekAnimator = ValueAnimator.ofFloat(0, -peekDistance);
        peekAnimator.setDuration(450);
        peekAnimator.setInterpolator(new FastOutSlowInInterpolator());
        peekAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float mLastValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float currentValue = (float) animation.getAnimatedValue();
                mPager.fakeDragBy(currentValue - mLastValue);
                mLastValue = currentValue;
            }
        });
        peekAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPager.beginFakeDrag();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPager.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPager.endFakeDrag();
                    }
                }, 300);
            }
        });
        peekAnimator.start();
    }

    private BasePreviewFragment getCurrentPreviewFragment() {
        Fragment f = ((PreviewPagerAdapter) mPager.getAdapter()).getItem(mPager.getCurrentItem());

        if (f instanceof BasePreviewFragment) {
            return (BasePreviewFragment) f;
        }

        return null;
    }

    private void openAdjustmentPanel() {
        final Fragment f;

        String adjustmentFragmentClassname = ADJUSTMENT_METADATA[mSelectedAdjustment][1];
        try {
            Class adjustmentFragmentClass = Class.forName(adjustmentFragmentClassname);
            f = (Fragment) adjustmentFragmentClass.newInstance();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        f.setArguments(mCurrentConfiguration);

        // Perform a slide up fashion on L or higher versions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            f.setEnterTransition(new Slide(Gravity.BOTTOM).setDuration(500));
            f.postponeEnterTransition();
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.container2, f)
                .commitNow();

        mPanelContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (mPanelContainer.getChildCount() == 0) {
                    // Fragment view is not installed, ignore the callback one time.
                    return true;
                }

                ViewTreeObserver observer = mPanelLayout.getViewTreeObserver();
                if (observer.isAlive()) {
                    observer.removeOnPreDrawListener(this);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    f.startPostponedEnterTransition();
                }

                mPanelLayout.setVisibility(View.VISIBLE);
                mPanelLayout.setTranslationY(mPanelLayout.getHeight());
                mPanelLayout
                        .animate()
                        .translationY(0)
                        .setDuration(400)
                        .setListener(null)
                        .start();

                return true;
            }
        });

        mPanelClosed = false;
        mFab.hide();
    }

    private boolean closeAdjustmentPanel() {
        return closeAdjustmentPanel(null);
    }

    private boolean closeAdjustmentPanel(final Runnable callback) {
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment f = fm.findFragmentById(R.id.container2);
        if (f != null && !mPanelClosed) {
            mPanelClosed = true;
            mPanelLayout
                    .animate()
                    .translationY(mPanelLayout.getHeight())
                    .setDuration(300)
                    .setListener(new SimpleAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mPanelClosed = false;
                            mFab.show();

                            fm.beginTransaction()
                                    .remove(f)
                                    .commit();

                            if (callback != null) {
                                callback.run();
                            }
                        }
                    });
        } else {
            return false;
        }

        return true;
    }

    private boolean isAdjustmentPanelVisible() {
        return getSupportFragmentManager().findFragmentById(R.id.container2) != null;
    }

    @Override
    public AbsSpringAnimator provideAnimator() {
        return ConfigurationResolver.resolveConfiguration(mCurrentConfiguration);
    }

    @Override
    public void onAdjustmentCommit(Bundle bundle) {
        mCurrentConfiguration = bundle;
        BasePreviewFragment f = getCurrentPreviewFragment();
        if (f != null) {
            f.onStartAnimation();
        }
    }

    private class PreviewPagerAdapter extends FragmentPagerAdapter {

        private SparseArray<Fragment> mCache = new SparseArray<>();

        public PreviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = mCache.get(position);
            if (f == null) {
                try {
                    f = (Fragment) PREVIEW_FRAGMENT_CLASSES[position].newInstance();
                    mCache.put(position, f);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return f;
        }

        @Override
        public int getCount() {
            return PREVIEW_FRAGMENT_CLASSES.length;
        }

    }

}
