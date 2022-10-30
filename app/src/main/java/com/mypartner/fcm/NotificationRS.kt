package com.mypartner.fcm

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.mypartner.Constants
import com.mypartner.MyPartnerApplication
import org.json.JSONException
import org.json.JSONObject

class NotificationRS { //RS -> referencia a Remote Service
    fun sendNotification(title: String, message: String, tokens: String){
        //crear un objeto que pueda llevar los parametros declarados
        val params = JSONObject()
        //clave / valor
        params.put(Constants.PARAM_METHOD, Constants.SEND_NOTIFICATION)
        params.put(Constants.PARAM_TITLE, title)
        params.put(Constants.PARAM_MESSAGE, message)
        params.put(Constants.PARAM_TOKENS, tokens)
        params.put(Constants.PARAM_TOPIC, "")
        params.put(Constants.PARAM_IMAGE, "")

        //enviar esa solicitud
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
            Constants.MY_PARTNER_RS, params, Response.Listener { response ->
                //aqui se comienza a procesar la respuesta
                try {
                    val success = response.getInt(Constants.PARAM_SUCCESS)
                    Log.i("Volley success", success.toString())
                    Log.i("Response", response.toString())
                } catch (e: JSONException){
                    e.printStackTrace()
                    Log.e("Volley exception", e.localizedMessage)
                }
        },
            //aqui se procesan los errores
            Response.ErrorListener { error ->
                if (error.localizedMessage != null){
                    Log.e("Volley error", error.localizedMessage)
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val paramsHeaders = HashMap<String, String>()
                paramsHeaders["Content-Type"] = "application/json; charset=utf-8"  //configurado el Json
                return super.getHeaders()
            }
        }
        //ahora si se puede enviar la peticion
        MyPartnerApplication.volleyHelper.addToRequestQueue(jsonObjectRequest)
    }

    fun sendNotificationByTopic(title: String, message: String, topic: String, photoUrl: String,
                                callback: (Boolean)->Unit){
        val params = JSONObject()
        params.put(Constants.PARAM_METHOD, Constants.SEND_NOTIFICATION_BY_TOPIC)
        params.put(Constants.PARAM_TITLE, title)
        params.put(Constants.PARAM_MESSAGE, message)
        params.put(Constants.PARAM_TOKENS, "")
        params.put(Constants.PARAM_TOPIC, topic)
        params.put(Constants.PARAM_IMAGE, photoUrl)

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
            Constants.MY_PARTNER_RS, params, Response.Listener { response ->
                try {
                    val success = response.getInt(Constants.PARAM_SUCCESS)
                    if (success == 3){
                        callback(Constants.SUCCESS)
                    } else {
                        callback(Constants.ERROR)
                    }
                } catch (e: JSONException){
                    e.printStackTrace()
                    callback(Constants.ERROR)
                }
        }, Response.ErrorListener { error ->
                if (error.localizedMessage != null){
                    callback(Constants.ERROR)
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val paramsHeaders = HashMap<String, String>()
                paramsHeaders["Content-Type"] = "application/json; charset=utf-8"
                return super.getHeaders()
            }
        }
        MyPartnerApplication.volleyHelper.addToRequestQueue(jsonObjectRequest)
    }
}