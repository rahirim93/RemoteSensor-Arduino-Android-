package com.example.remotesensor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class LaunchActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        registerPermissionLauncher()

        requestPermissions()
    }

    private fun requestPermissions() {
        val permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        Log.d("ggg", "1: $permission1")
        Log.d("ggg", "2: $permission2")

        if(permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this , MainActivity::class.java)
            startActivity(intent)
        } else if (permission1 != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (permission2 != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it) {
                requestPermissions()
            } else {
                requestPermissions()
            }
        }
    }
}