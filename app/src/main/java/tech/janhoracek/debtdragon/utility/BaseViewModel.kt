package tech.janhoracek.debtdragon.utility

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Base view model
 *
 * @constructor Create empty Base view model
 */
abstract class BaseViewModel : ViewModel() {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()



}