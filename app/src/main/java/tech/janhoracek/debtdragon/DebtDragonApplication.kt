package tech.janhoracek.debtdragon

import android.app.Application
import android.content.Context
import com.squareup.okhttp.internal.Internal.instance

/**
 * Debt dragon application
 *
 * @constructor Create empty Debt dragon application
 */
class DebtDragonApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: DebtDragonApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}

fun localized(resource: Int): String {
    return DebtDragonApplication.applicationContext().getString(resource)
}



