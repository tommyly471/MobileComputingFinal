package com.example.vampiresurvivors

import android.os.Bundle
import android.util.AttributeSet
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.MotionEvent
import android.widget.Button
import android.widget.RelativeLayout
import android.animation.ObjectAnimator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val parentLayout = findViewById<RelativeLayout>(R.id.parentLayout)
        val movableButton = findViewById<Button>(R.id.movableButton)

        parentLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {

                val targetX = event.x - movableButton.width / 2.0f
                val targetY = event.y - movableButton.height / 2.0f

                val animatorX = ObjectAnimator.ofFloat(movableButton, "x", movableButton.x, targetX)
                animatorX.duration = 500 // Animation duration in milliseconds
                animatorX.start()

                val animatorY = ObjectAnimator.ofFloat(movableButton, "y", movableButton.y, targetY)
                animatorY.duration = 500
                animatorY.start()
            }
            true
        }
    }
}