package com.example.taller2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, MainActivity2::class.java)

// Inicia la Activity 2 usando el Intent
        val imagen1: ImageView = findViewById(R.id.imageButton1)

        // Agrega un listener de clic al botón de imagen
        imagen1.setOnClickListener {
            // Crea un Intent para abrir la actividad 2
            val intent = Intent(this, MainActivity2::class.java)

            // Inicia la actividad 2
            startActivity(intent)
        }


        val imagen2: ImageView = findViewById(R.id.imageButton2)

        // Agrega un listener de clic al botón de imagen
        imagen2.setOnClickListener {
            // Crea un Intent para abrir la actividad 3
            val intent = Intent(this, MainActivity3::class.java)

            // Inicia la actividad
            startActivity(intent)
        }
        val imagen3: ImageView = findViewById(R.id.imageButton3)

        // Agrega un listener de clic al botón de imagen
        imagen3.setOnClickListener {
            // Check if it has permission for location
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Permission already granted
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            } else {
                // Permission not granted
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                checkPermissionForLocation(this)
            }
        }
    }

    private fun checkPermissionForLocation(activity: AppCompatActivity) {
        when{
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // If user previously denied the permission
                Toast.makeText(this, "Permission previously denied", Toast.LENGTH_SHORT).show()
                requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

            else -> {
                // Always call the own function to request permission, not the system one (requestPermissions)
                requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

        }
    }

    private fun requestPermission(context: Activity, permission: String, requestCode: Int, justify: String) {
        if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(this, justify, Toast.LENGTH_SHORT).show()
            }
            requestPermissions(arrayOf(permission), requestCode)
        } else {
            // Permission granted
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    companion object {
        const val LOCATION_PERMISSION_CODE = 1
    }
}