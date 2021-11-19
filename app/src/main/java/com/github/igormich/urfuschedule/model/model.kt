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

package com.github.igormich.urfuschedule.model

import kotlinx.serialization.Serializable
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator


enum class UserType {
    TEACHER, STUDENT
}

@Serializable
data class SuggestionItem(val value: String, val data: Int)

@Serializable
data class Suggestions(val suggestions: List<SuggestionItem>)

sealed class Lesson(
    val timeStart: String,
    val timeEnd: String,
    val discipline: String,
    val type: String,
    val location: String,
) {
    override fun toString(): String {
        return "timeStart='$timeStart', timeEnd='$timeEnd', discipline='$discipline', type='$type', location='$location'"
    }
}

class LessonForTeacher(
    timeStart: String,
    timeEnd: String,
    discipline: String,
    type: String,
    location: String,
    val group: String
) :
    Lesson(timeStart, timeEnd, discipline, type, location) {
    override fun toString(): String {
        return "LessonForTeacher(${super.toString()},group='$group')"
    }
}

class LessonForStudent(
    timeStart: String,
    timeEnd: String,
    discipline: String,
    type: String,
    location: String,
    val teacher: String
) :
    Lesson(timeStart, timeEnd, discipline, type, location) {
    override fun toString(): String {
        return "LessonForStudent(${super.toString()},teacher='$teacher')"
    }
}

fun scheduleParse(body: Element, userType: UserType): HashMap<String, MutableList<Lesson>> {
    val scheduleElements = body.select(Evaluator.Tag("tr"))
    val schedule = LinkedHashMap<String, MutableList<Lesson>>()
    var date = "";
    for (e in scheduleElements) {
        if ("divide" in e.classNames()) {
            date = e.text().takeIf { it.isNotBlank() } ?: continue
            schedule[date] = ArrayList()
        }
        if ("shedule-weekday-row" in e.classNames()) {
            if ("shedule-weekday-first-row" in e.classNames())
                continue;
            try {
                var timeInterval = e.selectFirst(Evaluator.Class("shedule-weekday-time"))?.text()
                    ?: throw IllegalStateException("No shedule-weekday-time")
                val (timeStart, timeEnd) = timeInterval.split(" - ")
                val item = e.selectFirst(Evaluator.Class("shedule-weekday-item"))
                    ?: throw IllegalStateException("No shedule-weekday-item")
                var discipline = item.child(0).text()
                discipline = discipline.replace(Regex("^\\d\\.\\s+"), "")
                val about = item.child(1)
                val list = schedule[date]?:throw IllegalStateException("Lesson before date")
                list += when (userType) {
                    UserType.TEACHER -> {
                        val info = about.children().toList()
                        when (info.size) {
                            1 -> LessonForTeacher(
                                timeStart,
                                timeEnd,
                                discipline,
                                info[0].text(),
                                "",
                                ""
                            )
                            2 -> LessonForTeacher(
                                timeStart,
                                timeEnd,
                                discipline,
                                info[0].text(),
                                info[1].text(),
                                ""
                            )
                            3 -> LessonForTeacher(
                                timeStart,
                                timeEnd,
                                discipline,
                                info[0].text(),
                                info[1].text(),
                                info[2].text().replace("Группа: ", "")
                            )
                            else -> throw IllegalStateException("Wrong lesson info $info")
                        }
                    }
                    UserType.STUDENT -> {
                        val info = about.children().toList()
                        when (info.size) {
                            1 -> LessonForStudent(
                                timeStart,
                                timeEnd, discipline, info[0].text(), "", ""
                            )
                            2 -> LessonForStudent(
                                timeStart,
                                timeEnd,
                                discipline,
                                info[0].text(),
                                info[1].text(),
                                ""
                            )
                            3 -> if (info[1].hasClass("cabinet")) {
                                LessonForStudent(
                                    timeStart,
                                    timeEnd,
                                    discipline,
                                    info[0].text(),
                                    info[1].text(),
                                    info[2].text().replace("Преподаватель: ", "")
                                )
                            } else {
                                LessonForStudent(
                                    timeStart,
                                    timeEnd,
                                    discipline,
                                    info[0].text(),
                                    info[2].text(),
                                    info[1].text().replace("Преподаватель: ", "")
                                )
                            }
                            else -> throw IllegalStateException("Wrong lesson info $info")
                        }
                    }
                }
            } catch (ee: Exception) {
                print(e)
                ee.printStackTrace()
            }
        }
    }
    return schedule
}

fun String.looksLikeAddress(): String? {
    val addressRegex = Regex("(ул.?)?[а-яА-ЯёЁ]{4,}[а-яА-Я ёЁ]{5,}[,.]? \\d+")
    return addressRegex.find(this, 0)?.groups?.first()?.value
}

