package com.organic.journey

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.organic.journey.databinding.ActivityDashboardScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class DashboardScreen : AppCompatActivity() {

    lateinit var binding : ActivityDashboardScreenBinding

    var player: ExoPlayer?= null

    override fun onStart() {
        super.onStart()
       // initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = true
        //player?.release()
        // player1?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding =  ActivityDashboardScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.Main).async{
            startMarkerBounceAnimation()
        }


       // setContentView(R.layout.activity_dashboard_screen)

    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(applicationContext).build()
        binding.exoPlayer.player = player
        binding.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        binding.exoPlayer.useController = false

        // Enable video looping (reloop)
        player?.repeatMode = ExoPlayer.REPEAT_MODE_ONE

        // Mute video sound
        player?.volume = 0f

        var driveStreamUrl = "https://organicjourney.in/assets/videos/hero-turmeric.mp4"

        val mediaItem = MediaItem.fromUri(driveStreamUrl);
        player?.setMediaItem(mediaItem);

        player?.prepare()
        player?.playWhenReady = true
    }

    fun startMarkerBounceAnimation() {
        // Move -12dp upwards from original Y position
        val bounceAnimator = ObjectAnimator.ofFloat(
            binding.ivLocationMarker,
            "translationY",
            0f,
            -12f
        ).apply {
            duration = 600 // 600ms for up/down motion
            repeatCount = ValueAnimator.INFINITE // Repeat forever
            repeatMode = ValueAnimator.REVERSE // Smooth bounce back down
        }

        bounceAnimator.start()
    }


}