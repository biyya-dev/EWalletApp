package com.example.relational.database.sample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.example.relational.database.sample.R

class MyIconSpinnerAdapter(
    context: Context,
    private val iconList: List<Int>
) : ArrayAdapter<Int>(context, 0, iconList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_icon_item, parent, false)

        // Get the ImageView from the layout and set the resource ID of the icon
        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        iconImageView.setImageResource(iconList[position])

        return view
    }
}
