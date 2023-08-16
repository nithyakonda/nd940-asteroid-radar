package com.udacity.asteroidradar

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.udacity.asteroidradar.main.AsteroidListAdapter
import timber.log.Timber

@BindingAdapter("pictureOfTheDayUrl")
fun bindPictureOfTheDayImage(imageView: ImageView, imageUrl: String?) {
    imageUrl?.let {
        val imgUri = imageUrl.toUri().buildUpon().scheme("https").build()
        Timber.d("Image Uri ::" + imgUri)
        Glide.with(imageView.context)
            .load(imgUri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image))
            .into(imageView)
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Asteroid>?) {
    val adapter = recyclerView.adapter as AsteroidListAdapter
    adapter.submitList(data)
}

@BindingAdapter("statusIcon")
fun bindAsteroidStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.ic_status_potentially_hazardous)
    } else {
        imageView.setImageResource(R.drawable.ic_status_normal)
    }
}

@BindingAdapter("asteroidStatusImage")
fun bindDetailsStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.asteroid_hazardous)
    } else {
        imageView.setImageResource(R.drawable.asteroid_safe)
    }
}

@BindingAdapter("asteroidStatusImageContentDescription")
fun bindAsteroidStatusImageContentDescription(view: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        view.contentDescription = view.context.getString(R.string.potentially_hazardous_asteroid_image)
    } else {
        view.contentDescription = view.context.getString(R.string.not_hazardous_asteroid_image)
    }
}

@BindingAdapter("asteroidStatusContentDescription")
fun bindDetailsStatusContentDescription(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
    } else {
    }
}

@BindingAdapter("codename")
fun bindTextViewToCodeName(textView: TextView, codename: String) {
    textView.text = codename
}

@BindingAdapter("astronomicalUnitText")
fun bindTextViewToAstronomicalUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.astronomical_unit_format), number)
}

@BindingAdapter("kmUnitText")
fun bindTextViewToKmUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_unit_format), number)
}

@BindingAdapter("velocityText")
fun bindTextViewToDisplayVelocity(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_s_unit_format), number)
}
