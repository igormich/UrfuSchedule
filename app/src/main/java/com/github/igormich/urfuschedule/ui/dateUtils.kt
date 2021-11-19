package com.github.igormich.urfuschedule.ui

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.*
import java.util.*

const val interval = 7

fun startDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -interval)
    return calendar.time.time
}

fun endDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, interval)
    return calendar.time.time
}

fun showDatePicker(
    activity: AppCompatActivity,
    date: Date,
    updatedDate: (Long) -> Unit
) {
    val maxDate = endDay()
    val minDate = startDay()
    val constraintsBuilder = CalendarConstraints.Builder()
    val validators: ArrayList<CalendarConstraints.DateValidator> = ArrayList()
    validators.add(DateValidatorPointForward.from(minDate))
    validators.add(DateValidatorPointBackward.before(maxDate))
    constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators))
    constraintsBuilder.setOpenAt(date.time)
    val picker = MaterialDatePicker.Builder.datePicker().setTitleText("Выберите дату")
        .setSelection(date.time)
        .setCalendarConstraints(constraintsBuilder.build()).build()
    picker.show(activity.supportFragmentManager, picker.toString())
    picker.addOnPositiveButtonClickListener {
        updatedDate(it)
    }
}