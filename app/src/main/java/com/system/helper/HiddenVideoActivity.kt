package com.system.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class HiddenVideoActivity : AppCompatActivity() {

    private lateinit var videoListView: ListView
    private val realVideoPaths = mutableListOf<String>()   // 真实路径，用于播放
    private val displayNames = mutableListOf<String>()     // 显示给用户的原始文件名
    private lateinit var adapter: ArrayAdapter<String>

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris?.forEach { uri ->
            saveVideoToInternalStorage(uri)
        }
        loadVideos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_video)

        val addButton = findViewById<Button>(R.id.addButton)
        videoListView = findViewById(R.id.videoList)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        videoListView.adapter = adapter

        addButton.setOnClickListener {
            pickVideoLauncher.launch(arrayOf("video/*"))
        }

        videoListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putStringArrayListExtra("video_list", ArrayList(realVideoPaths))
            intent.putExtra("video_index", position)
            startActivity(intent)
        }

        loadVideos()
    }

    private fun loadVideos() {
        realVideoPaths.clear()
        displayNames.clear()

        val hiddenDir = File(filesDir, "hidden_videos")
        if (!hiddenDir.exists()) hiddenDir.mkdirs()

        hiddenDir.listFiles()?.sortedBy { it.name }?.forEach { file ->
            realVideoPaths.add(file.absolutePath)

            val displayName = if (file.name.contains("__")) {
                file.name.substringAfter("__")
            } else {
                file.name
            }
            displayNames.add(displayName)
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveVideoToInternalStorage(uri: Uri) {
        var originalName = "video.mp4"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                originalName = cursor.getString(nameIndex)
            }
        }

        val randomName = UUID.randomUUID().toString().replace("-", "").take(12)
        val finalName = "${randomName}__${originalName}"

        val hiddenDir = File(filesDir, "hidden_videos")
        if (!hiddenDir.exists()) hiddenDir.mkdirs()

        val outputFile = File(hiddenDir, finalName)

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
