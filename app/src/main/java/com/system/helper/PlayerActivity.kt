package com.system.helper

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer

    private lateinit var controlLayout: View

    private lateinit var seekBar: SeekBar

    private val handler =
        Handler(Looper.getMainLooper())

    private var isPortrait = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        supportActionBar?.hide()

        setContentView(R.layout.activity_player)

        val playerView =
            findViewById<PlayerView>(
                R.id.playerView
            )

        controlLayout =
            findViewById(R.id.controlLayout)

        seekBar =
            findViewById(R.id.seekBar)

        val rotateButton =
            findViewById<ImageButton>(
                R.id.rotateButton
            )

        player = ExoPlayer.Builder(this).build()

        playerView.player = player

        playerView.useController = false

        val videoPath =
            intent.getStringExtra("videoUri")

        if (videoPath != null) {

            val file = File(videoPath)

            val mediaItem =
                MediaItem.fromUri(
                    Uri.fromFile(file)
                )

            player.setMediaItem(mediaItem)

            player.prepare()

            player.play()
        }

        controlLayout.visibility = View.GONE

        playerView.setOnClickListener {

            if (player.isPlaying) {

                player.pause()

                controlLayout.visibility =
                    View.VISIBLE

            } else {

                player.play()

                controlLayout.visibility =
                    View.GONE
            }
        }

        rotateButton.setOnClickListener {

            if (isPortrait) {

                requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                isPortrait = false

            } else {

                requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                isPortrait = true
            }
        }

        startSeekBarUpdate()
    }

    private fun startSeekBarUpdate() {

        handler.post(object : Runnable {

            override fun run() {

                if (player.duration > 0) {

                    seekBar.max =
                        player.duration.toInt()

                    seekBar.progress =
                        player.currentPosition.toInt()
                }

                handler.postDelayed(this, 500)
            }
        })

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    if (fromUser) {

                        player.seekTo(
                            progress.toLong()
                        )
                    }
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            })
    }

    override fun onPause() {
        super.onPause()

        player.pause()

        controlLayout.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }
}
