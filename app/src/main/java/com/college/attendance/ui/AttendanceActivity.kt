package com.college.attendance.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.college.attendance.R
import com.college.attendance.data.Attendance
import com.college.attendance.data.AttendanceRepository
import com.college.attendance.data.Lesson
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var spinnerGroup: Spinner
    private lateinit var etLessonDate: EditText
    private lateinit var etSubject: EditText
    private lateinit var btnCreateLesson: Button
    private lateinit var spinnerSavedLessons: Spinner
    private lateinit var rvStudents: RecyclerView
    private lateinit var tvEmptyList: TextView
    private lateinit var btnSaveAttendance: Button

    private var adapter: AttendanceAdapter? = null
    private var selectedLesson: Lesson? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)
        findViewById<Button>(R.id.btnBackToMain).setOnClickListener {
            finish()
        }
        initViews()
        setupGroupSpinner()
        setupDatePicker()
        setupListeners()
        refreshSavedLessonsSpinner()
    }

    private fun initViews() {
        spinnerGroup = findViewById(R.id.spinnerGroup)
        etLessonDate = findViewById(R.id.etLessonDate)
        etSubject = findViewById(R.id.etSubject)
        btnCreateLesson = findViewById(R.id.btnCreateLesson)
        spinnerSavedLessons = findViewById(R.id.spinnerSavedLessons)
        rvStudents = findViewById(R.id.rvAttendanceStudents)
        tvEmptyList = findViewById(R.id.tvEmptyAttendanceList)
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance)

        rvStudents.layoutManager = LinearLayoutManager(this)
    }

    private fun setupGroupSpinner() {
        val groups = AttendanceRepository.getGroups()
        if (groups.isEmpty()) {
            val emptyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Нет групп"))
            spinnerGroup.adapter = emptyAdapter
            spinnerGroup.isEnabled = false
        } else {
            val groupAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groups)
            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGroup.adapter = groupAdapter
            spinnerGroup.isEnabled = true
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        etLessonDate.setText(format.format(calendar.time))

        etLessonDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etLessonDate.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupListeners() {
        btnCreateLesson.setOnClickListener {
            val selectedGroup = spinnerGroup.selectedItem?.toString() ?: ""
            val date = etLessonDate.text.toString().trim()
            val subject = etSubject.text.toString().trim()

            if (!spinnerGroup.isEnabled || selectedGroup.isEmpty()) {
                Toast.makeText(this, "Сначала добавьте студентов и группы", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (date.isEmpty() || subject.isEmpty()) {
                Toast.makeText(this, "Введите дату и название дисциплины", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (lesson, isNew) = AttendanceRepository.addOrGetLesson(selectedGroup, date, subject)
            etSubject.text.clear()

            if (isNew) {
                Toast.makeText(this, "Занятие «$subject» сохранено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Такое занятие уже есть — открыто из списка", Toast.LENGTH_SHORT).show()
            }

            val index = AttendanceRepository.lessons.indexOf(lesson)
            refreshSavedLessonsSpinner(selectIndex = index)
        }

        spinnerSavedLessons.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val lessons = AttendanceRepository.lessons
                if (lessons.isEmpty() || position !in lessons.indices) return

                val lesson = lessons[position]
                selectedLesson = lesson
                loadStudentList(lesson)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSaveAttendance.setOnClickListener {
            val currentAdapter = adapter ?: return@setOnClickListener
            val lesson = selectedLesson ?: return@setOnClickListener
            val statusesMap = currentAdapter.getCurrentStatuses()

            val records = statusesMap.map { (studentId, status) ->
                Attendance(
                    studentId = studentId,
                    lessonDate = lesson.lessonDate,
                    subject = lesson.subject,
                    status = status
                )
            }

            AttendanceRepository.saveAttendanceList(records)
            Toast.makeText(
                this,
                "Сохранено для «${lesson.subject}» (${records.size} чел.)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun refreshSavedLessonsSpinner(selectIndex: Int = 0) {
        val lessons = AttendanceRepository.lessons
        if (lessons.isEmpty()) {
            val emptyAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Нет сохранённых занятий")
            )
            spinnerSavedLessons.adapter = emptyAdapter
            spinnerSavedLessons.isEnabled = false
            rvStudents.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
            btnSaveAttendance.isEnabled = false
        } else {
            val titles = lessons.map { "${it.lessonDate} | ${it.groupName} — ${it.subject}" }
            val lessonAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
            lessonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSavedLessons.adapter = lessonAdapter
            spinnerSavedLessons.isEnabled = true

            if (selectIndex in lessons.indices) {
                spinnerSavedLessons.setSelection(selectIndex)
            }
        }
    }

    private fun loadStudentList(lesson: Lesson) {
        val students = AttendanceRepository.getStudentsByGroup(lesson.groupName)

        if (students.isEmpty()) {
            rvStudents.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
            tvEmptyList.text = "В группе «${lesson.groupName}» пока нет студентов."
            btnSaveAttendance.isEnabled = false
        } else {
            tvEmptyList.visibility = View.GONE
            rvStudents.visibility = View.VISIBLE
            btnSaveAttendance.isEnabled = true

            val existingStatuses = students.associate { student ->
                student.id to (AttendanceRepository.getStatusForStudent(
                    student.id,
                    lesson.lessonDate,
                    lesson.subject
                ) ?: "Присутствовал")
            }

            adapter = AttendanceAdapter(students, existingStatuses)
            rvStudents.adapter = adapter
        }
    }
}