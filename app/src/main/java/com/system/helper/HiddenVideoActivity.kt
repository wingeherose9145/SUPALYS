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
import java.util.UUID

class HiddenVideoActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    private lateinit var adapter: ArrayAdapter<String>

    private val videoList = mutableListOf<String>()

    private val videoPaths = mutableListOf<String>()

    private val pickVideos =
        registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->

            uris.forEach {

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

            pickVideos.launch(
                arrayOf("video/*")
            )
        }

        listView.setOnItemClickListener { _, _, position, _ ->

            val intent = Intent(
                this,
                PlayerActivity::class.java
            )

            intent.putStringArrayListExtra(
                "videoList",
                ArrayList(videoPaths)
            )

            intent.putExtra(
                "currentIndex",
                position
            )

            startActivity(intent)
        }
    }

    private fun importVideo(uri: Uri) {

        val inputStream =
            contentResolver.openInputStream(uri)
                ?: return

        val hiddenDir =
            File(filesDir, ".sys")

        if (!hiddenDir.exists()) {

            hiddenDir.mkdirs()
        }

        val randomName =
            UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .uppercase()

        val outputFile =
            File(hiddenDir, randomName)

        val outputStream =
            FileOutputStream(outputFile)

        inputStream.copyTo(outputStream)

        inputStream.close()

        outputStream.close()

        videoList.add(randomName)

        videoPaths.add(outputFile.absolutePath)

        adapter.notifyDataSetChanged()
    }

    private fun loadSavedVideos() {

        val hiddenDir =
            File(filesDir, ".sys")

        if (!hiddenDir.exists()) return

        val files =
            hiddenDir.listFiles()

        files?.sortedBy { it.name }?.forEach {

            videoList.add(it.name)

            videoPaths.add(it.absolutePath)
        }

        adapter.notifyDataSetChanged()
    }
}
