package com.example.office_hours_client.adapters

import android.content.Context
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import com.example.office_hours_client.models.UserPublicData

abstract class DataAdapter<T : Any> : ListAdapter {
    abstract val context: Context
    abstract val list: List<T>
    override fun registerDataSetObserver(p0: DataSetObserver?) {
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getCount(): Int {
        return list.count()
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    abstract override fun getItemId(p0: Int): Long

    override fun hasStableIds(): Boolean {
        return false
    }

    abstract override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View

    override fun getItemViewType(p0: Int): Int {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(p0: Int): Boolean {
        return true
    }
}