package tech.janhoracek.debtdragon.dashboard.viewmodels

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory

class DashboradViewModel: BaseViewModel() {


    init {
        GlobalScope.launch(IO) {
            Log.d("ZIPPO", "Spoustim")
            db.collection(Constants.DATABASE_FRIENDSHIPS)
                .whereArrayContains("members", auth.currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if(error != null) {
                        Log.d("ZIPPO", error.message.toString())
                    }

                    if (snapshot != null) {
                        snapshot.forEach {document ->
                            db.collection(Constants.DATABASE_FRIENDSHIPS).document(document.id).collection(Constants.DATABASE_DEBTS).addSnapshotListener {snapshot, error ->
                                snapshot!!.forEach {
                                    Log.d("ZIPPO", "Název položky jest: " + it["name"] + " a value: " + it["value"])
                                }
                            }
                        }
                    } else {
                        Log.d("ZIPPO", "Current data null")
                    }
                }
        }
    }

    private fun setupDataForSummaryPie() {

    }

    private fun retrieveValueAndCategory(document: DocumentSnapshot): Pair<String, Int> {
        val category = transformDatabaseStringToCategory(document[Constants.DATABASE_DEBTS_CATEGORY].toString())
        val value = document[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
        return Pair(category, value)
    }
}