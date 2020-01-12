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
val scGap : Float = 0.02f
val delay : Long = 20
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
    val gap : Float = h / (nodes + 1)
    save()
    translate(0f, gap * (i + 1))
    drawBars(scale, w, paint)
    restore()
}

class BouncyScaleDownView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}