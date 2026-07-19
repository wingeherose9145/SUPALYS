package com.supalys

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val videoUris = mutableListOf<Uri>()
    private val displayNames = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private val PREF_NAME = "VideoPlayerPrefs"
    private val KEY_VIDEO_LIST = "video_list"

    private val pickVideos = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult

        uris.forEach { uri ->
            if (videoUris.contains(uri)) return@forEach

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            videoUris.add(uri)
            displayNames.add(getFileNameFromUri(uri))
        }
        adapter.notifyDataSetChanged()
        saveVideoList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.videoListView)
        val addButton = findViewById<Button>(R.id.addButton)
        val clearButton = findViewById<Button>(R.id.clearButton)  // 新增清除按钮

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        listView.adapter = adapter

        loadVideoList()   // 加载历史列表

        // 添加视频
        addButton.setOnClickListener {
            pickVideos.launch(arrayOf("video/*"))
        }

        // 清除全部列表
        clearButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("清除列表")
                .setMessage("确定清除所有视频？")
                .setPositiveButton("清除") { _, _ ->
                    clearVideoList()
                }
                .setNegativeButton("取消", null)
                .show()
        }

        // 点击播放
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putStringArrayListExtra(
                "video_list",
                ArrayList(videoUris.map { it.toString() })
            )
            intent.putExtra("current_index", position)
            startActivity(intent)
        }

        // 长按删除单个
        listView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("删除视频")
                .setMessage("确定删除该视频？")
                .setPositiveButton("删除") { _, _ ->
                    if (position in videoUris.indices) {
                        videoUris.removeAt(position)
                        displayNames.removeAt(position)
                        adapter.notifyDataSetChanged()
                        saveVideoList()
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("取消", null)
                .show()
            true
        }
    }

    private fun saveVideoList() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val uriStrings = videoUris.map { it.toString() }.toSet()
        prefs.edit().putStringSet(KEY_VIDEO_LIST, uriStrings).apply()
    }

    private fun loadVideoList() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedUris = prefs.getStringSet(KEY_VIDEO_LIST, emptySet()) ?: emptySet()

        videoUris.clear()
        displayNames.clear()

        savedUris.forEach { uriStr ->
            try {
                val uri = Uri.parse(uriStr)
                videoUris.add(uri)
                displayNames.add(getFileNameFromUri(uri))
            } catch (_: Exception) {}
        }
        adapter.notifyDataSetChanged()
    }

    private fun clearVideoList() {
        videoUris.clear()
        displayNames.clear()
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().remove(KEY_VIDEO_LIST).apply()
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "列表已清除", Toast.LENGTH_SHORT).show()
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    cursor.getString(nameIndex)
                } else "未知视频"
            } ?: "未知视频"
        } catch (e: Exception) {
            "未知视频"
        }
    }
}
