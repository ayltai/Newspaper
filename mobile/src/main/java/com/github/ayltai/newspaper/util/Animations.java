package com.github.ayltai.newspaper.util;

import java.util.Arrays;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.AnimRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.fujiyuu75.sequent.Sequent;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;

public final class Animations {
    private Animations() {
    }

    @NonNull
    public static Animation getAnimation(@NonNull final Context context, @AnimRes final int animationId, @IntegerRes final int durationId) {
        final Animation animation = AnimationUtils.loadAnimation(context, animationId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            animation.setDuration((int)(context.getResources().getInteger(durationId) * Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f)));
        } else {
            animation.setDuration(context.getResources().getInteger(durationId));
        }

        return animation;
    }

    @NonNull
    public static Iterable<Animator> createDefaultAnimators(@NonNull final View view) {
        return Arrays.asList(
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0f)
        );
    }

    public static void animateViewGroup(@NonNull final ViewGroup container) {
        Sequent.origin(container)
            .delay(0)
            .offset(Constants.ANIMATION_OFFSET)
            .duration(Constants.ANIMATION_DURATION)
            .anim(container.getContext(), R.anim.fade_in_up)
            .start();
    }
}
