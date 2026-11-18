package com.college.attendance.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: StudentEntity): Long

    @Query("SELECT * FROM students ORDER BY lastName ASC, firstName ASC")
    suspend fun getAll(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE LOWER(groupName) = LOWER(:groupName) ORDER BY lastName ASC")
    suspend fun getByGroup(groupName: String): List<StudentEntity>

    @Query("SELECT DISTINCT groupName FROM students ORDER BY groupName ASC")
    suspend fun getAllGroups(): List<String>

    @Query("SELECT COUNT(*) FROM students WHERE LOWER(lastName) = LOWER(:lastName) AND LOWER(firstName) = LOWER(:firstName) AND LOWER(groupName) = LOWER(:groupName)")
    suspend fun countDuplicate(lastName: String, firstName: String, groupName: String): Int

    @Delete
    suspend fun delete(student: StudentEntity)
}
