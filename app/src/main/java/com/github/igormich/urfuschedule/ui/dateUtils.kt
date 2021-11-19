/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010-2020 Igor Mikhailov aka https://github.com/igormich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.igormich.urfuschedule.ui

import androidx.appcompat.app.AppCompatActivity
import com.github.igormich.urfuschedule.strings.STRINGS
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
    val picker = MaterialDatePicker.Builder.datePicker().setTitleText(STRINGS.SELECT_DATE)
        .setSelection(date.time)
        .setCalendarConstraints(constraintsBuilder.build()).build()
    picker.show(activity.supportFragmentManager, picker.toString())
    picker.addOnPositiveButtonClickListener {
        updatedDate(it)
    }
}