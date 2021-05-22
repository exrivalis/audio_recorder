package com.thezone.audiorecorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint()

    init {
        paint.color = Color.rgb(244, 81, 30)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.drawRoundRect(RectF(20f, 30f, 20+30f, 30f+60f), 6f, 6f, paint)
        canvas?.drawRoundRect(RectF(60f, 60f, 60+80f, 60f+360f), 6f, 6f, paint)
    }
}