package com.college.attendance.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.college.attendance.R
import com.college.attendance.data.AppDatabase
import com.college.attendance.data.AttendanceEntity
import com.college.attendance.data.LessonEntity
import com.college.attendance.data.Student
import kotlinx.coroutines.launch
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

    private lateinit var db: AppDatabase
    private var adapter: AttendanceAdapter? = null
    private var savedLessons: List<LessonEntity> = emptyList()
    private var selectedLesson: LessonEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        db = AppDatabase.getInstance(this)

        findViewById<Button>(R.id.btnBackToMain)?.setOnClickListener {
            finish()
        }

        initViews()
        setupDatePicker()
        setupListeners()
        loadInitialData()
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

    private fun loadInitialData() {
        lifecycleScope.launch {
            setupGroupSpinner()
            refreshSavedLessonsSpinner(selectId = null)
        }
    }

    private suspend fun setupGroupSpinner() {
        val groups = db.studentDao().getAllGroups()
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

            lifecycleScope.launch {
                val existing = db.attendanceDao().findLesson(selectedGroup, date, subject)
                val targetId: Int
                if (existing != null) {
                    targetId = existing.id
                    Toast.makeText(this@AttendanceActivity, "Такое занятие уже есть — открыто из БД", Toast.LENGTH_SHORT).show()
                } else {
                    val insertedId = db.attendanceDao().insertLesson(
                        LessonEntity(groupName = selectedGroup, lessonDate = date, subject = subject)
                    )
                    targetId = insertedId.toInt()
                    Toast.makeText(this@AttendanceActivity, "Занятие «$subject» сохранено в БД", Toast.LENGTH_SHORT).show()
                }
                etSubject.text.clear()
                refreshSavedLessonsSpinner(selectId = targetId)
            }
        }

        spinnerSavedLessons.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (savedLessons.isEmpty() || position !in savedLessons.indices) return
                val lesson = savedLessons[position]
                selectedLesson = lesson
                lifecycleScope.launch {
                    loadStudentList(lesson)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSaveAttendance.setOnClickListener {
            val currentAdapter = adapter ?: return@setOnClickListener
            val lesson = selectedLesson ?: return@setOnClickListener
            val statusesMap = currentAdapter.getCurrentStatuses()

            lifecycleScope.launch {
                statusesMap.forEach { (studentId, status) ->
                    // Гарантируем правило 1 итоговой отметки на студента и занятие
                    db.attendanceDao().deleteByStudentAndLesson(studentId, lesson.id)
                    db.attendanceDao().insertAttendance(
                        AttendanceEntity(
                            studentId = studentId,
                            lessonId = lesson.id,
                            lessonDate = lesson.lessonDate,
                            subject = lesson.subject,
                            status = status
                        )
                    )
                }
                Toast.makeText(
                    this@AttendanceActivity,
                    "Отметки сохранены в БД для «${lesson.subject}» (${statusesMap.size} чел.)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun refreshSavedLessonsSpinner(selectId: Int?) {
        savedLessons = db.attendanceDao().getAllLessons()
        if (savedLessons.isEmpty()) {
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
            val titles = savedLessons.map { "${it.lessonDate} | ${it.groupName} — ${it.subject}" }
            val lessonAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
            lessonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSavedLessons.adapter = lessonAdapter
            spinnerSavedLessons.isEnabled = true

            val index = if (selectId != null) {
                savedLessons.indexOfFirst { it.id == selectId }.coerceAtLeast(0)
            } else {
                0
            }
            spinnerSavedLessons.setSelection(index)
            selectedLesson = savedLessons[index]
            loadStudentList(savedLessons[index])
        }
    }

    private suspend fun loadStudentList(lesson: LessonEntity) {
        val entities = db.studentDao().getByGroup(lesson.groupName)
        val students = entities.map {
            Student(id = it.id, firstName = it.firstName, lastName = it.lastName, groupName = it.groupName)
        }

        if (students.isEmpty()) {
            rvStudents.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
            tvEmptyList.text = "В группе «${lesson.groupName}» пока нет студентов."
            btnSaveAttendance.isEnabled = false
        } else {
            tvEmptyList.visibility = View.GONE
            rvStudents.visibility = View.VISIBLE
            btnSaveAttendance.isEnabled = true

            val existingRecords = db.attendanceDao().getAttendanceByLesson(lesson.id)
                .associate { it.studentId to it.status }

            val initialStatuses = students.associate { student ->
                student.id to (existingRecords[student.id] ?: "Присутствовал")
            }

            adapter = AttendanceAdapter(students, initialStatuses)
            rvStudents.adapter = adapter
        }
    }
}
