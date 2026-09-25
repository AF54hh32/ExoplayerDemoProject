package com.example.nextwatch

import com.google.android.exoplayer2.MediaItem
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.nextwatch.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


class MainActivity : AppCompatActivity(), Player.Listener {
    lateinit var binding : ActivityMainBinding

    var player: ExoPlayer?= null
    var isPipMode: Boolean ?= false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
    }

    override fun onResume() {
        super.onResume()
        if (player?.isPlaying == true) {
            isPipMode = false
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(applicationContext).build()
        binding.exoPlayer.player = player
        binding.exoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

      //  var driveStreamUrl = "https://drive.google.com/uc?export=download&confirm=t&id=1nFAdlh4gzJlxSl0iXfqhkV2ozIm3uK_R"
        var driveStreamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
      //  var driveStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"



// 2. Configure HTTP Data Source to allow cross-protocol redirects
   //    val dataSourceFactory =  DefaultHttpDataSource.Factory()
     //   .setAllowCrossProtocolRedirects(true);
      val mediaItem = MediaItem.fromUri(driveStreamUrl);
      player?.setMediaItem(mediaItem);
// 3. Build Progressive Media Source (for MP4 / video containers)
//val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
  //      .createMediaSource(MediaItem.fromUri(driveStreamUrl));

// 4. Pass to ExoPlayer
       // player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.playWhenReady = true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBackPressed() {
        if(!isPipMode!!) {
            enterPictureInPictureMode()
            isPipMode = true
        } else {
            super.onBackPressed()
        }
    }

    private fun hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE
        )
    }
}