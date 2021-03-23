package tech.janhoracek.debtdragon.friends.models

import com.google.firebase.Timestamp

data class DebtModel(
    val id: String = "",
    val value: Int = 0,
    val name: String = "",
    val description: String = "",
    val img: String = "",
    val payer: String = "",
    val category: String = "",
    val timestamp: Timestamp
)