package com.college.attendance.data

object AttendanceRepository {
    val lessons = mutableListOf<Lesson>()
    val attendanceRecords = mutableListOf<Attendance>()

    fun getGroups(): List<String> {
        return StudentRepository.getAll()
            .map { it.groupName }
            .distinct()
            .sorted()
    }

    fun getStudentsByGroup(groupName: String): List<Student> {
        return StudentRepository.getAll()
            .filter { it.groupName.equals(groupName, ignoreCase = true) }
            .sortedBy { it.lastName }
    }

    fun addOrGetLesson(groupName: String, lessonDate: String, subject: String): Pair<Lesson, Boolean> {
        val existing = lessons.find {
            it.groupName.equals(groupName, ignoreCase = true) &&
                    it.lessonDate == lessonDate &&
                    it.subject.equals(subject, ignoreCase = true)
        }
        if (existing != null) {
            return Pair(existing, false) // занятие уже существует
        }
        val newId = (lessons.maxOfOrNull { it.id } ?: 0) + 1
        val newLesson = Lesson(newId, groupName, lessonDate, subject)
        lessons.add(newLesson)
        return Pair(newLesson, true) // создано новое занятие
    }

    fun saveAttendanceList(newRecords: List<Attendance>) {
        newRecords.forEach { newRecord ->
            attendanceRecords.removeAll {
                it.studentId == newRecord.studentId &&
                        it.lessonDate == newRecord.lessonDate &&
                        it.subject.equals(newRecord.subject, ignoreCase = true)
            }
            attendanceRecords.add(newRecord)
        }
    }

    fun getStatusForStudent(studentId: Int, lessonDate: String, subject: String): String? {
        return attendanceRecords.find {
            it.studentId == studentId &&
                    it.lessonDate == lessonDate &&
                    it.subject.equals(subject, ignoreCase = true)
        }?.status
    }
}