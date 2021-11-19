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