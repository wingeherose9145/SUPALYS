package com.system.helper

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FakeHomeActivity : AppCompatActivity() {

    private var clickCount = 0

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fake_home)

        val titleText =
            findViewById<TextView>(
                R.id.titleText
            )

        titleText.setOnClickListener {

            clickCount++

            if (clickCount == 5) {

                clickCount = 0

                startActivity(
                    Intent(
                        this,
                        HiddenVideoActivity::class.java
                    )
                )
            }

            handler.removeCallbacksAndMessages(null)

            handler.postDelayed({

                clickCount = 0

            }, 1000)
        }

        Toast.makeText(
            this,
            "System Status Normal",
            Toast.LENGTH_SHORT
        ).show()
    }
}
