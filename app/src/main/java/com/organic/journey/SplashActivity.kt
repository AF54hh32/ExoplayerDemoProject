package com.organic.journey

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.organic.journey.databinding.ActivitySplashScreenBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startProgressAnimation()
    }

    private fun startProgressAnimation() {
        // Animate integer from 0 to 100 over 3 seconds
        val animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 3000
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Int
                binding.progressBar.progress = progress
                binding.tvProgress.text = "$progress%"
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    navigateToNextScreen()
                }
            })
        }

        animator.start()
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this@SplashActivity, PdfActivity::class.java)
        startActivity(intent)
        finish() // Close splash activity from back stack
    }
}