package com.giwon.aide.feature.calendar.scheduler

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.PointF
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.giwon.aide.feature.calendar.scheduler.customview.WeekLayout
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max

class CalendarGestureDetector(
    private val recyclerView: RecyclerView,
    private val onVerticalScrolled: (Boolean) -> Unit,
    private val onHorizontalScrolled: (Boolean) -> Unit,
    private val onCalendarTransformEnd: (Boolean) -> Unit
) : SimpleOnGestureListener(), OnTouchListener {

    private val gestureDetector = GestureDetector(recyclerView.context, this)
    private var initialRecyclerViewHeight: Int = 0
    private var currentHeight = 0
    private var maxHeight = recyclerView.measuredHeight
    private var minimumHeight = maxHeight / 2
    private var isExpand = false

    private var isScrollingVertically = false
    private var isScrollingHorizontally = false
    private var initialX = 0f
    private var initialY = 0f

    private var isScrolled = false

    private var isVerticalAnimating: Boolean = false

    private var horizontalHelper: OrientationHelper? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val childView = recyclerView.findChildViewUnder(e.x, e.y)
        childView?.let { linearLayoutView ->
            if (linearLayoutView is LinearLayout) {
                linearLayoutView.children.forEach { child ->
                    if (child is WeekLayout) {
                        if (isPointInsideView(e.y, child, linearLayoutView)) {
                            val point = transformPointToView(e.x, child)
                            child.onClickWeekLayout(point.x, point.y)
                        }
                    }
                }
            }

        }

        return super.onSingleTapUp(e)
    }

    private fun transformPointToView(
        x: Float,
        childView: View,
    ): PointF {
        val location = IntArray(2)
        childView.getLocationOnScreen(location)
        val viewX = x - location[0]
        return PointF(viewX, 10f)
    }

    private fun isPointInsideView(y: Float, view: View, parentView: View): Boolean {
        val parentLocation = IntArray(2)
        parentView.getLocationOnScreen(parentLocation)
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewY = location[1] - parentLocation[1]
        return y >= viewY && y <= (viewY + view.height)
    }

    override fun onDown(e: MotionEvent): Boolean {
        initialX = e.x
        initialY = e.y
        initialRecyclerViewHeight = recyclerView.height
        isScrollingVertically = false
        onVerticalScrolled(false)
        isScrollingHorizontally = false
        onHorizontalScrolled(false)
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!isScrollingHorizontally && !isScrollingVertically) {
            val deltaX = abs(e2.x - initialX)
            val deltaY = abs(e2.y - initialY)
            if (deltaX > deltaY) {
                isScrollingHorizontally = true
                onHorizontalScrolled(true)
            } else {
                isScrollingVertically = true
                onVerticalScrolled(true)
            }
        }

        if (isScrollingHorizontally) {
            return false
        }

        isVerticalAnimating = true

        if (distanceY > 0) { // 위 방향으로 스크롤
            isExpand = false
        } else if (distanceY < 0) {
            isExpand = true
        }

        val layoutParams = recyclerView.layoutParams
        currentHeight = recyclerView.height
        var newHeight = currentHeight - distanceY.toInt()
        newHeight = newHeight.coerceIn(minimumHeight, maxHeight)
        layoutParams.height = newHeight
        recyclerView.layoutParams = layoutParams
        val percentage = calculateHeightChangePercentage(initialRecyclerViewHeight, newHeight)
        (recyclerView as? CalendarView)?.updateWeekEventOpacity(percentage)
        return true
    }

    private fun calculateHeightChangePercentage(initialHeight: Int, currentHeight: Int): Float {
        if (initialHeight == 0) {
            return 0f
        }

        val percentage = ((currentHeight - initialHeight).toFloat() / initialHeight) * 3
        return (1 - percentage.absoluteValue).coerceIn(0f, 1f)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val layoutManager = recyclerView.layoutManager ?: return false
        val minFlingVelocity: Int = recyclerView.minFlingVelocity
        return (abs(velocityY) > minFlingVelocity || abs(velocityX) > minFlingVelocity)
                && snapFromFling(layoutManager, velocityX.toInt())
    }

    fun handleTouchEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (isScrollingVertically) {
                expandAnimateCalendar(isExpand)
            }
        }
    }

    private fun expandAnimateCalendar(isExpand: Boolean, duration: Long = 300L) {
        val layoutParams = recyclerView.layoutParams
        val currentHeight = if (layoutParams.height > 0) layoutParams.height else initialRecyclerViewHeight

        val targetHeight = if (isExpand) {
            (initialRecyclerViewHeight * 2).coerceAtMost(maxHeight)
        } else {
            (initialRecyclerViewHeight / 2).coerceAtLeast(minimumHeight)
        }

        val animator = ValueAnimator.ofInt(currentHeight, targetHeight)
        animator.addUpdateListener { animation ->
            layoutParams.height = if (isExpand) {
                (animation.animatedValue as Int).coerceAtMost(maxHeight)
            } else {
                (animation.animatedValue as Int).coerceAtLeast(minimumHeight)
            }
            val percentage = calculateHeightChangePercentage(
                initialRecyclerViewHeight,
                layoutParams.height
            )

            (recyclerView as? CalendarView)?.updateWeekEventOpacity(percentage)
            recyclerView.layoutParams = layoutParams
        }
        animator.duration = duration
        animator.start()

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                onCalendarTransformEnd(isExpand)
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {

            }

        })
    }

    fun shrinkCalendar() {
        onVerticalScrolled(true)
        expandAnimateCalendar(isExpand = false, duration = 500L)
    }

    private fun findCenterView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount = layoutManager.childCount

        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        val center = helper.startAfterPadding + helper.totalSpace / 2
        var absClosest = Int.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter =
                helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = abs(childCenter - center)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }

    private fun distanceToCenter(
        targetView: View,
        helper: OrientationHelper
    ): Int {
        val childCenter =
            helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2
        val containerCenter = helper.startAfterPadding + helper.totalSpace / 2
        return childCenter - containerCenter
    }

    private fun snapFromFling(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
    ): Boolean {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return false
        }

        val smoothScroller = createSnapScroller(layoutManager)
        val targetPosition = findTargetSnapPosition(layoutManager, velocityX)
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false
        }

        smoothScroller.targetPosition = targetPosition
        layoutManager.startSmoothScroll(smoothScroller)
        return true
    }

    private fun createSnapScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller {
        return object : LinearSmoothScroller(recyclerView.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                val snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(max(abs(dx), abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return super.calculateSpeedPerPixel(displayMetrics)
            }
        }
    }

    private fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }
        return out
    }

    private fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int
    ): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val orientationHelper = this.getHorizontalHelper(layoutManager)
        var closestChildBeforeCenter: View? = null
        var distanceBefore = Int.MIN_VALUE
        var closestChildAfterCenter: View? = null
        var distanceAfter = Int.MAX_VALUE
        val childCount = layoutManager.childCount

        var visibleView: View? = null
        var visiblePosition: Int
        for (i in 0 until childCount) {
            visibleView = layoutManager.getChildAt(i)
            visibleView?.let { nonNullVisibleView ->
                visiblePosition = this.distanceToCenter(nonNullVisibleView, orientationHelper)

                if (visiblePosition in (distanceBefore + 1)..0) {
                    distanceBefore = visiblePosition
                    closestChildBeforeCenter = visibleView
                }

                if (visiblePosition in 0 until distanceAfter) {
                    distanceAfter = visiblePosition
                    closestChildAfterCenter = visibleView
                }
            }
        }
        val forwardDirection: Boolean = velocityX > 0
        if (forwardDirection && closestChildAfterCenter != null) {
            return layoutManager.getPosition(
                closestChildAfterCenter ?: return RecyclerView.NO_POSITION
            )
        } else if (!forwardDirection && closestChildBeforeCenter != null) {
            return layoutManager.getPosition(
                closestChildBeforeCenter ?: return RecyclerView.NO_POSITION
            )
        } else {
            visiblePosition =
                layoutManager.getPosition(visibleView ?: return RecyclerView.NO_POSITION)

            val snapToPosition = visiblePosition + 1
            return if (snapToPosition in 0 until itemCount) {
                snapToPosition
            } else {
                RecyclerView.NO_POSITION
            }
        }
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (horizontalHelper == null || horizontalHelper?.layoutManager !== layoutManager) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return OrientationHelper.createHorizontalHelper(layoutManager)
    }

    fun attachToRecyclerView() {
        if (recyclerView.onFlingListener == null) {
            recyclerView.onFlingListener = null
        }

        recyclerView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                return this@CalendarGestureDetector.onFling(
                    null,
                    MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0f, 0f, 0),
                    velocityX.toFloat(),
                    velocityY.toFloat()
                )
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                isScrolled = newState != RecyclerView.SCROLL_STATE_IDLE
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isVerticalAnimating = false
                    snapToTargetExistingView()
                }
            }
        })

        snapToTargetExistingView()
    }

    fun snapToTargetExistingView() {
        val layoutManager: RecyclerView.LayoutManager = recyclerView.layoutManager ?: return

        val snapView = findSnapView(layoutManager) ?: return
        val snapDistance = calculateDistanceToFinalSnap(layoutManager, snapView)
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            recyclerView.smoothScrollBy(snapDistance[0], snapDistance[1])
        }
    }

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
        this.minimumHeight = maxHeight / 2
    }

    private fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return findCenterView(layoutManager, this.getHorizontalHelper(layoutManager))
    }
}
