package com.example.homestyler

import com.example.homestyler.model.ImageModel
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    fun uploadImageToFirestore(imageModel: ImageModel, onSuccess: () -> Unit, onFailure: () -> Unit) {
        FirebaseFirestore.getInstance().collection("images")
            .document(imageModel.imageId)
            .set(imageModel)
            .addOnSuccessListener {
                onSuccess.invoke()
            }
            .addOnFailureListener {
                onFailure.invoke()
            }
    }

    //Get user's username from Firestore
    fun getUsername(userId: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    if (username != null) {
                        onSuccess.invoke(username)
                    } else {
                        onFailure.invoke()
                    }
                } else {
                    onFailure.invoke()
                }
            }
            .addOnFailureListener {
                onFailure.invoke()
            }
    }

}