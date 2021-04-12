package tech.janhoracek.debtdragon.groups.models

import com.google.firebase.Timestamp

/**
 * Bill model
 *
 * @property id as bill ID
 * @property name as bill Name
 * @property payer as payer ID
 * @property timestamp as datetime created
 * @constructor Create empty Bill model
 */
data class BillModel (
    var id: String = "",
    var name: String = "",
    var payer: String = "",
    var timestamp: Timestamp = Timestamp.now()
        )