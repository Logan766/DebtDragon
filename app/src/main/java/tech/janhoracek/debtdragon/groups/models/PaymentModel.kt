package tech.janhoracek.debtdragon.groups.models

/**
 * Payment model
 *
 * @property id as payment id
 * @property debtor as debtor id
 * @property value as payment value
 * @property creditor as creditor id
 * @property isResolved as solved status
 * @constructor Create empty Payment model
 */
data class PaymentModel (
    var id: String = "",
    var debtor: String = "",
    var value: Int = 0,
    var creditor: String = "",
    var isResolved: Boolean = false
)