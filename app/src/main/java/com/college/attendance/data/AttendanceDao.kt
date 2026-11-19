package com.college.attendance.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AttendanceDao {
    // --- Занятия ---
    @Insert
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Query("SELECT * FROM lessons ORDER BY id DESC")
    suspend fun getAllLessons(): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE LOWER(groupName) = LOWER(:groupName) AND lessonDate = :lessonDate AND LOWER(subject) = LOWER(:subject) LIMIT 1")
    suspend fun findLesson(groupName: String, lessonDate: String, subject: String): LessonEntity?

    // --- Посещаемость ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND lessonId = :lessonId")
    suspend fun deleteByStudentAndLesson(studentId: Int, lessonId: Int)

    @Query("SELECT * FROM attendance WHERE lessonId = :lessonId")
    suspend fun getAttendanceByLesson(lessonId: Int): List<AttendanceEntity>

    @Query("SELECT * FROM attendance ORDER BY id DESC")
    suspend fun getAllAttendance(): List<AttendanceEntity>

    @Query("SELECT DISTINCT lessonDate FROM attendance ORDER BY lessonDate DESC")
    suspend fun getAllAttendanceDates(): List<String>
}