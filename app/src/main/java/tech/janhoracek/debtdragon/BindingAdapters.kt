package tech.janhoracek.debtdragon

import android.graphics.drawable.Drawable
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
        if (!url.isNullOrEmpty()) {
            Glide.with(image.context).load(url).into(image)
        } else {
            image.setImageDrawable(placeHolder)
        }
    }


}