package com.college.attendance.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentValidatorTest {

    @Test
    fun validData_isValid() {
        val result = StudentValidator.validate("Иванов", "Пётр", "ИС-21")
        assertTrue(result.isValid)
    }

    @Test
    fun emptyFields_areRejected() {
        val result = StudentValidator.validate("", "", "")
        assertEquals(FieldError.EMPTY, result.lastName)
        assertEquals(FieldError.EMPTY, result.firstName)
        assertEquals(FieldError.EMPTY, result.groupName)
        assertFalse(result.isValid)
    }

    @Test
    fun spacesOnly_areRejected() {
        val result = StudentValidator.validate("   ", "  ", "    ")
        assertEquals(FieldError.EMPTY, result.lastName)
        assertEquals(FieldError.EMPTY, result.firstName)
        assertEquals(FieldError.EMPTY, result.groupName)
    }

    @Test
    fun digitsInName_areRejected() {
        val result = StudentValidator.validate("Иванов1", "Пётр", "ИС-21")
        assertEquals(FieldError.INVALID, result.lastName)
        assertNull(result.firstName)
        assertNull(result.groupName)
    }

    @Test
    fun hyphenOnlyName_isRejected() {
        val result = StudentValidator.validate("-", "Пётр", "ИС-21")
        assertEquals(FieldError.INVALID, result.lastName)
    }

    @Test
    fun doubleSurname_isAccepted() {
        val result = StudentValidator.validate("Петров-Водкин", "Анна-Мария", "ИС-21")
        assertTrue(result.isValid)
    }

    @Test
    fun specialSymbolsInGroup_areRejected() {
        val result = StudentValidator.validate("Иванов", "Пётр", "ИС#21")
        assertEquals(FieldError.INVALID, result.groupName)
    }

    @Test
    fun tooLongName_isRejected() {
        val result = StudentValidator.validate("А".repeat(51), "Пётр", "ИС-21")
        assertEquals(FieldError.INVALID, result.lastName)
    }

    @Test
    fun normalize_trimsAndCollapsesSpaces() {
        assertEquals("Иван Петров", StudentValidator.normalize("  Иван    Петров  "))
    }
}
