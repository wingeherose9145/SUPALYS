package com.system.helper

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    private val pickVideos = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->

        uris?.forEach { uri ->

            if (!videoUris.contains(uri)) {
                videoUris.add(uri)

                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val name = getFileNameFromUri(uri)
                displayNames.add(name)
            }
        }

        adapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.videoListView)
        val addButton = findViewById(R.id.addButton)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            displayNames
        )

        listView.adapter = adapter

        addButton.setOnClickListener {
            pickVideos.launch(arrayOf("video/*"))
        }

        // 点击播放
        listView.setOnItemClickListener { _, _, position, _ ->

            val intent = Intent(this, PlayerActivity::class.java)

            intent.putExtra(
                "video_uri",
                videoUris[position].toString()
            )

            intent.putExtra(
                "current_index",
                position
            )

            intent.putStringArrayListExtra(
                "video_list",
                ArrayList(videoUris.map { it.toString() })
            )

            startActivity(intent)
        }

        // 长按删除
        listView.setOnItemLongClickListener { _, _, position, _ ->

            AlertDialog.Builder(this)
                .setTitle("删除视频")
                .setMessage("确定从列表中删除该视频？")
                .setPositiveButton("删除") { _, _ ->

                    videoUris.removeAt(position)
                    displayNames.removeAt(position)

                    adapter.notifyDataSetChanged()

                    Toast.makeText(
                        this,
                        "已删除",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("取消", null)
                .show()

            true
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {

        return contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->

            val nameIndex =
                cursor.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME
                )

            if (cursor.moveToFirst() && nameIndex != -1) {
                cursor.getString(nameIndex)
            } else {
                "未知视频"
            }

        } ?: "未知视频"
    }
}
