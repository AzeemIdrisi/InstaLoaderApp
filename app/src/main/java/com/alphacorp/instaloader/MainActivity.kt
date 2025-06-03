package com.alphacorp.instaloader

import android.Manifest.permission.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alphacorp.instaloader.databinding.ActivityMainBinding
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val CHANNEL_ID = "download_channel"
    private val NOTIFICATION_ID = 1
    private var pythonInitialized = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            initializePython()
        } else {
            showPermissionDeniedMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()
        createNotificationChannel()
        checkAndRequestPermissions()

        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MainFragment())
                .commit()
        }
    }

    private fun setupNavigationDrawer() {
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    fun startDownload(input: String, isProfile: Boolean) {
        if (!pythonInitialized) {
            showSnackbar("Python is not initialized. Please grant storage permissions.")
            return
        }

        showNotification("Download Started", "Downloading content from Instagram")
        showToast("Download started...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!isProfile) {
                    downloadPost(input)
                } else {
                    downloadProfile(input)
                }
                withContext(Dispatchers.Main) {
                    showNotification("Download Complete", "Content has been downloaded successfully")
                    showToast("Download completed successfully!")
                    (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? MainFragment)?.clearStatusText()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showNotification("Download Failed", "Error: ${e.message}")
                    showToast("Download failed: ${e.message}")
                    (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? MainFragment)?.clearStatusText()
                }
            }
        }
    }

    private suspend fun downloadPost(url: String) {
        if (!pythonInitialized) {
            throw IllegalStateException("Python is not initialized")
        }
        
        val py = Python.getInstance()
        val module = py.getModule("script")
        val linkDownloader = module["download_post_from_link"]

        val postShortcode = when {
            url.startsWith("https://www.instagram.com/p/") -> 
                url.substringAfter("https://www.instagram.com/p/").substringBefore("/")
            url.startsWith("https://www.instagram.com/reel/") -> 
                url.substringAfter("https://www.instagram.com/reel/").substringBefore("/")
            else -> throw IllegalArgumentException("Invalid Instagram post URL")
        }

        linkDownloader?.call(postShortcode)
    }

    private suspend fun downloadProfile(username: String) {
        if (!pythonInitialized) {
            throw IllegalStateException("Python is not initialized")
        }
        
        val py = Python.getInstance()
        val module = py.getModule("script")
        val downloader = module["download"]
        val posts = module["post_count"]

        val postCount = posts?.call(username) as? Int ?: 0
        withContext(Dispatchers.Main) {
            showNotification("Download Started", "Found $postCount posts, Downloading...")
            showToast("Found $postCount posts, starting download...")
        }

        downloader?.call(username)
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            } else {
                initializePython()
            }
        } else {
            val permissions = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
            if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                initializePython()
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    private fun initializePython() {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            pythonInitialized = true
        } catch (e: Exception) {
            showSnackbar("Failed to initialize Python: ${e.message}")
            pythonInitialized = false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Download Notifications"
            val descriptionText = "Notifications for download status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showPermissionDeniedMessage() {
        showSnackbar("Storage permission is required for downloading content")
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                navigateToFragment(MainFragment())
            }
            R.id.nav_about -> {
                navigateToFragment(AboutFragment())
            }
        }
        binding.drawerLayout.close()
        return true
    }

    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

