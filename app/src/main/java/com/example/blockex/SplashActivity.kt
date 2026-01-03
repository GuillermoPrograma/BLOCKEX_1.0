package com.example.blockex

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        videoView = findViewById(R.id.videoView)
        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.corazonvideo}")
        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = false
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)

            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight

            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            val videoRatio = videoWidth.toFloat() / videoHeight
            val screenRatio = screenWidth.toFloat() / screenHeight

            val layoutParams = videoView.layoutParams

            if (videoRatio > screenRatio) {
                // ajustar por ancho
                layoutParams.width = screenWidth
                layoutParams.height = (screenWidth / videoRatio).toInt()
            } else {
                // ajustar por alto
                layoutParams.height = screenHeight
                layoutParams.width = (screenHeight * videoRatio).toInt()
            }

            videoView.layoutParams = layoutParams
        }

        videoView.start()

        videoView.setOnCompletionListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}
