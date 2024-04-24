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
import android.widget.ImageView
import kotlin.random.Random
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    private lateinit var parentLayout: RelativeLayout
    private lateinit var movableButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private val monsters = mutableListOf<ImageView>()

    private val spawnMonsterRunnable = object : Runnable {
        override fun run() {
            spawnMonster()
            handler.postDelayed(this, 10000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        parentLayout = findViewById<RelativeLayout>(R.id.parentLayout)

        handler.post(spawnMonsterRunnable)
        handler.post(updateMonsterAnimationsRunnable)

        // Create a Runnable to periodically check for collisions
        val collisionCheckRunnable = object : Runnable {
            override fun run() {
                checkForCollisions()
                handler.postDelayed(this, 500)  // Check every 500ms
            }
        }

        // Start the collision check
        handler.post(collisionCheckRunnable)

        movableButton = findViewById<Button>(R.id.movableButton)

        parentLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {

                val targetX = event.x - movableButton.width / 2.0f
                val targetY = event.y - movableButton.height / 2.0f

                val animatorX = ObjectAnimator.ofFloat(movableButton, "x", movableButton.x, targetX)
                animatorX.duration = 500
                animatorX.start()

                val animatorY = ObjectAnimator.ofFloat(movableButton, "y", movableButton.y, targetY)
                animatorY.duration = 500
                animatorY.start()
            }
            true
        }

    }

    private fun spawnMonster() {
        val monster = ImageView(this).apply {
            setImageResource(R.drawable.monster_icon)
            val sizeInPixels = dpToPx(75)
            layoutParams = RelativeLayout.LayoutParams(sizeInPixels, sizeInPixels)
        }

        var x: Int
        var y: Int
        val minDistance = dpToPx(100)

        do {
            x = Random.nextInt(parentLayout.width - monster.layoutParams.width)
            y = Random.nextInt(parentLayout.height - monster.layoutParams.height)
        } while (!isPositionValid(x.toFloat(), y.toFloat(), minDistance))


        monster.x = x.toFloat()
        monster.y = y.toFloat()

        parentLayout.addView(monster)
        monsters.add(monster)
        animateMonster(monster)
    }

    private fun isPositionValid(x: Float, y: Float, minDistance: Int): Boolean {
        val buttonCenterX = movableButton.x + movableButton.width / 2
        val buttonCenterY = movableButton.y + movableButton.height / 2
        val monsterCenterX = x + dpToPx(75) / 2
        val monsterCenterY = y + dpToPx(75) / 2

        val distance = Math.sqrt(
            ((monsterCenterX - buttonCenterX) * (monsterCenterX - buttonCenterX) +
                    (monsterCenterY - buttonCenterY) * (monsterCenterY - buttonCenterY)).toDouble()
        )

        return distance >= minDistance
    }

    private val updateMonsterAnimationsRunnable = object : Runnable {
        override fun run() {
            monsters.forEach { animateMonster(it) }
            handler.postDelayed(this, 1000)
        }
    }

    private fun animateMonster(monster: ImageView) {
        monster.animate().cancel()

        val targetX = movableButton.x + movableButton.width / 2 - monster.width / 2
        val targetY = movableButton.y + movableButton.height / 2 - monster.height / 2

        val animatorX = ObjectAnimator.ofFloat(monster, "x", monster.x, targetX)
        animatorX.duration = 3000
        animatorX.start()

        val animatorY = ObjectAnimator.ofFloat(monster, "y", monster.y, targetY)
        animatorY.duration = 3000
        animatorY.start()
    }
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(spawnMonsterRunnable)
        handler.removeCallbacks(updateMonsterAnimationsRunnable)
    }

    private fun checkForCollisions() {
        val buttonRect = getRectForButton(movableButton)

        monsters.forEach { monster ->
            val monsterRect = getRectForMonster(monster)

            if (buttonRect.intersect(monsterRect)) {
                // Collision detected, remove all monsters and show Snackbar
                clearMonsters()
                showGameOverSnackbar()
            }
        }
    }

    // Define the method to clear all monsters
    private fun clearMonsters() {
        monsters.forEach { monster ->
            parentLayout.removeView(monster)
        }
        monsters.clear()
    }

    // Define a method to show the Snackbar
    private fun showGameOverSnackbar() {
        Snackbar.make(parentLayout, "Game Over!", Snackbar.LENGTH_LONG)
            .setAction("Restart") { restartGame() }  // Optional restart action
            .setActionTextColor(ContextCompat.getColor(this, R.color.black))
            .show()
    }

    // Helper method to get the button's bounds
    private fun getRectForButton(button: Button): android.graphics.Rect {
        val left = button.x.toInt()
        val top = button.y.toInt()
        val right = left + button.width
        val bottom = top + button.height
        return android.graphics.Rect(left, top, right, bottom)
    }

    // Helper method to get the monster's bounds
    private fun getRectForMonster(monster: ImageView): android.graphics.Rect {
        val left = monster.x.toInt()
        val top = monster.y.toInt()
        val right = left + monster.layoutParams.width
        val bottom = top + monster.layoutParams.height
        return android.graphics.Rect(left, top, right, bottom)
    }

    // Method to restart the game (optional)
    private fun restartGame() {
        // Code to restart the game, such as restarting the spawning of monsters
        handler.post(spawnMonsterRunnable)
    }

}