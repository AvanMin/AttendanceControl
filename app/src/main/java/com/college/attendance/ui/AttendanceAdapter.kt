package com.college.attendance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.college.attendance.R
import com.college.attendance.data.Student

class AttendanceAdapter(
    private val students: List<Student>,
    initialStatuses: Map<Int, String>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    private val selectedStatuses = mutableMapOf<Int, String>()

    init {
        students.forEach { student ->
            selectedStatuses[student.id] = initialStatuses[student.id] ?: "Присутствовал"
        }
    }

    inner class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val rgStatus: RadioGroup = view.findViewById(R.id.rgStatus)
        val rbPresent: RadioButton = view.findViewById(R.id.rbPresent)
        val rbAbsent: RadioButton = view.findViewById(R.id.rbAbsent)
        val rbLate: RadioButton = view.findViewById(R.id.rbLate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_student, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val student = students[position]
        holder.tvStudentName.text = "${student.lastName} ${student.firstName}"

        holder.rgStatus.setOnClickListener(null)
        holder.rgStatus.setOnCheckedChangeListener(null)

        when (selectedStatuses[student.id]) {
            "Присутствовал" -> holder.rbPresent.isChecked = true
            "Отсутствовал" -> holder.rbAbsent.isChecked = true
            "Опоздал" -> holder.rbLate.isChecked = true
        }

        holder.rgStatus.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.rbPresent -> "Присутствовал"
                R.id.rbAbsent -> "Отсутствовал"
                R.id.rbLate -> "Опоздал"
                else -> "Присутствовал"
            }
            selectedStatuses[student.id] = status
        }
    }

    override fun getItemCount(): Int = students.size

    fun getCurrentStatuses(): Map<Int, String> = selectedStatuses.toMap()
}