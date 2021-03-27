package tech.janhoracek.debtdragon.utility

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import tech.janhoracek.debtdragon.R


object BindingAdapters {

    @JvmStatic
    @BindingAdapter("app:errorText")
    fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
        view.error = errorMessage
    }

    @JvmStatic
    @BindingAdapter("app:imageUrl", "app:placeholder")
    fun setImage(image: ImageView, url: String?, placeHolder: Drawable) {
        if (url == "") {
            Glide.with(image.context).load(placeHolder).into(image)
        } else if (url != null) {
            Log.d("OHEN",
                "nastavuji obrazek, protoze url jest: " + url + " a kontrola je " + !url.isNullOrEmpty())
            Glide.with(image.context).load(url).into(image)
        } else {
            Log.d("OHEN", "nastavuji placeholder")
            //image.setImageDrawable(placeHolder)
            Glide.with(image.context).load(placeHolder).into(image)
        }
    }

    @JvmStatic
    @BindingAdapter("app:entries")
    fun setAdapter(view: AutoCompleteTextView, entries: List<String>?) {
        view.setAdapter(ArrayAdapter(view.context, R.layout.list_item, entries!!))
    }



    @JvmStatic
    @BindingAdapter(value = ["onValueChangeListener"])
    fun setOnValueChangeListener(slider: Slider, listener: OnValueChangeListener) {
        slider.addOnChangeListener { _: Slider?, value: Float, _: Boolean ->
            listener.menimeZivoty(value)
        }
    }

    interface OnValueChangeListener {
        fun menimeZivoty(value: Float)
    }





}