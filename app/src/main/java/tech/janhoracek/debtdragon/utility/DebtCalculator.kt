package tech.janhoracek.debtdragon.utility

import android.util.Log
import tech.janhoracek.debtdragon.groups.models.PaymentModel
import kotlin.math.abs

/**
 * Debt calculator
 *
 * @constructor Create empty Debt calculator
 */
class DebtCalculator {

    /**
     * Calculate payments
     *
     * @param membersNetDebts as list of memebers with net of debts
     * @return list as list of payments
     */
    fun calculatePayments(membersNetDebts: HashMap<String, Int>): MutableList<PaymentModel> {
        val inputMemebers = membersNetDebts
        val resultPayments: MutableList<PaymentModel> = mutableListOf()

        // Check number of users, otherwise use simple algorithm
        if(inputMemebers.size <= 15) {
            var n=2
            while (n < inputMemebers.size - 1) {
                val leftUsers = inputMemebers.toList()
                val memberCombination = CombinationGenerator(leftUsers, n)
                var pairFound = false
                while (memberCombination.hasNext()) {
                    var sum = 0
                    val combination = memberCombination.next()
                    // Calculate sum of n-pair
                    for(member in combination) {
                        sum += member.second
                    }
                    if(sum == 0) {
                        // N-pair found, calculate in simple algorithm
                        val result = calculateSimple(combination)
                        // Add results to payments
                        resultPayments.addAll(result)
                        // Remove N-pair from list
                        for(member in combination) {
                            inputMemebers.remove(member.first)
                        }
                        pairFound = true
                    }
                    // Break
                    if (pairFound) {
                        break
                    }
                }
                // Iterate
                if (!pairFound) {
                    n++
                }
            }
        }

        // Calculate rest with simple algorithm or all if members are more than 15
        val result = calculateSimple(inputMemebers.toList())
        resultPayments.addAll(result)

        // Return payments
        return resultPayments
    }

    /**
     * Calculate payments simple algorithm
     *
     * @param combination as n-pair group of members
     * @return list of Payment Models as list of payments calculated
     */
    private fun calculateSimple(combination: List<Pair<String, Int>>): MutableList<PaymentModel> {
        val resultPayments: MutableList<PaymentModel> = mutableListOf()
        // Sort members
        var sortedMembers = sortListPair(combination).toMutableList()
        // Calculate while there are users left
        while(sortedMembers.size > 1) {
            // Get the biggest debtor and biggest creditor
            val debtor = sortedMembers.first().first
            val creditor = sortedMembers.last().first
            var value = 0

            // Compare if biggest debt is bigger than biggest credit and react
            if (abs(sortedMembers.last().second) > sortedMembers.first().second) {
                // Debt is bigger than commitment
                value = sortedMembers.first().second
                // Calculate leftover of debt
                val valueLeft = sortedMembers.last().second + sortedMembers.first().second
                val editedPair = Pair(sortedMembers.last().first, valueLeft)

                // Remove biggest creditor and change value of commitment
                sortedMembers.remove(sortedMembers.first())
                sortedMembers.remove(sortedMembers.last())
                sortedMembers.add(editedPair)
            } else {
                // Commitment is bigger than debt
                value = abs(sortedMembers.last().second)

                // Calculate leftover of commitment
                val valueLeft = sortedMembers.last().second + sortedMembers.first().second
                val editedPair = Pair(sortedMembers.first().first, valueLeft)

                // Remove biggest debtor and change value of debt
                sortedMembers.remove(sortedMembers.last())
                sortedMembers.remove(sortedMembers.first())
                sortedMembers.add(editedPair)
            }
            // Create payment without ID and add it to list
            val payment = PaymentModel("", debtor, value, creditor)
            resultPayments.add(payment)

            // Sort edited list of members
            sortedMembers = sortListPair(sortedMembers).toMutableList()
        }
        // Return list of payments
        return resultPayments
    }

    /**
     * Sort list of pair
     *
     * @param unsortedList as list of unsroted pairs (members and debt net summary)
     * @return sorted list of pairs (members and debt net summary)
     */
    fun sortListPair(unsortedList: List<Pair<String, Int>>) : List<Pair<String, Int>> {
        val result = unsortedList.sortedWith(compareBy { it.second }).asReversed()
        return result
    }


}