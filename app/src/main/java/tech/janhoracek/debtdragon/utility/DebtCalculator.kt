package tech.janhoracek.debtdragon.utility

import android.util.Log
import tech.janhoracek.debtdragon.groups.models.PaymentModel
import kotlin.math.abs

class DebtCalculator {

    fun calculatePayments(membersNetDebts: HashMap<String, Int>): MutableList<PaymentModel> {
        /*val inputMemebers = HashMap<String, Int>()
        inputMemebers["Pepa"] = 10
        inputMemebers["Honza"] = -5
        inputMemebers["Jana"] = -9
        inputMemebers["Anna"] = 13
        inputMemebers["Tomas"] = -8
        inputMemebers["Martin"] = -1
        inputMemebers["Hugo"] = 17
        inputMemebers["Paja"] = -3
        inputMemebers["Vera"] = 6
        inputMemebers["Xenie"] = -20*/

        val inputMemebers = membersNetDebts

        //////////////////////////////////////////
        val resultPayments: MutableList<PaymentModel> = mutableListOf()

        val usersList = inputMemebers.toList()
        for (user in usersList) {
            Log.d("WTF", "User: " + user.first + " Value: " + user.second)
        }
        Log.d("WTF", "///////////////////////////////////")

        var n=2
        while (n < inputMemebers.size - 1) {
            val nevimUsers = inputMemebers.toList()
            val memberCombination = CombinationGenerator(nevimUsers, n)
            var pairFound = false
            while (memberCombination.hasNext()) {
                var sum = 0
                val combination = memberCombination.next()
                for(member in combination) {
                    sum += member.second
                }
                if(sum == 0) {
                    //vyres to
                    val result = calculateSimple(combination)
                    resultPayments.addAll(result)
                    for(member in combination) {
                        inputMemebers.remove(member.first)
                    }
                    pairFound = true
                    Log.d("WTF", "////////////////////////////")
                }
                if (pairFound) {
                    break
                }
            }
            if (!pairFound) {
                n++
            }
        }
        Log.d("WTF", "Zbytek")
        for (left in inputMemebers) {
            Log.d("WTF", "Zbyl nam: " + left)
        }
        val result = calculateSimple(inputMemebers.toList())
        resultPayments.addAll(result)

        for (res in resultPayments) {
            Log.d("WTF", "Vysledek: " + res.debtor + " -> " + res.value + " -> " + res.creditor )
        }

        return resultPayments
    }

    private fun calculateSimple(combination: List<Pair<String, Int>>): MutableList<PaymentModel> {
        Log.d("WTF", "Pocital bych: " + combination)
        val resultPayments: MutableList<PaymentModel> = mutableListOf()
        var sortedMembers = sortListPair(combination).toMutableList()
        while(sortedMembers.size > 1) {
            val debtor = sortedMembers.first().first
            val creditor = sortedMembers.last().first
            var value = 0
            if (abs(sortedMembers.last().second) > sortedMembers.first().second) {
                value = sortedMembers.first().second

                val valueLeft = sortedMembers.last().second + sortedMembers.first().second
                val editedPair = Pair(sortedMembers.last().first, valueLeft)
                sortedMembers.remove(sortedMembers.first())
                sortedMembers.remove(sortedMembers.last())
                sortedMembers.add(editedPair)
            } else {
                value = abs(sortedMembers.last().second)

                val valueLeft = sortedMembers.last().second + sortedMembers.first().second
                val editedPair = Pair(sortedMembers.first().first, valueLeft)
                sortedMembers.remove(sortedMembers.last())
                sortedMembers.remove(sortedMembers.first())
                sortedMembers.add(editedPair)
            }
            val payment = PaymentModel("", debtor, value, creditor)
            Log.d("WTF", "Vytvarim payment: " + debtor + " -> " + value + " -> " + creditor)
            resultPayments.add(payment)
            sortedMembers = sortListPair(sortedMembers).toMutableList()
        }
        return resultPayments
    }

    fun sortListPair(unsortedList: List<Pair<String, Int>>) : List<Pair<String, Int>> {
        val result = unsortedList.sortedWith(compareBy { it.second }).asReversed()
        return result
    }


}