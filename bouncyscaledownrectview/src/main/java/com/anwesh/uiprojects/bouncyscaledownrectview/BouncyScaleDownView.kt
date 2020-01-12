package com.anwesh.uiprojects.bouncyscaledownrectview

/**
 * Created by anweshmishra on 12/01/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val bars : Int = 5
val scGap : Float = 0.02f / bars
val delay : Long = 20L / bars
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBar(i : Int, scale : Float, w : Float, paint : Paint) {
    val gap : Float = w / (nodes + 1)
    val sf : Float = scale.sinify().divideScale(i, bars)
    val size : Float = gap * (1 - sf) * 0.5f
    save()
    translate(gap * (i + 1), 0f)
    drawRect(RectF(-size, -size, size, size), paint)
    restore()
}

fun Canvas.drawBars(scale : Float, w : Float, paint : Paint) {
    for (j in 0..(bars - 1)) {
        drawBar(j, scale, w, paint)
    }
}

fun Canvas.drawSBDNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = foreColor
    val gap : Float = h / (nodes + 1)
    save()
    translate(0f, gap * (i + 1))
    drawBars(scale, w, paint)
    restore()
}

class BouncyScaleDownView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }

    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.invalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SBDNode(var i : Int, val state : State = State()) {

        private var next : SBDNode? = null
        private var prev : SBDNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SBDNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSBDNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SBDNode {
            var curr : SBDNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BouncyScaleDown(var i : Int) {

        private val root : SBDNode = SBDNode(0)
        private var curr : SBDNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BouncyScaleDownView) {

        private val animator : Animator = Animator(view)
        private val bsd : BouncyScaleDown = BouncyScaleDown(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bsd.draw(canvas, paint)
            animator.animate {
                bsd.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bsd.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BouncyScaleDownView {
            val view : BouncyScaleDownView = BouncyScaleDownView(activity)
            activity.setContentView(view)
            return view
        }
    }
}