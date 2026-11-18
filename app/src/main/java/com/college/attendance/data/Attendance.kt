package com.college.attendance.data

data class Attendance(
    val studentId: Int,
    val lessonDate: String,
    val subject: String,
    val status: String // "Присутствовал", "Отсутствовал", "Опоздал"
)