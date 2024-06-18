import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class FirebaseStorageHelper {

    // Upload image to Firestore
    fun uploadImage(uri: Uri, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val imageReference = FirebaseStorage.getInstance()
            .reference
            .child("images/${uri.lastPathSegment}")

        imageReference.putFile(uri)
            .addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess.invoke(downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                onFailure.invoke()
            }
    }


}
