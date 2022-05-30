package com.example.office_hours_client.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.office_hours_client.R
import com.example.office_hours_client.extensions.formatTimeRange
import com.example.office_hours_client.models.SlotData
import java.time.LocalTime

class  SlotDataAdapter(
    override val context: Context,
    override val list: List<SlotData>
) : DataAdapter<SlotData>() {

    override fun getItemId(p0: Int): Long {
        return list[p0].id.hashCode().toLong()
    }

    override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View {
        val data = list[p0]
        val milliToNano = 1000000L
        val startTime = LocalTime.ofNanoOfDay(data.startNano * milliToNano)
        val endTime = LocalTime.ofNanoOfDay(data.endNano * milliToNano)

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(context)
            val row = layoutInflater.inflate(R.layout.row_slot, convertView)

            val holder = SlotDataHolder(
                timeText = row.findViewById<TextView>(R.id.slot_row_time),
                dayText = row.findViewById<TextView>(R.id.slot_row_day),
            )
            row.tag = holder

            data.apply {
                holder.timeText.text =
                    formatTimeRange(startTime, endTime, hour12 = false)
                holder.dayText.text = "${data.dayOfWeek}"
            }
            return row
        } else {
            val row = convertView
            val holder = convertView.tag as SlotDataHolder
            data.apply {
                holder.timeText.text =
                    formatTimeRange(startTime, endTime, hour12 = false)
                holder.dayText.text = "${data.dayOfWeek}"
            }
            return row
        }
    }

    class SlotDataHolder(
        val timeText: TextView,
        val dayText: TextView
    )
}