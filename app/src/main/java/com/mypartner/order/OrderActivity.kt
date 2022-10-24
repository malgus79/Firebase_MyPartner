package com.mypartner.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.mypartner.Constants
import com.mypartner.R
import com.mypartner.databinding.ActivityOrderBinding
import com.mypartner.entities.Order

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux {

    private lateinit var binding: ActivityOrderBinding

    private lateinit var adapter: OrderAdaper

    private lateinit var orderSelected: Order

//    private lateinit var firebaseAnalytics: FirebaseAnalytics

//    private val aValues: Array<String> by lazy {
//        resources.getStringArray(R.array.status_value)
//    }
//
//    private val aKeys: Array<Int> by lazy {
//        resources.getIntArray(R.array.status_key).toTypedArray()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFirestore()
//        configAnalytics()
    }

    private fun setupRecyclerView() {
        adapter = OrderAdaper(mutableListOf(), this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = this@OrderActivity.adapter
        }
    }

    private fun setupFirestore(){
        val db = FirebaseFirestore.getInstance()

        db.collection(Constants.COLL_REQUESTS)
//            .orderBy(Constants.PROP_DATE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                for (document in it){
                    val order = document.toObject(Order::class.java)
                    order.id = document.id
                    adapter.add(order)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar los datos.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

//    private fun configAnalytics(){
//        firebaseAnalytics = Firebase.analytics
//    }
//
//    private fun notifyClient(order: Order){
//        val db = FirebaseFirestore.getInstance()
//
//        db.collection(Constants.COLL_USERS)
//            .document(order.clientId)
//            .collection(Constants.COLL_TOKENS)
//            .get()
//            .addOnSuccessListener {
//                var tokensStr = ""
//                for (document in it){
//                    val tokenMap = document.data
//                    tokensStr += "${tokenMap.getValue(Constants.PROP_TOKEN)},"
//                }
//                if (tokensStr.length > 0) {
//                    tokensStr = tokensStr.dropLast(1)
//
//                    var names = ""
//                    order.products.forEach {
//                        names += "${it.value.name}, "
//                    }
//                    names = names.dropLast(2)
//
//                    val index = aKeys.indexOf(order.status)
//
//                    val notificationRS = NotificationRS()
//                    notificationRS.sendNotification(
//                        "Tu pedido ha sido ${aValues[index]}",
//                        names, tokensStr
//                    )
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error al consultar los datos.", Toast.LENGTH_SHORT)
//                    .show()
//            }
//    }

    override fun onStartChat(order: Order) {
//        orderSelected = order
//
//        val fragment = ChatFragment()
//
//        supportFragmentManager
//            .beginTransaction()
//            .add(R.id.containerMain, fragment)
//            .addToBackStack(null)
//            .commit()
    }

    override fun onStatusChange(order: Order) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection(Constants.COLL_REQUESTS)
//            .document(order.id)
//            .update(Constants.PROP_STATUS, order.status)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Orden actualizada.", Toast.LENGTH_SHORT).show()
//                notifyClient(order)
//                //Analytics
//                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_SHIPPING_INFO){
//                    val products = mutableListOf<Bundle>()
//                    order.products.forEach {
//                        val bundle = Bundle()
//                        bundle.putString("id_product", it.key)
//                        products.add(bundle)
//                    }
//                    param(FirebaseAnalytics.Param.SHIPPING, products.toTypedArray())
//                    param(FirebaseAnalytics.Param.PRICE, order.totalPrice)
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error al actualizar orden.", Toast.LENGTH_SHORT).show()
//            }
    }

    override fun getOrderSelected(): Order = orderSelected
}