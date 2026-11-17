package com.college.attendance.data

sealed class AddResult {
    data class Added(val student: Student) : AddResult()
    object Duplicate : AddResult()
}

object StudentRepository {
    private val students = mutableListOf<Student>()
    private var nextId = 1

    fun getAll(): List<Student> = students.sortedWith(
        compareBy<Student>(
            { it.groupName.lowercase() },
            { it.lastName.lowercase() },
            { it.firstName.lowercase() }
        )
    )

    fun add(lastName: String, firstName: String, groupName: String): AddResult {
        val last = StudentValidator.normalize(lastName)
        val first = StudentValidator.normalize(firstName)
        val group = StudentValidator.normalize(groupName)

        val exists = students.any {
            it.lastName.equals(last, ignoreCase = true) &&
                it.firstName.equals(first, ignoreCase = true) &&
                it.groupName.equals(group, ignoreCase = true)
        }
        if (exists) return AddResult.Duplicate

        val student = Student(nextId++, first, last, group)
        students.add(student)
        return AddResult.Added(student)
    }

    fun remove(id: Int): Boolean = students.removeAll { it.id == id }

    fun clear() {
        students.clear()
        nextId = 1
    }
}
