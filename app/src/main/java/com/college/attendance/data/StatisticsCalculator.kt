package com.college.attendance.data

data class AttendanceStats(
    val totalRecords: Int,
    val totalLessons: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val attendancePercent: Int
)

data class AttendanceHistoryItem(
    val id: Int,
    val studentId: Int,
    val studentFullName: String,
    val groupName: String,
    val lessonId: Int,
    val lessonDate: String,
    val subject: String,
    val status: String
)

object StatisticsCalculator {

    const val STATUS_PRESENT = "Присутствовал"
    const val STATUS_ABSENT = "Отсутствовал"
    const val STATUS_LATE = "Опоздал"

    fun buildHistory(
        records: List<AttendanceEntity>,
        students: List<StudentEntity>
    ): List<AttendanceHistoryItem> {
        val studentsById = students.associateBy { it.id }
        return records.mapNotNull { record ->
            val student = studentsById[record.studentId] ?: return@mapNotNull null
            AttendanceHistoryItem(
                id = record.id,
                studentId = student.id,
                studentFullName = "${student.lastName} ${student.firstName}",
                groupName = student.groupName,
                lessonId = record.lessonId,
                lessonDate = record.lessonDate,
                subject = record.subject,
                status = record.status
            )
        }
    }

    fun filterHistory(
        items: List<AttendanceHistoryItem>,
        groupName: String? = null,
        studentId: Int? = null,
        lessonDate: String? = null
    ): List<AttendanceHistoryItem> {
        return items.filter { item ->
            val matchesGroup = groupName.isNullOrBlank() ||
                    item.groupName.equals(groupName.trim(), ignoreCase = true)
            val matchesStudent = studentId == null || item.studentId == studentId
            val matchesDate = lessonDate.isNullOrBlank() ||
                    item.lessonDate == lessonDate.trim()
            matchesGroup && matchesStudent && matchesDate
        }
    }

    fun calculate(records: List<AttendanceHistoryItem>): AttendanceStats {
        val total = records.size
        val uniqueLessons = records.map { it.lessonId }.distinct().size
        val present = records.count { it.status == STATUS_PRESENT }
        val absent = records.count { it.status == STATUS_ABSENT }
        val late = records.count { it.status == STATUS_LATE }
        val percent = if (total == 0) {
            0
        } else {
            ((present + late) * 100.0 / total).toInt()
        }
        return AttendanceStats(
            totalRecords = total,
            totalLessons = uniqueLessons,
            presentCount = present,
            absentCount = absent,
            lateCount = late,
            attendancePercent = percent
        )
    }
}