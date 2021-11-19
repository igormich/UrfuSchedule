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

package com.github.igormich.urfuschedule.strings


object RussianStrings {
    const val YES = "Да"
    const val PLEASE_NO = "Нет, не надо!"

    const val TITLE = "Расписание УрФУ"
    const val TEACHER = "Преподаватель"
    const val STUDENT = "Студент"
    const val HELLO = "Привет!"
    const val WHO_ARE_YOU = "Кто ты?"
    val ABOUT = """
        Это неофициальное приложение, оно сломается если УрФУ сменит API.
        Гарантий не даю, денег не прошу.
        Данные храню только на телефоне, передаю только на сайт УрФУ.
        Отказ от ответвенности и всё такое...""".trimIndent()
    const val ABOUT_ME = "Разработчик: Игорь Михайлов aka igormich"
    const val GROUP_NUMBER = "Номер группы"
    const val FAMILY_NAME = "Фамилия"

    const val SELECT_DATE = "Выберите дату"
    private const val gear = "\u2699"
    val ASK_RESET_SELECTION = """
        Вернуться на стартовый экран и выбрать новое расписание?
        Спецкурсы это не удалит, что бы изменить их используй $gear справа.
        """.trimIndent()
    const val START_SCREEN = "Стартовый экран"
    const val NO_LESSON_TODAY = "В это день занятий нет!"
}

val STRINGS = RussianStrings