package com.mypartner.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mypartner.R
import com.mypartner.databinding.ItemOrderBinding
import com.mypartner.entities.Order

class OrderAdaper(private val orderList: MutableList<Order>, private val listener: OnOrderListener) :
    RecyclerView.Adapter<OrderAdaper.ViewHolder>() {

    private lateinit var context: Context

    //manejar los estados de la orden
    private val aValues: Array<String> by lazy {
        context.resources.getStringArray(R.array.status_value)
    }

    private val aKeys: Array<Int> by lazy {
        context.resources.getIntArray(R.array.status_key).toTypedArray()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]

        holder.setListener(order)

        holder.binding.tvId.text = context.getString(R.string.order_id, order.id)
        //holder.binding.tvId.text = order.id

        var names = ""
        order.products.forEach{
            names += "${it.value.name}, "
        }
        holder.binding.tvProductNames.text = names.dropLast(2)

        holder.binding.tvTotalPrice.text = context.getString(R.string.order_total_price, order.totalPrice)
        //holder.binding.tvTotalPrice.text = order.totalPrice.toString()

        val index = aKeys.indexOf(order.status)
        val statusStr = if (index != -1) aValues[index] else context.getString(R.string.order_status_unknown)
        holder.binding.tvStatus.text = context.getString(R.string.order_status, statusStr)
    }

    override fun getItemCount(): Int = orderList.size

    //agregar orden
    fun add(order: Order){
        orderList.add(order)
        notifyItemInserted(orderList.size - 1)  //ultima posicion
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemOrderBinding.bind(view)

        //seguir el envio
        fun setListener(order: Order){

            //iniciar el chat
            binding.chpChat.setOnClickListener {
                listener.onStartChat(order)
            }
        }
    }
}