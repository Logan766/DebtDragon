package tech.janhoracek.debtdragon.utility

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout


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


}