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

data class SimpleTime(val hours: Int, val minutes: Int) {
    companion object {
        fun fromString(s: String): SimpleTime {
            val (hours, minutes) = s.split(':').map { it.toInt() }
            return SimpleTime(hours, minutes)
        }
    }

    override fun toString() = "$hours:$minutes"
}

sealed interface LessonTime
data class SimpleInterval(val start: SimpleTime, val end: SimpleTime) : LessonTime
data class FallbackLessonTime(val time: String) : LessonTime
sealed interface Lesson {
    val time: LessonTime
    val discipline: String
    val type: String
    val location: String
}

data class LessonForTeacher(
    override val time: LessonTime,
    override val discipline: String,
    override val type: String,
    override val location: String,
    val group: String
) : Lesson

data class LessonForStudent(
    override val time: LessonTime,
    override val discipline: String,
    override val type: String,
    override val location: String,
    val teacher: String
) : Lesson {

}

fun scheduleParse(body: Element, userType: UserType): HashMap<String, MutableList<Lesson>> {
    val scheduleElements = body.select(Evaluator.Tag("tr"))
    val schedule = LinkedHashMap<String, MutableList<Lesson>>()
    var date = ""
    for (e in scheduleElements) {
        if ("divide" in e.classNames()) {
            date = e.text().takeIf { it.isNotBlank() } ?: continue
            schedule[date] = ArrayList()
        }
        if ("shedule-weekday-row" in e.classNames()) {
            if ("shedule-weekday-first-row" in e.classNames())
                continue
            try {
                val timeIntervalRaw = e.selectFirst(Evaluator.Class("shedule-weekday-time"))?.text()
                    ?: throw IllegalStateException("No shedule-weekday-time")

                val timeInterval = try {
                    val (begin, end) = timeIntervalRaw.split(Regex("[^:\\d]+"))
                        .map { SimpleTime.fromString(it) }
                    SimpleInterval(begin, end)
                } catch (e: Exception) {
                    FallbackLessonTime(timeIntervalRaw)
                }
                val item = e.selectFirst(Evaluator.Class("shedule-weekday-item"))
                    ?: throw IllegalStateException("No shedule-weekday-item")
                var discipline = item.child(0).text()
                discipline = discipline.replace(Regex("^\\d\\.\\s+"), "")
                val about = item.child(1)
                val list = schedule[date] ?: throw IllegalStateException("Lesson before date")
                list += when (userType) {
                    UserType.TEACHER -> {
                        val info = about.children().toList()
                        when (info.size) {
                            1 -> LessonForTeacher(
                                timeInterval,
                                discipline,
                                info[0].text(),
                                "",
                                ""
                            )
                            2 -> LessonForTeacher(
                                timeInterval,
                                discipline,
                                info[0].text(),
                                info[1].text(),
                                ""
                            )
                            3 -> LessonForTeacher(
                                timeInterval,
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
                                timeInterval, discipline, info[0].text(), "", ""
                            )
                            2 -> LessonForStudent(
                                timeInterval,
                                discipline,
                                info[0].text(),
                                info[1].text(),
                                ""
                            )
                            3 -> if (info[1].hasClass("cabinet")) {
                                LessonForStudent(
                                    timeInterval,
                                    discipline,
                                    info[0].text(),
                                    info[1].text(),
                                    info[2].text().replace("Преподаватель: ", "")
                                )
                            } else {
                                LessonForStudent(
                                    timeInterval,
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
    val addressRegex =
        Regex("(ул(\\.|ица)?)? [а-яА-ЯёЁ]{4,}[а-яА-Я ёЁ]{5,}[,.]?\\s*(д(ом|\\.)\\s*)?\\d+[а-яА-ЯёЁ]?")
    return addressRegex.find(this, 0)?.groups?.first()?.value
}

