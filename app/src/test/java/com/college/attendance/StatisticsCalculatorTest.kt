package com.college.attendance

import com.college.attendance.data.AttendanceEntity
import com.college.attendance.data.StatisticsCalculator
import com.college.attendance.data.StudentEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsCalculatorTest {

    private val students = listOf(
        StudentEntity(id = 1, firstName = "Иван", lastName = "Иванов", groupName = "ИС-21"),
        StudentEntity(id = 2, firstName = "Пётр", lastName = "Петров", groupName = "ИС-21"),
        StudentEntity(id = 3, firstName = "Анна", lastName = "Сидорова", groupName = "2ИП-4")
    )

    private val records = listOf(
        AttendanceEntity(id = 1, studentId = 1, lessonId = 10, lessonDate = "18.11.2026", subject = "Базы данных", status = "Присутствовал"),
        AttendanceEntity(id = 2, studentId = 2, lessonId = 10, lessonDate = "18.11.2026", subject = "Базы данных", status = "Отсутствовал"),
        AttendanceEntity(id = 3, studentId = 1, lessonId = 11, lessonDate = "19.11.2026", subject = "Kotlin", status = "Опоздал"),
        AttendanceEntity(id = 4, studentId = 3, lessonId = 12, lessonDate = "19.11.2026", subject = "Сети", status = "Присутствовал")
    )

    @Test
    fun calculate_returnsExpectedCountsAndPercent() {
        val history = StatisticsCalculator.buildHistory(records, students)
        val stats = StatisticsCalculator.calculate(history)

        assertEquals(4, stats.totalRecords)
        assertEquals(3, stats.totalLessons)
        assertEquals(2, stats.presentCount)
        assertEquals(1, stats.absentCount)
        assertEquals(1, stats.lateCount)
        assertEquals(75, stats.attendancePercent)
    }

    @Test
    fun calculate_emptyHistory_returnsZerosWithoutCrash() {
        val stats = StatisticsCalculator.calculate(emptyList())
        assertEquals(0, stats.totalRecords)
        assertEquals(0, stats.totalLessons)
        assertEquals(0, stats.presentCount)
        assertEquals(0, stats.absentCount)
        assertEquals(0, stats.lateCount)
        assertEquals(0, stats.attendancePercent)
    }

    @Test
    fun filterHistory_byGroupStudentAndDate() {
        val history = StatisticsCalculator.buildHistory(records, students)

        assertEquals(3, StatisticsCalculator.filterHistory(history, groupName = "ИС-21").size)
        assertEquals(2, StatisticsCalculator.filterHistory(history, studentId = 1).size)
        assertEquals(2, StatisticsCalculator.filterHistory(history, lessonDate = "19.11.2026").size)

        val combined = StatisticsCalculator.filterHistory(
            history,
            groupName = "ИС-21",
            studentId = 1,
            lessonDate = "19.11.2026"
        )
        assertEquals(1, combined.size)
        assertEquals("Опоздал", combined.first().status)
    }
}