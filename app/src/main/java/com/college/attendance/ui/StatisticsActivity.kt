package com.college.attendance.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.college.attendance.R
import com.college.attendance.data.AppDatabase
import com.college.attendance.data.AttendanceHistoryItem
import com.college.attendance.data.StatisticsCalculator
import com.college.attendance.data.StudentEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var spinnerFilterGroup: Spinner
    private lateinit var spinnerFilterStudent: Spinner
    private lateinit var etFilterDate: EditText
    private lateinit var btnClearDate: Button
    private lateinit var btnResetFilters: Button

    private lateinit var tvStatLessons: TextView
    private lateinit var tvStatRecords: TextView
    private lateinit var tvStatPresent: TextView
    private lateinit var tvStatAbsent: TextView
    private lateinit var tvStatLate: TextView
    private lateinit var tvStatPercent: TextView

    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmptyHistory: TextView

    private lateinit var db: AppDatabase
    private val historyAdapter = StatisticsHistoryAdapter()

    private var allStudents: List<StudentEntity> = emptyList()
    private var allHistoryItems: List<AttendanceHistoryItem> = emptyList()

    private var groupOptions: List<String> = emptyList()
    private var studentOptions: List<StudentEntity?> = emptyList()

    private var selectedGroupName: String? = null
    private var selectedStudentId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        db = AppDatabase.getInstance(this)

        findViewById<Button>(R.id.btnBackToMain)?.setOnClickListener {
            finish()
        }

        initViews()
        setupDateFilter()
        setupFilterListeners()
        loadDataFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        loadDataFromDatabase()
    }

    private fun initViews() {
        spinnerFilterGroup = findViewById(R.id.spinnerFilterGroup)
        spinnerFilterStudent = findViewById(R.id.spinnerFilterStudent)
        etFilterDate = findViewById(R.id.etFilterDate)
        btnClearDate = findViewById(R.id.btnClearDate)
        btnResetFilters = findViewById(R.id.btnResetFilters)

        tvStatLessons = findViewById(R.id.tvStatLessons)
        tvStatRecords = findViewById(R.id.tvStatRecords)
        tvStatPresent = findViewById(R.id.tvStatPresent)
        tvStatAbsent = findViewById(R.id.tvStatAbsent)
        tvStatLate = findViewById(R.id.tvStatLate)
        tvStatPercent = findViewById(R.id.tvStatPercent)

        rvHistory = findViewById(R.id.rvAttendanceHistory)
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory)

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter
    }

    private fun setupDateFilter() {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        etFilterDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etFilterDate.setText(format.format(calendar.time))
                    applyFilters()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnClearDate.setOnClickListener {
            if (etFilterDate.text.isNotEmpty()) {
                etFilterDate.text.clear()
                applyFilters()
            }
        }

        btnResetFilters.setOnClickListener {
            selectedGroupName = null
            selectedStudentId = null
            etFilterDate.text.clear()
            if (spinnerFilterGroup.count > 0) {
                spinnerFilterGroup.setSelection(0, false)
            }
            updateStudentSpinner(null)
            applyFilters()
        }
    }

    private fun setupFilterListeners() {
        spinnerFilterGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newGroup = if (position <= 0 || position >= groupOptions.size) {
                    null
                } else {
                    groupOptions[position]
                }
                if (newGroup != selectedGroupName) {
                    selectedGroupName = newGroup
                    selectedStudentId = null
                    updateStudentSpinner(selectedGroupName)
                    applyFilters()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerFilterStudent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newStudentId = if (position <= 0 || position >= studentOptions.size) {
                    null
                } else {
                    studentOptions[position]?.id
                }
                if (newStudentId != selectedStudentId) {
                    selectedStudentId = newStudentId
                    applyFilters()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadDataFromDatabase() {
        lifecycleScope.launch {
            try {
                allStudents = db.studentDao().getAll()
                val allAttendance = db.attendanceDao().getAllAttendance()
                allHistoryItems = StatisticsCalculator.buildHistory(allAttendance, allStudents)

                setupGroupSpinner()
                applyFilters()
            } catch (e: Exception) {
                Toast.makeText(
                    this@StatisticsActivity,
                    "Ошибка загрузки статистики: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupGroupSpinner() {
        val groups = allStudents.map { it.groupName }.distinct().sorted()
        groupOptions = listOf(getString(R.string.filter_all_groups)) + groups

        val groupAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groupOptions)
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterGroup.adapter = groupAdapter

        val restoredIndex = selectedGroupName?.let { groupOptions.indexOf(it) } ?: 0
        val validIndex = if (restoredIndex >= 0) restoredIndex else 0
        if (validIndex == 0) selectedGroupName = null

        spinnerFilterGroup.setSelection(validIndex, false)
        updateStudentSpinner(selectedGroupName)
    }

    private fun updateStudentSpinner(groupFilter: String?) {
        val filteredStudents = if (groupFilter == null) {
            allStudents
        } else {
            allStudents.filter { it.groupName.equals(groupFilter, ignoreCase = true) }
        }

        studentOptions = listOf(null) + filteredStudents
        val labels = listOf(getString(R.string.filter_all_students)) +
                filteredStudents.map { "${it.lastName} ${it.firstName} (${it.groupName})" }

        val studentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterStudent.adapter = studentAdapter

        val restoredStudentIndex = selectedStudentId?.let { id ->
            studentOptions.indexOfFirst { it?.id == id }
        } ?: 0
        val validIndex = if (restoredStudentIndex >= 0) restoredStudentIndex else 0
        if (validIndex == 0) selectedStudentId = null

        spinnerFilterStudent.setSelection(validIndex, false)
    }

    private fun applyFilters() {
        val selectedDate = etFilterDate.text.toString().trim().takeIf { it.isNotEmpty() }

        val filtered = StatisticsCalculator.filterHistory(
            items = allHistoryItems,
            groupName = selectedGroupName,
            studentId = selectedStudentId,
            lessonDate = selectedDate
        )

        val stats = StatisticsCalculator.calculate(filtered)

        tvStatLessons.text = getString(R.string.stat_lessons, stats.totalLessons)
        tvStatRecords.text = getString(R.string.stat_records, stats.totalRecords)
        tvStatPresent.text = getString(R.string.stat_present, stats.presentCount)
        tvStatAbsent.text = getString(R.string.stat_absent, stats.absentCount)
        tvStatLate.text = getString(R.string.stat_late, stats.lateCount)
        tvStatPercent.text = getString(R.string.stat_percent, stats.attendancePercent)

        historyAdapter.updateItems(filtered)

        if (filtered.isEmpty()) {
            rvHistory.visibility = View.GONE
            tvEmptyHistory.visibility = View.VISIBLE
            tvEmptyHistory.text = if (allHistoryItems.isEmpty()) {
                getString(R.string.history_empty_db)
            } else {
                getString(R.string.history_empty_filtered)
            }
        } else {
            tvEmptyHistory.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
        }
    }
}