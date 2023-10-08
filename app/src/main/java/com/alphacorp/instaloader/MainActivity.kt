package com.alphacorp.instaloader

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf<String>(WRITE_EXTERNAL_STORAGE), 1)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {

                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }

        }
        var a = Environment.getExternalStorageDirectory()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }

        val py = Python.getInstance()
        val module = py.getModule("script")
        val downloader = module["download"]
        val posts =  module["post_count"]
        val linkDownloader = module["download_post_from_link"]


        val Box = findViewById<EditText>(R.id.inputBox)
        val dl_status = findViewById<TextView>(R.id.StatusText)
        val Btn = findViewById<Button>(R.id.button)


        Btn.setOnClickListener() {
            if (Box.text.toString() != "") {
                Toast.makeText(this, "Download Started", Toast.LENGTH_LONG).show()

                if (Box.text.toString().startsWith("https://www.instagram.com/p/")) { // checks if the text is a valid instagram link
                    // Post shortcode is a part of the Post URL, https://www.instagram.com/p/SHORTCODE/
                    val url = Box.text.toString()
                    val post_shortcode = url.substringAfter("https://www.instagram.com/p/").substringBefore("/")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            linkDownloader?.call(post_shortcode)
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Download Finished",
                                    Toast.LENGTH_LONG
                                ).show()
                                dl_status.text = "Download Status: Finished"
                            }
                        } catch (error: Throwable) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Something went wrong",
                                    Toast.LENGTH_LONG
                                ).show()
                                val show_error = findViewById<TextView>(R.id.StatusText)
                                val error_name = error.toString().split(":")
                                show_error.text = error_name.toString()
                            }
                        }
                    }
                }
                else { // if the text is not a link, it must be an instagram username
                    try {
                        dl_status.text =
                            "Found ${posts?.call(Box.text.toString())} posts, Downloading..."
                    } catch (error: Throwable) {
                        Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_LONG)
                            .show()
                        val show_error = findViewById<TextView>(R.id.StatusText)
                        val error_name = error.toString().split(":")
                        show_error.text = error_name[error_name.size - 1]
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            downloader?.call(Box.text.toString())
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Download Finished",
                                    Toast.LENGTH_LONG
                                ).show()
                                dl_status.text = "Download Status: Finished"
                            }
                        } catch (error: Throwable) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Something went wrong",
                                    Toast.LENGTH_LONG
                                ).show()
                                val show_error = findViewById<TextView>(R.id.StatusText)
                                val error_name = error.toString().split(":")
                                show_error.text = error_name[error_name.size - 1]
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Empty Field", Toast.LENGTH_LONG).show()
            }
        }
    }
}

