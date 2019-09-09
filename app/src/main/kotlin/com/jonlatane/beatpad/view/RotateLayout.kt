package com.jonlatane.beatpad.view


import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.*

import android.view.View.MeasureSpec.UNSPECIFIED
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.HideableView
import java.lang.Math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rotates first view in this layout by specified angle.
 *
 *
 * This layout is supposed to have only one view. Behaviour of the views after the first one
 * is not defined.
 *
 *
 * XML attributes
 * See com.github.rongi.rotate_layout.R.styleable#RotateLayout RotateLayout Attributes,
 */
class RotateLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  ViewGroup(context, attrs), HideableView {
  override var initialHeight: Int? = null
  override var initialWidth: Int? = null
  override var initialTopMargin: Int? = null
  override var initialBottomMargin: Int? = null
  override var initialLeftMargin: Int? = null
  override var initialRightMargin: Int? = null

  var angle: Int = 0
  set(value) {
    if (field != value) {
      field = value
      angleChanged = true
      requestLayout()
      invalidate()
    }
  }
  /**
   * Circle angle, from 0 to TAU
   */
  private val angleC: Double get() = (2 * PI).let { tau -> tau * angle / 360}

  private val rotateMatrix = Matrix()

  private val viewRectRotated = Rect()

  private val tempRectF1 = RectF()
  private val tempRectF2 = RectF()

  private val viewTouchPoint = FloatArray(2)
  private val childTouchPoint = FloatArray(2)

  private var angleChanged = true

  /**
   * Returns this layout's child or null if there is no any
   */
  val view: View?
    get() = if (childCount > 0) {
      getChildAt(0)
    } else {
      null
    }

  init {

    val a = context.obtainStyledAttributes(attrs, R.styleable.RotateLayout)
    angle = a.getInt(R.styleable.RotateLayout_angle, 0)
    a.recycle()

    setWillNotDraw(false)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val child = view
    if (child != null) {
      when {
        abs(angle % 180) == 90 -> {
          measureChild(child, heightMeasureSpec, widthMeasureSpec)
          setMeasuredDimension(
            View.resolveSize(child.measuredHeight, widthMeasureSpec),
            View.resolveSize(child.measuredWidth, heightMeasureSpec))
        }
        abs(angle % 180) == 0  -> {
          measureChild(child, widthMeasureSpec, heightMeasureSpec)
          setMeasuredDimension(
            View.resolveSize(child.measuredWidth, widthMeasureSpec),
            View.resolveSize(child.measuredHeight, heightMeasureSpec))
        }
        else                   -> {
          val childWithMeasureSpec = MeasureSpec.makeMeasureSpec(0, UNSPECIFIED)
          val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, UNSPECIFIED)
          measureChild(child, childWithMeasureSpec, childHeightMeasureSpec)

          val measuredWidth = ceil(child.measuredWidth * abs(cos(angleC)) + child.measuredHeight * abs(sin(angleC))).toInt()
          val measuredHeight = ceil(child.measuredWidth * abs(sin(angleC)) + child.measuredHeight * abs(cos(angleC))).toInt()

          setMeasuredDimension(
            View.resolveSize(measuredWidth, widthMeasureSpec),
            View.resolveSize(measuredHeight, heightMeasureSpec))
        }
      }
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    val layoutWidth = r - l
    val layoutHeight = b - t

    if (angleChanged || changed) {
      val layoutRect = tempRectF1
      layoutRect.set(0f, 0f, layoutWidth.toFloat(), layoutHeight.toFloat())
      val layoutRectRotated = tempRectF2
      rotateMatrix.setRotate(angle.toFloat(), layoutRect.centerX(), layoutRect.centerY())
      rotateMatrix.mapRect(layoutRectRotated, layoutRect)
      layoutRectRotated.round(viewRectRotated)
      angleChanged = false
    }

    val child = view
    if (child != null) {
      val childLeft = (layoutWidth - child.measuredWidth) / 2
      val childTop = (layoutHeight - child.measuredHeight) / 2
      val childRight = childLeft + child.measuredWidth
      val childBottom = childTop + child.measuredHeight
      child.layout(childLeft, childTop, childRight, childBottom)
    }
  }

  override fun dispatchDraw(canvas: Canvas) {
    canvas.save()
    canvas.rotate((-angle).toFloat(), width / 2f, height / 2f)
    super.dispatchDraw(canvas)
    canvas.restore()
  }

  override fun invalidateChildInParent(location: IntArray, dirty: Rect): ViewParent? {
    invalidate()
    return super.invalidateChildInParent(location, dirty)
  }

  override fun dispatchTouchEvent(event: MotionEvent): Boolean {
    viewTouchPoint[0] = event.x
    viewTouchPoint[1] = event.y

    rotateMatrix.mapPoints(childTouchPoint, viewTouchPoint)

    event.setLocation(childTouchPoint[0], childTouchPoint[1])
    val result = super.dispatchTouchEvent(event)
    event.setLocation(viewTouchPoint[0], viewTouchPoint[1])

    return result
  }
}