package com.mypartner

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.mypartner.databinding.FragmentDialogAddBinding

class AddDialogFragment : DialogFragment(), DialogInterface.OnShowListener {

    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null

    private var product: Product? = null

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

        val dialog = dialog as? AlertDialog
        dialog?.let {
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                binding?.let {
//                    enableUI(false)
//
//                    if (product == null) {
                        val product = Product(
                            name = it.etName.text.toString().trim(),
                            description = it.etDescription.text.toString().trim(),
                            quantity = it.etQuantity.text.toString().toInt(),
                            price = it.etPrice.text.toString().toDouble()
                        )

                        save(product)
//                    } else {
//                        product?.apply {
//                            name = it.etName.text.toString().trim()
//                            description = it.etDescription.text.toString().trim()
//                            quantity = it.etQuantity.text.toString().toInt()
//                            price = it.etPrice.text.toString().toDouble()
//
//                            update(this)
//                        }
//                    }
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
                it.etName.setText(product.name)
                it.etDescription.setText(product.description)
                it.etQuantity.setText(product.quantity.toString())
                it.etPrice.setText(product.price.toString())
            }
        }
    }

    //insertar producto
    private fun save(product: Product){
        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.COLL_PRODUCTS)
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(activity, "Producto añadido.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error al insertar.", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
//                enableUI(true)
                dismiss()
            }
    }

//    private fun update(product: Product){
//        val db = FirebaseFirestore.getInstance()
//
//        product.id?.let { id ->
//            db.collection(Constants.COLL_PRODUCTS)
//                .document(id)
//                .set(product)
//                .addOnSuccessListener {
//                    Toast.makeText(activity, "Producto actualizado.", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener {
//                    Toast.makeText(activity, "Error al actualizar.", Toast.LENGTH_SHORT).show()
//                }
//                .addOnCompleteListener {
//                    enableUI(true)
//                    dismiss()
//                }
//        }
//    }

//    private fun enableUI(enable: Boolean){
//        positiveButton?.isEnabled = enable
//        negativeButton?.isEnabled = enable
//        binding?.let {
//            with(it){
//                etName.isEnabled = enable
//                etDescription.isEnabled = enable
//                etQuantity.isEnabled = enable
//                etPrice.isEnabled = enable
//            }
//        }
//    }

    //desvincular binding
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}