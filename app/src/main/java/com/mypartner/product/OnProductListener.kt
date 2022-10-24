package com.mypartner.product

import com.mypartner.entities.Product

interface OnProductListener {
    fun onClick(product: Product)
    fun onLongClick(product: Product)
}