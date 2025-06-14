/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */

package com.xlab.vbrowser.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

public class AnimatedProgressBar extends ProgressBar {
    private final static int PROGRESS_DURATION = 200;
    private final static int CLOSING_DELAY = 300;
    private final static int CLOSING_DURATION = 300;
    private ValueAnimator mPrimaryAnimator;
    private ValueAnimator mClosingAnimator = ValueAnimator.ofFloat(0f, 1f);
    private float mClipRegion = 0f;
    private int mExpectedProgress = 0;
    private Rect tempRect;
    private boolean mIsRtl;

    private ValueAnimator.AnimatorUpdateListener mListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setProgressImmediately((int) mPrimaryAnimator.getAnimatedValue());
        }
    };

    public AnimatedProgressBar(@NonNull Context context) {
        super(context, null);
        init(context, null);
    }

    public AnimatedProgressBar(@NonNull Context context,
                               @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimatedProgressBar(@NonNull Context context,
                               @Nullable AttributeSet attrs,
                               int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @Override
    public void setProgress(int nextProgress) {
        nextProgress = Math.min(nextProgress, getMax());
        nextProgress = Math.max(0, nextProgress);
        mExpectedProgress = nextProgress;

        // a dirty-hack for reloading page.
        if (mExpectedProgress < getProgress() && getProgress() == getMax()) {
            setProgressImmediately(0);
        }

        if (mPrimaryAnimator != null) {
            mPrimaryAnimator.cancel();
            mPrimaryAnimator.setIntValues(getProgress(), nextProgress);
            mPrimaryAnimator.start();
        } else {
            setProgressImmediately(nextProgress);
        }

        if (mClosingAnimator != null) {
            if (nextProgress != getMax()) {
                // stop closing animation
                mClosingAnimator.cancel();
                mClipRegion = 0f;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mClipRegion == 0) {
            super.onDraw(canvas);
        } else {
            canvas.getClipBounds(tempRect);
            final float clipWidth = tempRect.width() * mClipRegion;
            final int saveCount = canvas.save();


            if (mIsRtl) {
                canvas.clipRect(tempRect.left, tempRect.top, tempRect.right - clipWidth, tempRect.bottom);
            } else {
                canvas.clipRect(tempRect.left + clipWidth, tempRect.top, tempRect.right, tempRect.bottom);
            }
            super.onDraw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    public void setVisibility(int value) {
        if (value == GONE) {
            if (mExpectedProgress == getMax()) {
                animateClosing();
            } else {
                setVisibilityImmediately(value);
            }
        } else {
            setVisibilityImmediately(value);
        }
    }

    private void setVisibilityImmediately(int value) {
        super.setVisibility(value);
    }

    private void animateClosing() {
        mIsRtl = (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL);

        mClosingAnimator.cancel();

        final Handler handler = getHandler();
        if (handler != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mClosingAnimator.start();
                }
            }, CLOSING_DELAY);
        }
    }

    private void setProgressImmediately(int progress) {
        super.setProgress(progress);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        tempRect = new Rect();

        final TypedArray a = context.obtainStyledAttributes(attrs, com.xlab.vbrowser.R.styleable.AnimatedProgressBar);
        final int duration = a.getInteger(com.xlab.vbrowser.R.styleable.AnimatedProgressBar_shiftDuration, 1000);
        final int resID = a.getResourceId(com.xlab.vbrowser.R.styleable.AnimatedProgressBar_shiftInterpolator, 0);
        final boolean wrap = a.getBoolean(com.xlab.vbrowser.R.styleable.AnimatedProgressBar_wrapShiftDrawable, false);

        mPrimaryAnimator = ValueAnimator.ofInt(getProgress(), getMax());
        mPrimaryAnimator.setInterpolator(new LinearInterpolator());
        mPrimaryAnimator.setDuration(PROGRESS_DURATION);
        mPrimaryAnimator.addUpdateListener(mListener);

        mClosingAnimator.setDuration(CLOSING_DURATION);
        mClosingAnimator.setInterpolator(new LinearInterpolator());
        mClosingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float region = (float) valueAnimator.getAnimatedValue();
                if (mClipRegion != region) {
                    mClipRegion = region;
                    invalidate();
                }
            }
        });
        mClosingAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                try {
                    mClipRegion = 0f;
                }
                catch (java.lang.IllegalStateException e) {
                    //Ignore this known exception
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                try {
                    setVisibilityImmediately(GONE);
                }
                catch (IllegalStateException ex) {
                    //Ignore this know exception
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mClipRegion = 0f;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        setProgressDrawable(buildWrapDrawable(getProgressDrawable(), wrap, duration, resID));

        a.recycle();
    }

    private Drawable buildWrapDrawable(Drawable original, boolean isWrap, int duration, int resID) {
        if (isWrap) {
            final Interpolator interpolator = (resID > 0)
                    ? AnimationUtils.loadInterpolator(getContext(), resID)
                    : null;
            final ShiftDrawable wrappedDrawable = new ShiftDrawable(original, duration, interpolator);
            return wrappedDrawable;
        } else {
            return original;
        }
    }
}