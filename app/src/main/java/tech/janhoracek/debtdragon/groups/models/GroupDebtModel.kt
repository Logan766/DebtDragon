package tech.janhoracek.debtdragon.groups.models

import com.google.firebase.Timestamp

/**
 * Group debt model
 *
 * @property id as group debt id
 * @property payer as payer ID
 * @property debtor as debtor ID
 * @property name as group debt name
 * @property value as group debt value
 * @property timestamp as datetime created
 * @constructor Create empty Group debt model
 */
data class GroupDebtModel (
    var id: String = "",
    var payer: String = "",
    var debtor: String = "",
    var name: String = "",
    var value: String = "",
    var timestamp: Timestamp = Timestamp.now()
        )