package com.mypartner.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

        //mostrar los estados
        val index = aKeys.indexOf(order.status)
        val statusAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, aValues)
        //configurar el componente del xml (actvStatus)
        holder.binding.actvStatus.setAdapter(statusAdapter)
        //si encontro el estaod dentro del array
        if (index != -1){
            holder.binding.actvStatus.setText(aValues[index], false)
        } else {
            //si no esta en los estados conocidos
            holder.binding.actvStatus.setText(context.getText(R.string.order_status_unknown), false)
            //false = para que no pueda ser editable el actv y se comporte como un espinner
        }
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