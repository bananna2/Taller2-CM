package com.example.taller2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

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
            // Crea un Intent para abrir la actividad 3
            val intent = Intent(this, MapsActivity::class.java)

            // Inicia la actividad
            startActivity(intent)
        }


    }
}