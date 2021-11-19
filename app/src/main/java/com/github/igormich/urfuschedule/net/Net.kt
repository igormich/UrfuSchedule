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

package com.github.igormich.urfuschedule.net

import android.util.Log
import com.github.igormich.urfuschedule.model.*
import com.tfowl.ktor.client.features.JsoupFeature
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.jsoup.nodes.Document
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val client by lazy {
    HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(JsoupFeature)
    }
}
private val MainScope = MainScope()

fun loadSchedule(
    id: Int,
    date: Date,
    userType: UserType,
    callback: (Map<String, List<Lesson>>) -> Unit
) {

    val df: DateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val dateUrl = df.format(date)
    runBlocking {
        withContext(Dispatchers.IO) {
            Log.d("loadSchedule", "https://urfu.ru/api/schedule/teacher/lessons/$id/${dateUrl}/")
            val response: HttpResponse = when (userType) {
                UserType.TEACHER -> client.get(
                    "https://urfu.ru/api/schedule/teacher/lessons/$id/${dateUrl}/"
                )
                UserType.STUDENT -> client.get(
                    "https://urfu.ru/api/schedule/groups/lessons/$id/${dateUrl}/"
                )
            }
            val schedule = scheduleParse(
                response
                    .receive<Document>()
                    .body(),
                userType
            )
            Log.d("loadSchedule", schedule.toString())
            MainScope.launch {
                callback(schedule)
            }
        }
    }
}

fun loadTeacherSuggestions(it: String, callback: (List<SuggestionItem>) -> Unit) {
    runBlocking {
        withContext(Dispatchers.IO) {
            val response: HttpResponse = client.get(
                "https://urfu.ru/api/schedule/groups/suggest/?" + listOf("query" to it).formUrlEncode()
            )
            val teacherSuggestions = response.receive<Suggestions>()
            MainScope.launch {
                callback(teacherSuggestions.suggestions)
            }
        }
    }
}

fun loadGroupSuggestions(it: String, callback: (List<SuggestionItem>) -> Unit) {
    runBlocking {
        withContext(Dispatchers.IO) {
            val response: HttpResponse = client.get(
                "https://urfu.ru/api/schedule/teacher/suggest/?" + listOf("query" to it).formUrlEncode()
            )
            val teacherSuggestions = response.receive<Suggestions>()
            MainScope.launch {
                callback(teacherSuggestions.suggestions)
            }
        }
    }
}

