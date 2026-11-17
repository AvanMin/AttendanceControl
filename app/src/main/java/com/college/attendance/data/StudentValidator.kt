package com.college.attendance.data

enum class FieldError { EMPTY, INVALID }

data class ValidationResult(
    val lastName: FieldError?,
    val firstName: FieldError?,
    val groupName: FieldError?
) {
    val isValid: Boolean
        get() = lastName == null && firstName == null && groupName == null
}

object StudentValidator {
    private val spaces = Regex("\\s+")

    fun normalize(value: String): String = value.trim().replace(spaces, " ")

    fun validate(lastName: String, firstName: String, groupName: String): ValidationResult =
        ValidationResult(
            lastName = checkName(normalize(lastName)),
            firstName = checkName(normalize(firstName)),
            groupName = checkGroup(normalize(groupName))
        )

    private fun checkName(value: String): FieldError? = when {
        value.isEmpty() -> FieldError.EMPTY
        value.length > 50 -> FieldError.INVALID
        value.none { it.isLetter() } -> FieldError.INVALID
        !value.all { it.isLetter() || it == '-' || it == ' ' || it == '\'' } -> FieldError.INVALID
        else -> null
    }

    private fun checkGroup(value: String): FieldError? = when {
        value.isEmpty() -> FieldError.EMPTY
        value.length > 20 -> FieldError.INVALID
        value.none { it.isLetterOrDigit() } -> FieldError.INVALID
        !value.all { it.isLetterOrDigit() || it == '-' || it == ' ' } -> FieldError.INVALID
        else -> null
    }
}
