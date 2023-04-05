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


        val Box = findViewById<EditText>(R.id.inputBox)
        val dl_status = findViewById<TextView>(R.id.textView2)
        val Btn = findViewById<Button>(R.id.button)


        Btn.setOnClickListener() {
//            dl_status.text = "Download Started"
            if (Box.text.toString() != "") {
                Toast.makeText(this, "Download Started", Toast.LENGTH_LONG).show()
                try {
                    downloader?.call(Box.text.toString())
                    Toast.makeText(this, "Download Finished", Toast.LENGTH_LONG).show()
                    dl_status.text = "Download Finished"
                } catch (error: Throwable) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                    println(error)
                }
            }
            else
            {
                Toast.makeText(this, "Empty Field", Toast.LENGTH_LONG).show()
            }
        }
    }
}