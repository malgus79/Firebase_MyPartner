package com.mypartner.order

import com.mypartner.entities.Order

interface OnOrderListener {
    fun onStartChat(order: Order)
    //cambiarle el estado al pedido en cuestion
    fun onStatusChange(order: Order)
}