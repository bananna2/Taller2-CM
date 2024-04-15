package com.example.taller2

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity2 : AppCompatActivity() {
    private lateinit var mProjection: Array<String>
    private lateinit var mCursor: Cursor
    private var mContactsAdapter: ListAdapter? = null
    private var mlista: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mlista = findViewById(R.id.lista)
        mProjection = arrayOf(
            ContactsContract.Profile._ID,
            ContactsContract.Profile.DISPLAY_NAME_PRIMARY
        )
        mContactsAdapter = ListAdapter(this, null, 0)
        mlista?.adapter = mContactsAdapter

        // Verificar y solicitar permisos
        checkPermission()
    }

    private fun checkPermission() {
        val permission = Manifest.permission.READ_CONTACTS
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Permiso concedido
            initView()
        } else {
            // Permiso denegado, solicitar permiso
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                MY_PERMISSION_REQUEST_READ_CONTACTS
            )
        }
    }

    fun initView() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mCursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, mProjection, null, null, null
            )!!
            mContactsAdapter?.changeCursor(mCursor)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                initView()
            } else {
                // Permiso denegado
                showPermissionDeniedMessage()
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "La función de lectura de contactos no está disponible debido a la falta de permisos.",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val MY_PERMISSION_REQUEST_READ_CONTACTS = 0
    }
}
