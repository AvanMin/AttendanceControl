package com.college.attendance.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudentRepositoryTest {

    @Before
    fun setUp() {
        StudentRepository.clear()
    }

    @Test
    fun add_increasesListSize() {
        val result = StudentRepository.add("Иванов", "Пётр", "ИС-21")
        assertTrue(result is AddResult.Added)
        assertEquals(1, StudentRepository.getAll().size)
    }

    @Test
    fun add_trimsInput() {
        StudentRepository.add("  Иванов ", " Пётр", "ИС-21  ")
        val student = StudentRepository.getAll().first()
        assertEquals("Иванов", student.lastName)
        assertEquals("Пётр", student.firstName)
        assertEquals("ИС-21", student.groupName)
    }

    @Test
    fun add_duplicateIsRejected_ignoringCaseAndSpaces() {
        StudentRepository.add("Иванов", "Пётр", "ИС-21")
        val result = StudentRepository.add(" иванов ", "ПЁТР", "ис-21")
        assertEquals(AddResult.Duplicate, result)
        assertEquals(1, StudentRepository.getAll().size)
    }

    @Test
    fun add_sameNameInOtherGroup_isAllowed() {
        StudentRepository.add("Иванов", "Пётр", "ИС-21")
        val result = StudentRepository.add("Иванов", "Пётр", "ИС-22")
        assertTrue(result is AddResult.Added)
        assertEquals(2, StudentRepository.getAll().size)
    }

    @Test
    fun remove_deletesOnlySelectedStudent() {
        val first = (StudentRepository.add("Иванов", "Пётр", "ИС-21") as AddResult.Added).student
        StudentRepository.add("Сидоров", "Олег", "ИС-21")
        assertTrue(StudentRepository.remove(first.id))
        val rest = StudentRepository.getAll()
        assertEquals(1, rest.size)
        assertEquals("Сидоров", rest.first().lastName)
    }

    @Test
    fun remove_unknownId_returnsFalse() {
        assertFalse(StudentRepository.remove(999))
    }

    @Test
    fun getAll_isSortedByGroupThenLastName() {
        StudentRepository.add("Яковлев", "Иван", "ИС-22")
        StudentRepository.add("Борисов", "Иван", "ИС-21")
        StudentRepository.add("Андреев", "Иван", "ИС-21")
        val names = StudentRepository.getAll().map { it.lastName }
        assertEquals(listOf("Андреев", "Борисов", "Яковлев"), names)
    }
}
