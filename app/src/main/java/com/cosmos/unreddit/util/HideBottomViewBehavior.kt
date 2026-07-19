package com.cosmos.unreddit.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

/**
 * Hides a bottom view (e.g. bottom navigation) by sliding it vertically off-screen on scroll,
 * matching the feel of Reddit mobile web's full-width bottom bar.
 *
 * [leftHandedMode] is retained for API compatibility but no longer affects direction —
 * the bar is always full-width and slides on the Y axis.
 */
class HideBottomViewBehavior<V: View> @JvmOverloads constructor(
    private val leftHandedMode: Boolean = false,
    context: Context? = null,
    attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<V>(context, attrs) {

    private val LINEAR_OUT_SLOW_IN_INTERPOLATOR = FastOutLinearInInterpolator()
    private val FAST_OUT_LINEAR_IN_INTERPOLATOR = LinearOutSlowInInterpolator()

    private var height: Int = 0
    private var currentState: Int = STATE_SCROLLED_UP
    private var currentAnimator: ViewPropertyAnimator? = null

    private val isScrolledUp: Boolean
        get() = currentState == STATE_SCROLLED_UP

    private val isScrolledDown: Boolean
        get() = currentState == STATE_SCROLLED_DOWN

    var enabled: Boolean = true

    // Keep constructor parity with previous call sites that only pass leftHandedMode
    constructor(leftHandedMode: Boolean) : this(leftHandedMode, null, null)

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        val paramsCompat = child.layoutParams as ViewGroup.MarginLayoutParams
        height = child.measuredHeight + paramsCompat.bottomMargin
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return enabled && axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (dyConsumed > 0) {
            slideOut(child)
        } else if (dyConsumed < 0) {
            slideIn(child)
        }
    }

    fun slideIn(child: V) {
        slideIn(child, true)
    }

    fun slideIn(child: V, animate: Boolean) {
        if (isScrolledUp) return

        currentAnimator?.let { animator ->
            animator.cancel()
            child.clearAnimation()
        }

        currentState = STATE_SCROLLED_UP
        val targetTranslationY = 0
        if (animate) {
            animateChildTo(
                child,
                targetTranslationY,
                ENTER_ANIMATION_DURATION,
                LINEAR_OUT_SLOW_IN_INTERPOLATOR
            )
        } else {
            child.translationY = targetTranslationY.toFloat()
        }
    }

    fun slideOut(child: V) {
        slideOut(child, true)
    }

    fun slideOut(child: V, animate: Boolean) {
        if (isScrolledDown) return

        currentAnimator?.let { animator ->
            animator.cancel()
            child.clearAnimation()
        }

        currentState = STATE_SCROLLED_DOWN
        val targetTranslationY = height
        if (animate) {
            animateChildTo(
                child,
                targetTranslationY,
                EXIT_ANIMATION_DURATION,
                FAST_OUT_LINEAR_IN_INTERPOLATOR
            )
        } else {
            child.translationY = targetTranslationY.toFloat()
        }
    }

    private fun animateChildTo(
        child: V,
        targetY: Int,
        duration: Long,
        interpolator: TimeInterpolator
    ) {
        currentAnimator = child
            .animate()
            .translationY(targetY.toFloat())
            .setInterpolator(interpolator)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }
            })
    }

    companion object {
        private const val ENTER_ANIMATION_DURATION: Long = 225L
        private const val EXIT_ANIMATION_DURATION: Long = 175L

        private const val STATE_SCROLLED_DOWN: Int = 1
        private const val STATE_SCROLLED_UP: Int = 2
    }
}
