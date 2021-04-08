package tech.janhoracek.debtdragon.groups.models

data class PaymentModel (
    var id: String = "",
    var debtor: String = "",
    var value: Int = 0,
    var creditor: String = "",
    var isResolved: Boolean = false
)