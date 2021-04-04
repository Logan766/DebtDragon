package tech.janhoracek.debtdragon.groups.models

import com.google.firebase.Timestamp

data class GroupDebtModel (
    var id: String = "",
    var payer: String = "",
    var debtor: String = "",
    var name: String = "",
    var value: String = "",
    var timestamp: Timestamp = Timestamp.now()
        )