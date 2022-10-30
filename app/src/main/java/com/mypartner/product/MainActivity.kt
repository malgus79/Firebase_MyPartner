package com.mypartner.product

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mypartner.add.AddDialogFragment
import com.mypartner.Constants
import com.mypartner.entities.Product
import com.mypartner.R
import com.mypartner.databinding.ActivityMainBinding
import com.mypartner.order.OrderActivity
import com.mypartner.promo.PromoFragment

class MainActivity : AppCompatActivity(), OnProductListener, MainAux {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var adapter: ProductAdapter

    private lateinit var firestoreListener: ListenerRegistration

    private var productSelected: Product? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    //esperar el resultado
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //aca se procesa la respuesta
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK) {
                //usuario autenticado
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()

                    //analytics
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                        param(FirebaseAnalytics.Param.SUCCESS, 100)//100 = login successfully
                        param(FirebaseAnalytics.Param.METHOD, "login")
                    }
                }
            } else {
                //se pulso atras o canceló
                if (response == null) {
                    Toast.makeText(this, "Hasta pronto", Toast.LENGTH_SHORT).show()

                    //analytics
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                        param(FirebaseAnalytics.Param.SUCCESS, 200)//200 = cancel
                        param(FirebaseAnalytics.Param.METHOD, "login")
                    }
                    finish()
                } else { //otro tipo de error
                    response.error?.let {
                        if (it.errorCode == ErrorCodes.NO_NETWORK) {
                            Toast.makeText(this, "Sin internet", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Código de error: ${it.errorCode}",
                                Toast.LENGTH_SHORT).show()
                        }

                        //analytics
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                            param(FirebaseAnalytics.Param.SUCCESS, it.errorCode.toLong())
                            param(FirebaseAnalytics.Param.METHOD, "login")
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()
        configRecyclerView()
        //configFirestore()
        //configFirestoreRealTime()
        configButtons()
        configAnalytics()

    }

    private fun configAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            //usuario autenticado
            if (auth.currentUser != null) {
                supportActionBar?.title = auth.currentUser?.displayName
                binding.llProgress.visibility = View.GONE
                binding.nsvProducts.visibility = View.VISIBLE
                binding.efab.show()
            } else {
                //habilitar todos los proveedores de auth
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build())

                resultLauncher.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false) //desactivar la muestra de las opciones de uauario
                    .build())
            }
        }
    }

    //adherir o remover los listener
    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        configFirestoreRealTime()  //se ejecuta unicamente en onResume
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        firestoreListener.remove()  //quitar el listener cada vez que se pause la app
    }

    //config recyclerView
    private fun configRecyclerView(){
        adapter = ProductAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3,
                GridLayoutManager.HORIZONTAL, false)
            adapter = this@MainActivity.adapter
        }

//        (1..20).forEach {
//            val product = Product(it.toString(), "Producto $it", "Este producto es el $it",
//                "", it, it * 1.1)
//            adapter.add(product)
//        }
    }


    //crear el menu: cerrar sesion
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //config al hacer click en cerrar sesion
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT).show()

                        //analytics
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                            param(FirebaseAnalytics.Param.SUCCESS, 100)//100 = sign out successfully
                            param(FirebaseAnalytics.Param.METHOD, "sign_out")
                        }
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            binding.nsvProducts.visibility = View.GONE
                            binding.llProgress.visibility = View.VISIBLE
                            binding.efab.hide()
                        } else {
                            Toast.makeText(this, "No se pudo cerrar la sesión", Toast.LENGTH_SHORT).show()

                            //analytics
                            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                                param(FirebaseAnalytics.Param.SUCCESS, 201)//201 = error sign out
                                param(FirebaseAnalytics.Param.METHOD, "sign_out")
                            }
                        }
                    }
            }
            R.id.action_order_history -> startActivity(Intent(this, OrderActivity::class.java))

            R.id.action_promo -> {
                PromoFragment().show(supportFragmentManager, PromoFragment::class.java.simpleName)
            }
        }
        return super.onOptionsItemSelected(item)
    }
/*
    //config Firestore
    private fun configFirestore() {
        val db = FirebaseFirestore.getInstance()

        //ruta
        db.collection(Constants.COLL_PRODUCTS)
            .get()
            .addOnSuccessListener { snapshots ->
                for (document in snapshots){
                    val product = document.toObject(Product::class.java)
                    product.id = document.id
                    adapter.add(product)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar datos.", Toast.LENGTH_SHORT).show()
            }
    }
*/

    //config Firestore en tiempo real
    private fun configFirestoreRealTime() {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(Constants.COLL_PRODUCTS)

        //listener: captar los cambios
        firestoreListener = productRef.addSnapshotListener { snapshots, error ->
            if (error != null){
                Toast.makeText(this, "Error al consultar datos.", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            //en caso de que no exista error
            for (snapshot in snapshots!!.documentChanges){
                val product = snapshot.document.toObject(Product::class.java)
                product.id = snapshot.document.id
                when(snapshot.type){
                    DocumentChange.Type.ADDED -> adapter.add(product)
                    DocumentChange.Type.MODIFIED -> adapter.update(product)
                    DocumentChange.Type.REMOVED -> adapter.delete(product)
                }
            }
        }
    }

    private fun configButtons(){
        binding.efab.setOnClickListener {
            productSelected = null  //darle un valor antes de inicializar el fragment
            AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
        }
    }

    //config analytics
    private fun configAnalytics(){
        firebaseAnalytics = Firebase.analytics
    }

    //al hacer click en le producto
    override fun onClick(product: Product) {
        productSelected = product
        AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
    }

    //al hacer click largo
    override fun onLongClick(product: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.product_dialog_delete_title)
            .setMessage(R.string.product_dialog_delete_msg)
            .setPositiveButton(R.string.product_dialog_delete_confirm){_,_->

                val db = FirebaseFirestore.getInstance()
                val productRef = db.collection(Constants.COLL_PRODUCTS)

                product.id?.let { id ->
                    product.imgUrl?.let { url ->

                        //extraer la referencia en base a la url
                        val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                        //FirebaseStorage.getInstance().reference.child(Constants.PATH_PRODUCT_IMAGES).child(id)
                        photoRef
                            .delete()
                            .addOnSuccessListener {
                                productRef.document(id)
                                    .delete()
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error al eliminar registro.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al eliminar foto.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

/*
    override fun onLongClick(product: Product) {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(Constants.COLL_PRODUCTS)

        //eliminar segun el id
        product.id?.let { id ->
            productRef.document(id)
                .delete()
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar.", Toast.LENGTH_SHORT).show()
                }
        }
    }
*/

    //devuelve la var global
    override fun getProductSelected(): Product? = productSelected
}