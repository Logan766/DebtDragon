package tech.janhoracek.debtdragon.friends.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class DebtModel(
    var id: String = "",
    var value: Int = 0,
    var name: String = "",
    var description: String = "",
    var img: String = "",
    var payer: String = "",
    var category: String = "",
    var timestamp: Timestamp = Timestamp.now()
) : Serializable