package com.college.attendance.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.college.attendance.R
import com.college.attendance.data.AppDatabase
import com.college.attendance.data.FieldError
import com.college.attendance.data.Student
import com.college.attendance.data.StudentEntity
import com.college.attendance.data.StudentValidator
import kotlinx.coroutines.launch

class StudentsActivity : AppCompatActivity() {

    private lateinit var etLastName: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etGroupName: EditText
    private lateinit var btnAddStudent: Button
    private lateinit var tvEmpty: TextView
    private lateinit var tvCount: TextView
    private lateinit var rvStudents: RecyclerView

    private lateinit var db: AppDatabase
    private val adapter = StudentAdapter { student -> confirmDelete(student) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)
        title = getString(R.string.students_title)

        db = AppDatabase.getInstance(this)

        findViewById<Button>(R.id.btnBackToMain)?.setOnClickListener {
            finish()
        }

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
        val lastName = etLastName.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val groupName = etGroupName.text.toString().trim()

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

        lifecycleScope.launch {
            val duplicates = db.studentDao().countDuplicate(lastName, firstName, groupName)
            if (duplicates > 0) {
                Toast.makeText(this@StudentsActivity, R.string.error_duplicate, Toast.LENGTH_LONG).show()
            } else {
                db.studentDao().insert(
                    StudentEntity(
                        firstName = firstName,
                        lastName = lastName,
                        groupName = groupName
                    )
                )
                etLastName.text.clear()
                etFirstName.text.clear()
                etGroupName.text.clear()
                etLastName.requestFocus()
                refreshList()
                Toast.makeText(this@StudentsActivity, R.string.student_added, Toast.LENGTH_SHORT).show()
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
                lifecycleScope.launch {
                    db.studentDao().delete(
                        StudentEntity(
                            id = student.id,
                            firstName = student.firstName,
                            lastName = student.lastName,
                            groupName = student.groupName
                        )
                    )
                    refreshList()
                    Toast.makeText(this@StudentsActivity, R.string.student_deleted, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun refreshList() {
        lifecycleScope.launch {
            val entities = db.studentDao().getAll()
            val students = entities.map {
                Student(id = it.id, firstName = it.firstName, lastName = it.lastName, groupName = it.groupName)
            }
            adapter.submitList(students)
            tvEmpty.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
            rvStudents.visibility = if (students.isEmpty()) View.GONE else View.VISIBLE
            tvCount.text = getString(R.string.students_count, students.size)
        }
    }
}