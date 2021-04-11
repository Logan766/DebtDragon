package tech.janhoracek.debtdragon.friends.models

import com.google.firebase.Timestamp
import java.io.Serializable

/**
 * Debt model
 *
 * @property id as debt id
 * @property value as debt value
 * @property name as debt name
 * @property description as debt description
 * @property img as debt image URL
 * @property payer as ID of payer
 * @property category as category of debt
 * @property timestamp as date of creation
 * @constructor Create empty Debt model
 */
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