package com.system.helper

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class FakeHomeActivity : AppCompatActivity() {

    private var clickCount = 0

    private val handler = Handler()

    private val hiddenPassword = "9527"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fake_home)

        val titleText =
            findViewById<TextView>(
                R.id.titleText
            )

        val batteryText =
            findViewById<TextView>(
                R.id.batteryText
            )

        val tempText =
            findViewById<TextView>(
                R.id.tempText
            )

        val ramText =
            findViewById<TextView>(
                R.id.ramText
            )

        val cacheText =
            findViewById<TextView>(
                R.id.cacheText
            )

        val ramBar =
            findViewById<ProgressBar>(
                R.id.ramBar
            )

        val cleanButton =
            findViewById<Button>(
                R.id.cleanButton
            )

        val ramUsed =
            Random.nextInt(4, 8)

        val ramPercent =
            Random.nextInt(45, 92)

        val temp =
            Random.nextInt(30, 41)

        val cache =
            Random.nextDouble(0.8, 3.2)

        val batteryStates =
            listOf(
                "Excellent",
                "Good",
                "Normal"
            )

        batteryText.text =
            batteryStates.random()

        tempText.text =
            "Temperature: ${temp}°C"

        ramText.text =
            "${ramUsed}.2GB / 8GB Used"

        ramBar.progress =
            ramPercent

        cacheText.text =
            "Junk Cache: ${
                String.format("%.1f", cache)
            }GB"

        titleText.setOnClickListener {

            clickCount++

            if (clickCount == 3) {

                clickCount = 0

                showPasswordDialog()
            }

            handler.removeCallbacksAndMessages(null)

            handler.postDelayed({

                clickCount = 0

            }, 1000)
        }

        cleanButton.setOnClickListener {

            simulateCleaning()
        }
    }

    private fun simulateCleaning() {

        val progressDialog =
            ProgressDialog(this)

        progressDialog.setTitle(
            "System Cleaner"
        )

        progressDialog.setMessage(
            "Scanning..."
        )

        progressDialog.setCancelable(false)

        progressDialog.show()

        handler.postDelayed({

            progressDialog.setMessage(
                "Cleaning Cache..."
            )

        }, 1200)

        handler.postDelayed({

            progressDialog.setMessage(
                "Optimizing System..."
            )

        }, 2500)

        handler.postDelayed({

            progressDialog.dismiss()

            Toast.makeText(
                this,
                "System Optimized",
                Toast.LENGTH_SHORT
            ).show()

        }, 4000)
    }

    private fun showPasswordDialog() {

        val input = EditText(this)

        input.inputType =
            InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Security Verification")
            .setMessage("Enter Access Code")
            .setView(input)
            .setPositiveButton("Unlock") { _, _ ->

                val password =
                    input.text.toString()

                if (password == hiddenPassword) {

                    startActivity(
                        Intent(
                            this,
                            HiddenVideoActivity::class.java
                        )
                    )

                } else {

                    Toast.makeText(
                        this,
                        "Invalid Code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }
}
