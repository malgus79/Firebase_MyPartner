package com.mypartner.chat

import com.mypartner.entities.Message

interface OnChatListener {
    fun deleteMessage(message: Message)
}