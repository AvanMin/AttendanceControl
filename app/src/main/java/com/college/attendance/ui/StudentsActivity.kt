package com.college.attendance.ui

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.college.attendance.R
import com.college.attendance.data.AddResult
import com.college.attendance.data.FieldError
import com.college.attendance.data.Student
import com.college.attendance.data.StudentRepository
import com.college.attendance.data.StudentValidator

class StudentsActivity : AppCompatActivity() {

    private lateinit var etLastName: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etGroupName: EditText
    private lateinit var btnAddStudent: Button
    private lateinit var tvEmpty: TextView
    private lateinit var tvCount: TextView
    private lateinit var rvStudents: RecyclerView

    private val adapter = StudentAdapter { student -> confirmDelete(student) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)
        title = getString(R.string.students_title)

        etLastName = findViewById(R.id.etLastName)
        etFirstName = findViewById(R.id.etFirstName)
        etGroupName = findViewById(R.id.etGroupName)
        btnAddStudent = findViewById(R.id.btnAddStudent)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvCount = findViewById(R.id.tvCount)
        rvStudents = findViewById(R.id.rvStudents)

        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter

        btnAddStudent.setOnClickListener { addStudent() }
        etGroupName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addStudent()
                true
            } else {
                false
            }
        }

        refreshList()
    }

    private fun addStudent() {
        val lastName = etLastName.text.toString()
        val firstName = etFirstName.text.toString()
        val groupName = etGroupName.text.toString()

        etLastName.error = null
        etFirstName.error = null
        etGroupName.error = null

        val validation = StudentValidator.validate(lastName, firstName, groupName)
        if (!validation.isValid) {
            etLastName.error = nameErrorText(validation.lastName)
            etFirstName.error = nameErrorText(validation.firstName)
            etGroupName.error = groupErrorText(validation.groupName)
            focusFirstInvalid(validation.lastName, validation.firstName)
            return
        }

        when (StudentRepository.add(lastName, firstName, groupName)) {
            is AddResult.Added -> {
                etLastName.text.clear()
                etFirstName.text.clear()
                etGroupName.text.clear()
                etLastName.requestFocus()
                refreshList()
                Toast.makeText(this, R.string.student_added, Toast.LENGTH_SHORT).show()
            }
            AddResult.Duplicate -> {
                Toast.makeText(this, R.string.error_duplicate, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun focusFirstInvalid(lastName: FieldError?, firstName: FieldError?) {
        when {
            lastName != null -> etLastName.requestFocus()
            firstName != null -> etFirstName.requestFocus()
            else -> etGroupName.requestFocus()
        }
    }

    private fun nameErrorText(error: FieldError?): String? = when (error) {
        FieldError.EMPTY -> getString(R.string.error_empty)
        FieldError.INVALID -> getString(R.string.error_invalid_name)
        null -> null
    }

    private fun groupErrorText(error: FieldError?): String? = when (error) {
        FieldError.EMPTY -> getString(R.string.error_empty)
        FieldError.INVALID -> getString(R.string.error_invalid_group)
        null -> null
    }

    private fun confirmDelete(student: Student) {
        val fullName = getString(R.string.student_full_name, student.lastName, student.firstName)
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, fullName, student.groupName))
            .setPositiveButton(R.string.delete) { _, _ ->
                StudentRepository.remove(student.id)
                refreshList()
                Toast.makeText(this, R.string.student_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun refreshList() {
        val students = StudentRepository.getAll()
        adapter.submitList(students)
        tvEmpty.visibility = if (students.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        rvStudents.visibility = if (students.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        tvCount.text = getString(R.string.students_count, students.size)
    }
}
