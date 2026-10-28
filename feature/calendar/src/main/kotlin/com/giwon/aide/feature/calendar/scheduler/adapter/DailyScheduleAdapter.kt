package com.giwon.aide.feature.calendar.scheduler.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent

class DailyScheduleAdapter(
    @LayoutRes private val layoutResId: Int,
) : RecyclerView.Adapter<ScheduleViewHolder>() {

    private val items = mutableListOf<CalendarEvent>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder(layoutResId, parent)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<CalendarEvent>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
