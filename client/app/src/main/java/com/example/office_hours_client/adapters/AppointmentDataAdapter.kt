package com.example.office_hours_client.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.office_hours_client.R
import com.example.office_hours_client.extensions.formatTimeRange
import com.example.office_hours_client.models.AppointmentData
import kotlinx.datetime.toJavaLocalDateTime
import java.time.ZoneOffset

class  AppointmentDataAdapter(
    override val context: Context,
    override val list: List<AppointmentData>
) : DataAdapter<AppointmentData>() {

    override fun getItemId(p0: Int): Long {
        return list[p0].startDateTime.toJavaLocalDateTime().toEpochSecond(ZoneOffset.MIN)
    }

    override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View {
        val data = list[p0]
        val startTime = data.startDateTime.toJavaLocalDateTime().toLocalTime()
        val endTime =  data.endDateTime.toJavaLocalDateTime().toLocalTime()

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(context)
            val row = layoutInflater.inflate(R.layout.row_appointment, convertView)

            val holder = ConcreteSlotDataHolder(
                timeText = row.findViewById<TextView>(R.id.appointment_row_time),
                dayText = row.findViewById<TextView>(R.id.appointment_row_day),
            )
            row.tag = holder

            data.apply {
                holder.timeText.text =
                    formatTimeRange(startTime, endTime, hour12 = false)
                holder.dayText.text = "${data.startDateTime.date.dayOfWeek}"
            }
            return row
        } else {
            val row = convertView
            val holder = convertView.tag as ConcreteSlotDataHolder
            data.apply {
                holder.timeText.text =
                    formatTimeRange(startTime, endTime, hour12 = false)
                holder.dayText.text = "${data.startDateTime.date.dayOfWeek}"
            }
            return row
        }
    }

    class ConcreteSlotDataHolder(
        val timeText: TextView,
        val dayText: TextView
    )
}