package com.mypartner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configAuth()

    }

    private fun configAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            //usuario autenticado
            if (auth.currentUser != null) {
                supportActionBar?.title = auth.currentUser?.displayName
            } else {

                //habilitar todos los proveedores de auth
                val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

                //esperar el resultado
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    //aca se procesa la respuesta
                    val response = IdpResponse.fromResultIntent(it.data)

                    if (it.resultCode == RESULT_OK) {
                        //usuario autenticado
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener { authStateListener }
    }
}