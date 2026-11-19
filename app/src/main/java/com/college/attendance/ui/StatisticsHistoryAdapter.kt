package com.college.attendance.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.college.attendance.R
import com.college.attendance.data.AttendanceHistoryItem
import com.college.attendance.data.StatisticsCalculator

class StatisticsHistoryAdapter(
    private var items: List<AttendanceHistoryItem> = emptyList()
) : RecyclerView.Adapter<StatisticsHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentAndGroup: TextView = view.findViewById(R.id.tvHistoryStudentGroup)
        val tvLessonInfo: TextView = view.findViewById(R.id.tvHistoryLessonInfo)
        val tvStatus: TextView = view.findViewById(R.id.tvHistoryStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.tvStudentAndGroup.text = "${item.studentFullName} (${item.groupName})"
        holder.tvLessonInfo.text = "${item.lessonDate} • ${item.subject}"
        holder.tvStatus.text = item.status

        val color = when (item.status) {
            StatisticsCalculator.STATUS_PRESENT -> Color.parseColor("#2E7D32")
            StatisticsCalculator.STATUS_ABSENT -> Color.parseColor("#C62828")
            StatisticsCalculator.STATUS_LATE -> Color.parseColor("#EF6C00")
            else -> Color.DKGRAY
        }
        holder.tvStatus.setTextColor(color)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<AttendanceHistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}