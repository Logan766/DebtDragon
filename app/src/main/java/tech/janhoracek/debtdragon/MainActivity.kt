package tech.janhoracek.debtdragon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.android.navigationadvancedsample.setupWithNavController
import com.google.android.gms.common.internal.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.viewmodels.AddEditDebtViewModel
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory
import java.util.logging.Handler

private lateinit var mAuth: FirebaseAuth
private lateinit var username: String

class MainActivity : AppCompatActivity() {
    //////////////
    private var currentNavController: LiveData<NavController>? = null
    /////////////////
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)
        ////////////////
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }


        ////////////////

        ////pokus////

        GlobalScope.launch(IO) {
            db.collection(tech.janhoracek.debtdragon.utility.Constants.DATABASE_USERS).document(auth.currentUser.uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.d("PRASE", "Tady to maka")
                    snapshot.toObject(UserObject::class.java)
                    Log.d("PRASE", "Moje jmeno jest: " + UserObject.name)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }


        ////konec - pokus ////////////

        /*val bottomNavigationView = bottomNavigationViewMain //bottomNavigationViewMain
            val navController = findNavController(R.id.fragment)


            bottomNavigationView.setupWithNavController(navController)*/
    }



    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }
    //////////////////


    fun setBottomNavigationVisibility(visibility: Int) {
        bottomNavigationViewMain.visibility = visibility
    }

    /////////////////////
    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationViewMain)

        val navGraphIds = listOf(R.navigation.dashborad, R.navigation.friends, R.navigation.groups, R.navigation.profile)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.fragment,
            intent = intent
        )


        /*
        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })*/
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
    ////////////////


    fun showLoading() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        loadingCover.visibility = View.VISIBLE
    }

    fun hideLoading() {
        loadingCover.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}