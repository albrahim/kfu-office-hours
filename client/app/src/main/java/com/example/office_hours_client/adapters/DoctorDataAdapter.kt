package com.example.office_hours_client.adapters

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import com.example.office_hours_client.R
import com.example.office_hours_client.models.UserPublicData

class  DoctorDataAdapter(
    override val context: Context,
    override val list: List<UserPublicData>
) : DataAdapter<UserPublicData>() {

    override fun getItemId(p0: Int): Long {
        return list[p0].id.toLong()
    }

    override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View {
        val doctorData = list[p0]

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(context)
            val row = layoutInflater.inflate(R.layout.row_doctor, convertView)

            val holder = DoctorDataHolder(
                nameText = row.findViewById<TextView>(R.id.row_doctor_name),
            )
            row.tag = holder

            doctorData.apply {
                holder.nameText.text = "Dr. $firstName $lastName"
            }
            return row
        } else {
            val row = convertView
            val holder = convertView.tag as DoctorDataHolder
            doctorData.apply {
                holder.nameText.text = "Dr. $firstName $lastName"
            }
            return row
        }
    }

    class DoctorDataHolder(
        val nameText: TextView,
    )
}