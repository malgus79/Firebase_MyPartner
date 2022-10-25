package com.mypartner

import android.app.Application
import com.mypartner.fcm.VolleyHelper

class MyPartnerApplication: Application() {
    companion object{
        lateinit var volleyHelper: VolleyHelper
    }

    override fun onCreate() {
        super.onCreate()

        volleyHelper = VolleyHelper.getInstance(this)
    }
}