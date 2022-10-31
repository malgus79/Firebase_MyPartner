package com.mypartner.add

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mypartner.Constants
import com.mypartner.entities.EventPost
import com.mypartner.entities.Product
import com.mypartner.databinding.FragmentDialogAddBinding
import com.mypartner.product.MainAux
import java.io.ByteArrayOutputStream

class AddDialogFragment : DialogFragment(), DialogInterface.OnShowListener {

    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null

    private var product: Product? = null

    private var photoSelectedUri: Uri? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            photoSelectedUri = it.data?.data

//            binding?.imgProductPreview?.setImageURI(photoSelectedUri)  //modo de cargar imagen en el imageView (harcodeado, sin glide)
            binding?.let {
                Glide.with(this)
                    .load(photoSelectedUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imgProductPreview)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            binding = FragmentDialogAddBinding.inflate(LayoutInflater.from(context))

            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle("Agregar producto")
                    .setPositiveButton("Agregar", null)
                    .setNegativeButton("Cancelar", null)
                    .setView(it.root)

                val dialog = builder.create()
                dialog.setOnShowListener(this)

                return dialog
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    //mostrar el producto
    override fun onShow(dialogInterface: DialogInterface?) {
        initProduct()
        configButtons()

        val dialog = dialog as? AlertDialog
        dialog?.let {
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            product?.let { positiveButton?.setText("Actualizar") }

            positiveButton?.setOnClickListener {
                binding?.let {
                    enableUI(false)

                    //uploadImage(product?.id) { eventPost ->
                    uploadReducedImage(product?.id, product?.imgUrl) { eventPost ->
                        if (eventPost.isSuccess) {
                            if (product == null) {  //entonces se crea el producto
                                val product = Product(
                                    name = it.etName.text.toString().trim(),
                                    description = it.etDescription.text.toString().trim(),
                                    imgUrl = eventPost.photoUrl,
                                    quantity = it.etQuantity.text.toString().toInt(),
                                    price = it.etPrice.text.toString().toDouble(),
                                    sellerId = eventPost.sellerId
                                )

                                save(product, eventPost.documentId!!)

                            } else {
                                //retomar el producto y configurarle los nuevos valores
                                product?.apply {
                                    name = it.etName.text.toString().trim()
                                    description = it.etDescription.text.toString().trim()
                                    imgUrl = eventPost.photoUrl
                                    quantity = it.etQuantity.text.toString().toInt()
                                    price = it.etPrice.text.toString().toDouble()

                                    update(this)

                                }
                            }
                        }
                    }
                }
            }

            negativeButton?.setOnClickListener {
                dismiss()
            }
        }
    }

    //inicializar la var
    private fun initProduct() {
        //cast
        product = (activity as? MainAux)?.getProductSelected()

        //rellenar le formmulario
        product?.let { product ->
            binding?.let {
                dialog?.setTitle("Actualizar producto")

                it.etName.setText(product.name)
                it.etDescription.setText(product.description)
                it.etQuantity.setText(product.quantity.toString())
                it.etPrice.setText(product.price.toString())

                //carga la imagen en el item
                Glide.with(this)
                    .load(product.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imgProductPreview)
            }
        }
    }

    //config btn de la imagen del producto
    private fun configButtons(){
        binding?.let {
            it.ibProduct.setOnClickListener {
                openGallery()
            }
        }
    }

    //abrir la galeria
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }


    //subir image URI
    private fun uploadImage(productId: String?, callback: (EventPost) -> Unit){
        //instanciar EventPost
        val eventPost  = EventPost()
        //ruta donde se guardara en storage la imagen
        //?: -> en caso de null toma el id del nuevo document (document.Id), sin no con el "id del producto actual" (cuando se actualiza)
        eventPost.documentId = productId ?: FirebaseFirestore.getInstance()
            .collection(Constants.COLL_PRODUCTS).document().id

//        eventPost.documentId = FirebaseFirestore.getInstance()
//            .collection(Constants.COLL_PRODUCTS).document().id
        val storageRef = FirebaseStorage.getInstance().reference.child(Constants.PATH_PRODUCT_IMAGES)

        photoSelectedUri?.let { uri ->
            binding?.let { binding ->
                binding.progressBar.visibility = View.VISIBLE

                val photoRef = storageRef.child(eventPost.documentId!!)

                photoRef.putFile(uri)
                    .addOnProgressListener {
                        val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                        it.run {
                            binding.progressBar.progress = progress
                            binding.tvProgress.text = String.format("%s%%", progress)
                        }
                    }
                    .addOnSuccessListener {
                        //extraer la url para descargar en storage
                        it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                            Log.i("URL", downloadUrl.toString())
                            //insertar imagen en firestore
                            eventPost.isSuccess = true
                            eventPost.photoUrl = downloadUrl.toString()
                            callback(eventPost)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, "Error al subir imagen.", Toast.LENGTH_SHORT).show()
                        enableUI(true)

                        eventPost.isSuccess = false
                        callback(eventPost)
                    }
            }
        }
    }

    //subir image BITMAP
    private fun uploadReducedImage(productId: String?, imageUrl: String?, callback: (EventPost)->Unit){
        val eventPost  = EventPost()
        imageUrl?.let { eventPost.photoUrl = it }  //darle valor a la photoUrl (es " " por defecto)
        eventPost.documentId = productId ?: FirebaseFirestore.getInstance()
            .collection(Constants.COLL_PRODUCTS).document().id

        //identificar el id del usuario, asi se podra guardar las imagenes por usuario
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val imagesRef = FirebaseStorage.getInstance().reference.child(user.uid)
                .child(Constants.PATH_PRODUCT_IMAGES)
            val photoRef = imagesRef.child(eventPost.documentId!!).child("image0")

            eventPost.sellerId = user.uid

            //photoSelectedUri?.let { uri ->
            if (photoSelectedUri == null) {
                eventPost.isSuccess = true
                callback(eventPost)
            } else {
                binding?.let { binding ->
                    getBitmapFromUri(photoSelectedUri!!)?.let { bitmap ->
                        binding.progressBar.visibility = View.VISIBLE

                        //para configurar bitmap (formato y calidad)
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)

                        photoRef.putBytes(baos.toByteArray())
                            .addOnProgressListener {
                                val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                                it.run {
                                    binding.progressBar.progress = progress
                                    binding.tvProgress.text = String.format("%s%%", progress)
                                }
                            }
                            .addOnSuccessListener {
                                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                    Log.i("URL", downloadUrl.toString())
                                    eventPost.isSuccess = true
                                    eventPost.photoUrl = downloadUrl.toString()
                                    callback(eventPost)
                                }
                            }
                            .addOnFailureListener{
                                Toast.makeText(activity, "Error al subir imagen.", Toast.LENGTH_SHORT).show()
                                enableUI(true)

                                eventPost.isSuccess = false
                                callback(eventPost)
                            }
                    }
                }
            }
        }
    }

    //obtener imagen bitmap desde una uri
    private fun getBitmapFromUri(uri: Uri): Bitmap?{
        activity?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(it.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(it.contentResolver, uri)
            }
            return getResizedImage(bitmap, 320)
        }
        return null
    }

    //cambiar dimensiones de las imagenes
    private fun getResizedImage(image: Bitmap, maxSize: Int): Bitmap{
        var width = image.width
        var height = image.height
        //ancho y alto es menor al maximo de la imagen -> no se hace nada
        if (width <= maxSize && height <= maxSize) return image

        //imagen tiene una dimension mas grande que el tamaño maximo (alto o ancho)
        // objetivo: mantener el ratio de ese bitmap
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1){
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height / bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    //insertar producto
    private fun save(product: Product, documentId: String){
        //guardar imagen con vinculacion entre storage y firestore (documentId)
        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.COLL_PRODUCTS)
            .document(documentId)
            .set(product)
            //.add(product)
            .addOnSuccessListener {
                Toast.makeText(activity, "Producto añadido.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error al insertar.", Toast.LENGTH_SHORT).show()
                enableUI(true)
            }
            .addOnCompleteListener {
                binding?.progressBar?.visibility = View.INVISIBLE
            }
    }

    //actualizar producto
    private fun update(product: Product){
        val db = FirebaseFirestore.getInstance()

        product.id?.let { id ->
            db.collection(Constants.COLL_PRODUCTS)
                .document(id)
                .set(product)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Producto actualizado.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al actualizar.", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                    binding?.progressBar?.visibility = View.INVISIBLE
                    dismiss()
                }
        }
    }

    //bloquear temporalmente el addFragment para no duplicar ni volver a clickear hasta que se carge todoo ok
    private fun enableUI(enable: Boolean){
        positiveButton?.isEnabled = enable
        negativeButton?.isEnabled = enable
        binding?.let {
            with(it){
                etName.isEnabled = enable
                etDescription.isEnabled = enable
                etQuantity.isEnabled = enable
                etPrice.isEnabled = enable
                progressBar.visibility = if(enable) View.INVISIBLE else View.VISIBLE
                tvProgress.visibility = if(enable) View.INVISIBLE else View.VISIBLE
            }
        }
    }

    //desvincular binding
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}