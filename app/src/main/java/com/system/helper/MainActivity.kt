package com.system.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val videoPaths = mutableListOf<String>()
    private val displayNames = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private val pickVideos = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris?.forEach { uri -> saveVideo(uri) }
        loadVideos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.videoListView)
        val addButton = findViewById<Button>(R.id.addButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        listView.adapter = adapter

        addButton.setOnClickListener {
            pickVideos.launch(arrayOf("video/*"))
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putStringArrayListExtra("video_list", ArrayList(videoPaths))
            intent.putExtra("current_index", position)
            startActivity(intent)
        }

        loadVideos()
    }

    private fun loadVideos() {
        videoPaths.clear()
        displayNames.clear()

        val dir = File(filesDir, "videos")
        if (!dir.exists()) dir.mkdirs()

        dir.listFiles()?.sortedBy { it.name }?.forEach { file ->
            videoPaths.add(file.absolutePath)
            val name = if (file.name.contains("__")) {
                file.name.substringAfter("__")
            } else file.name
            displayNames.add(name)
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveVideo(uri: Uri) {
        val random = UUID.randomUUID().toString().take(12)
        val originalName = "video.mp4"
        val finalName = "${random}__${originalName}"

        val dir = File(filesDir, "videos")
        if (!dir.exists()) dir.mkdirs()

        val outFile = File(dir, finalName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
