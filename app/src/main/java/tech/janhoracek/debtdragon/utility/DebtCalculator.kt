package tech.janhoracek.debtdragon.utility

import android.util.Log
import tech.janhoracek.debtdragon.groups.models.PaymentModel
import kotlin.math.abs

class DebtCalculator {

    fun calculatePayments(membersNetDebts: HashMap<String, Int>) {
        val testUsers = HashMap<String, Int>()
        testUsers["Pepa"] = 10
        testUsers["Honza"] = -5
        testUsers["Jana"] = -9
        testUsers["Anna"] = 13
        testUsers["Tomas"] = -8
        testUsers["Martin"] = -1

        //////////////////////////////////////////
        val resultPayments: MutableList<PaymentModel> = mutableListOf()

        val usersList = testUsers.toList()
        for (user in usersList) {
            Log.d("WTF", "User: " + user.first + " Value: " + user.second)
        }
        Log.d("WTF", "///////////////////////////////////")

        var n=2
        while (n < testUsers.size) {
            val nevimUsers = testUsers.toList()
            val memberCombination = CombinationGenerator(usersList, n)
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
                        testUsers.remove(member.first)
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
        for (left in testUsers) {
            Log.d("WTF", "Zbyl nam: " + left)
        }
        val result = calculateSimple(testUsers.toList())
        resultPayments.addAll(result)

        for (res in resultPayments) {
            Log.d("WTF", "Vysledek: " + res.debtor + " -> " + res.value + " -> " + res.creditor )
        }
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


    fun calculatePaymentsTest(membersNetDebts: HashMap<String, Int>) {
        val list = membersNetDebts.toList()
        var list2 = listOf(1, 2, 3, 4, 5, 6)
        val combinaton = CombinationGenerator(list2, 2)

        for(nevim in combinaton) {
            Log.d("WTF", "Iterace " + nevim)
            Log.d("WTF", "Iterace prvni element " + nevim[0])
            Log.d("WTF", "Iterace druhej element " + nevim[1])
            nevim.forEach {
                Log.d("WTF", it.toString())
            }
        }

        /////////////////////////////
        /*val test = list2.combinations(2)
        for(nevim in test) {
            for (i in nevim.indices) {
                Log.d("WTF", "$i Cislo + " + nevim[i])
            }
        }*/
        ///////////////////////////////////////
    }

}