package tech.janhoracek.debtdragon

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout


object BindingAdapters {

    @JvmStatic
    @BindingAdapter("app:errorText")
    fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
        view.error = errorMessage
    }

}