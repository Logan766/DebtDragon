package tech.janhoracek.debtdragon.groups.models

import com.google.firebase.Timestamp

data class BillModel (
    var id: String = "",
    var name: String = "",
    var payer: String = "",
    var timestamp: Timestamp = Timestamp.now()
        )