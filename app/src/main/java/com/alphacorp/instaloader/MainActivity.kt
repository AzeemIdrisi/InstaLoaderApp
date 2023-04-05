package com.alphacorp.instaloader

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf<String>(WRITE_EXTERNAL_STORAGE), 1)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }

        val py = Python.getInstance()
        val module = py.getModule("script")
        val downloader = module["download"]


        val Box = findViewById<EditText>(R.id.inputBox)
        val dl_status =findViewById<TextView>(R.id.textView2)
        val Btn = findViewById<Button>(R.id.button)



        Btn.setOnClickListener {
            Toast.makeText(this, "Download Started", Toast.LENGTH_SHORT).show()
            dl_status.text= "Download Started"
            downloader?.call(Box.text.toString())
            dl_status.text= "Download Finished"
        }


    }
}