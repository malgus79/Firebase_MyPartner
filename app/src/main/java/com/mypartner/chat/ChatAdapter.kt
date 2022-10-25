package com.mypartner.chat

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mypartner.R
import com.mypartner.databinding.ItemChatBinding
import com.mypartner.entities.Message

class ChatAdapter(private val messageList: MutableList<Message>, private val listener: OnChatListener)
    : RecyclerView.Adapter<ChatAdapter.ViewHolder>(){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]

        holder.setListener(message)

        //var para manejar segun quién envia el chat
        var gravity = Gravity.END
        var background = ContextCompat.getDrawable(context, R.drawable.background_chat_support)
        var textColor = ContextCompat.getColor(context, R.color.colorOnPrimary)

        val marginHorizontal = context.resources.getDimensionPixelSize(R.dimen.chat_margin_horizontal)
        //var para manipular los margenes
        val params = holder.binding.tvMessage.layoutParams as ViewGroup.MarginLayoutParams
        params.marginStart = marginHorizontal
        params.marginEnd = 0
        params.topMargin = 0

        //margen entre globitos del chat
        if (position > 0 && message.isSentByMe() != messageList[position - 1].isSentByMe()){
            params.topMargin = context.resources.getDimensionPixelSize(R.dimen.common_padding_min)
        }

        //si no lo envio yo (cliente) -> modid las var
        if (!message.isSentByMe()){
            gravity = Gravity.START
            background = ContextCompat.getDrawable(context, R.drawable.background_chat_client)
            textColor = ContextCompat.getColor(context, R.color.colorOnSecondary)
            params.marginStart = 0
            params.marginEnd = marginHorizontal
        }

        //para modificar la gavity
        holder.binding.root.gravity = gravity

        holder.binding.tvMessage.layoutParams = params
        holder.binding.tvMessage.setBackground(background)
        holder.binding.tvMessage.setTextColor(textColor)
        holder.binding.tvMessage.text = message.message
    }

    override fun getItemCount(): Int = messageList.size

    //agregar mensaje
    fun add(message: Message){
        //si no lo contiene al mensaje -> lo agrega
        if (!messageList.contains(message)){
            messageList.add(message)
            notifyItemInserted(messageList.size - 1)
        }
    }

    //actualizar mensaje
    fun update(message: Message){
        val index = messageList.indexOf(message)
        //si fue encontrado el mensaje -> actualizar
        if (index != -1){
            messageList.set(index, message)
            notifyItemChanged(index)
        }
    }

    //eliminar mensaje
    fun delete(message: Message){
        val index = messageList.indexOf(message)
        //si fue encontrado el mensaje -> eliminar
        if (index != -1){
            messageList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemChatBinding.bind(view)

        fun setListener(message: Message){
            binding.tvMessage.setOnLongClickListener {
                listener.deleteMessage(message)
                true
            }
        }
    }
}