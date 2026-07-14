package com.system.helper

import android.content.pm.ActivityInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var seekBar: SeekBar
    private lateinit var timeText: TextView
    private lateinit var btnRewind: ImageButton

    private lateinit var videoUris: ArrayList<String>
    private var currentIndex = 0

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gestureDetector: GestureDetector

    // 自动隐藏控制
    private val hideControlsHandler = Handler(Looper.getMainLooper())
    private val hideControlsRunnable = Runnable {
        findViewById<View>(R.id.topControls).visibility = View.GONE
        seekBar.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        seekBar = findViewById(R.id.seekBar)
        timeText = findViewById(R.id.timeText)
        btnRewind = findViewById(R.id.btnRewind)

        player = ExoPlayer.Builder(this).build()

        playerView.player = player
        playerView.useController = false

        videoUris = intent.getStringArrayListExtra("video_list") ?: arrayListOf()
        currentIndex = intent.getIntExtra("current_index", 0)

        if (videoUris.isEmpty()) {
            Toast.makeText(this, "没有视频可播放", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupGestureDetector()
        setupSeekBar()
        setupRewindButton()
        setupAutoHideControls()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playNextVideo()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    showControlsTemporarily()
                }
            }
        })

        playCurrentVideo()

        // 点击视频区域切换控制栏显示
        playerView.setOnClickListener {
            toggleControls()
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    private fun setupRewindButton() {
        btnRewind.setOnClickListener {
            val newPosition = (player.currentPosition - 5000).coerceAtLeast(0L)
            player.seekTo(newPosition)
            showControlsTemporarily()
        }
    }

    private fun setupAutoHideControls() {
        showControlsTemporarily()
    }

    private fun showControlsTemporarily() {
        val topControls = findViewById<View>(R.id.topControls)
        topControls.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE

        hideControlsHandler.removeCallbacks(hideControlsRunnable)
        hideControlsHandler.postDelayed(hideControlsRunnable, 3000) // 3秒后自动隐藏
    }

    private fun toggleControls() {
        val topControls = findViewById<View>(R.id.topControls)
        if (topControls.visibility == View.VISIBLE) {
            topControls.visibility = View.GONE
            seekBar.visibility = View.GONE
        } else {
            showControlsTemporarily()
        }
    }

    private fun playCurrentVideo() {
        try {
            val uri = Uri.parse(videoUris[currentIndex])
            setVideoOrientation(uri)

            player.stop()
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            player.play()
        } catch (e: Exception) {
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setVideoOrientation(uri: Uri) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0

            requestedOrientation = if (height > width) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            retriever.release()
        } catch (e: Exception) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun playNextVideo() {
        if (currentIndex < videoUris.size - 1) {
            currentIndex++
            playCurrentVideo()
        }
    }

    private fun playPreviousVideo() {
        if (currentIndex > 0) {
            currentIndex--
            playCurrentVideo()
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (kotlin.math.abs(velocityX) > 700) {
                    if (velocityX > 0) {
                        playPreviousVideo()
                    } else {
                        playNextVideo()
                    }
                    return true
                }
                return false
            }
        })

        playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun setupSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                if (player.duration > 0) {
                    seekBar.max = player.duration.toInt()
                    seekBar.progress = player.currentPosition.toInt()

                    // 更新时间显示
                    val current = player.currentPosition / 1000
                    val total = player.duration / 1000
                    timeText.text = String.format("%02d:%02d / %02d:%02d", 
                        current / 60, current % 60, total / 60, total % 60)
                }
                handler.postDelayed(this, 500)
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                showControlsTemporarily()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        hideControlsHandler.removeCallbacksAndMessages(null)
        player.release()
    }
}
