package com.system.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream

class HiddenVideoActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    private lateinit var adapter: ArrayAdapter<String>

    private val videoList = mutableListOf<String>()

    private val videoFiles = mutableListOf<File>()

    private val pickVideo =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {

                importVideo(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hidden_video)

        listView = findViewById(R.id.videoList)

        val addButton =
            findViewById<FloatingActionButton>(
                R.id.addButton
            )

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            videoList
        )

        listView.adapter = adapter

        loadSavedVideos()

        addButton.setOnClickListener {

            pickVideo.launch("video/*")
        }

        listView.setOnItemClickListener { _, _, position, _ ->

            val file = videoFiles[position]

            val intent = Intent(
                this,
                PlayerActivity::class.java
            )

            intent.putExtra(
                "videoUri",
                file.absolutePath
            )

            startActivity(intent)
        }
    }

    private fun importVideo(uri: Uri) {

        val inputStream =
            contentResolver.openInputStream(uri)
                ?: return

        val videoDir = File(filesDir, "videos")

        if (!videoDir.exists()) {

            videoDir.mkdirs()
        }

        val fileName =
            "video_${System.currentTimeMillis()}.mp4"

        val outputFile = File(videoDir, fileName)

        val outputStream =
            FileOutputStream(outputFile)

        inputStream.copyTo(outputStream)

        inputStream.close()

        outputStream.close()

        videoList.add(fileName)

        videoFiles.add(outputFile)

        adapter.notifyDataSetChanged()
    }

    private fun loadSavedVideos() {

        val videoDir = File(filesDir, "videos")

        if (!videoDir.exists()) return

        val files = videoDir.listFiles()

        files?.forEach {

            videoList.add(it.name)

            videoFiles.add(it)
        }

        adapter.notifyDataSetChanged()
    }
}
