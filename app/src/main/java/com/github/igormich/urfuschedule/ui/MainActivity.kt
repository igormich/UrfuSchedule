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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.igormich.urfuschedule.model.*
import com.github.igormich.urfuschedule.net.loadGroupSuggestions
import com.github.igormich.urfuschedule.net.loadSchedule
import com.github.igormich.urfuschedule.net.loadTeacherSuggestions
import com.google.android.material.datepicker.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.util.*
import java.text.DateFormat
import android.provider.AlarmClock
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.items
import androidx.core.content.ContextCompat
import com.example.ui.ui.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Init()
        }
    }

    private fun isNewUser(): Boolean {
        val pref = getPreferences(Context.MODE_PRIVATE)
        return pref.getInt("id", -1) == -1
    }
    @Composable
    fun Init() {

        val startDestination = if (isNewUser()) {
            "start"
        } else {
            "loadLectures"
        }
        UrfuTheme {
            val navController = rememberNavController()//
            NavHost(navController = navController, startDestination = startDestination) {
                composable("start") { Start(navController) }
                composable("searchGroup") { SearchGroup(navController) }
                composable("searchName") { SearchName(navController) }
                composable("loadLectures") {
                    LoadLectures(navController)
                }
                /*...*/
            }
        }
    }

    @Composable
    private fun LoadLectures(navController: NavHostController) {
        val (title, id, userType) = try {
            val pref = getPreferences(Context.MODE_PRIVATE)
            val title = pref.getString("title", "")
            require(!title.isNullOrEmpty())
            val id = pref.getInt("id", -1)
            require(id != -1)
            val userType = UserType.valueOf(pref.getString("type", "") ?: "")
            Triple(title, id, userType)
        } catch (e: Exception) {
            navController.navigate("start") {
                popUpTo(0)
            }
            return
        }

        var date by remember { mutableStateOf(Date()) }
        var lesson by remember { mutableStateOf(listOf<Lesson>()) }
        var loading by remember { mutableStateOf(true) }
        if (loading) {
            loadSchedule(id, date, userType) {
                loading = false
                lesson = it.values.firstOrNull() ?: emptyList()
            }
        }
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                var openDialog by remember { mutableStateOf(false) }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp, 32.dp)
                        .padding(start = 10.dp)
                        .align(CenterVertically)
                        .clickable {
                            openDialog = true
                        },
                    tint = MaterialTheme.colors.onSurface
                )
                if (openDialog)
                    AlertDialog(onDismissRequest = { openDialog = false },
                        title = { Text("Стартовый экран") },
                        text = { Text("Забыть данные и вернуться на стартовый экран?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    getPreferences(Context.MODE_PRIVATE).edit().clear().apply()
                                    navController.navigate("start")
                                }) {
                                Text("Да")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    openDialog = false
                                }) {
                                Text("Нет, не надо!")
                            }
                        }
                    )
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            val activity = LocalContext.current as AppCompatActivity
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp, 48.dp)
                        .padding(start = 10.dp)
                        .align(CenterVertically)
                        .clickable(enabled = date.time > startDay()) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            calendar.add(Calendar.DAY_OF_YEAR, -1)
                            loading = true
                            date = calendar.time
                        },
                    tint = if (date.time > startDay()) Color.DarkGray else Color.LightGray
                )
                Box(
                    modifier = Modifier
                        .wrapContentSize(Alignment.TopStart)
                        .padding(top = 10.dp)
                        .border(0.5.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                        .clickable {
                            showDatePicker(activity, date) { newDate ->
                                date = Date(newDate)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val df: DateFormat =
                            DateFormat.getDateInstance(DateFormat.LONG, Locale("RU"))
                        val formattedDate: String = df.format(date)
                        Text(
                            text = formattedDate,
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.onSurface,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp, 20.dp),
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp, 48.dp)
                        .padding(start = 10.dp)
                        .align(CenterVertically)
                        .clickable(enabled = date.time < endDay()) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                            loading = true
                            date = calendar.time
                        },
                    tint = if (date.time < endDay()) Color.DarkGray else Color.LightGray
                )
            }
            when {
                loading -> {
                    Spacer(modifier = Modifier.weight(1.0f))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f)
                            .padding(100.dp),
                        strokeWidth = 10.dp
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                }
                lesson.isEmpty() -> {
                    Text(text = "В это день занятий нет!", textAlign = TextAlign.Center, fontSize = 40.sp)
                }
                else -> LazyColumn(modifier = Modifier.padding(10.dp)) {
                    items(lesson, itemContent = {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            verticalAlignment = CenterVertically
                        ) {
                            Column {
                                Text(text = it.timeStart, modifier = Modifier.padding(5.dp))
                                Spacer(modifier = Modifier.weight(1.0f))
                                Text(text = it.timeEnd, modifier = Modifier.padding(5.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.9f)
                                    .width(1.dp)
                                    .background(color = Color.DarkGray)
                                    .align(CenterVertically)
                            )
                            Column(modifier = Modifier.padding(5.dp)) {
                                Text(text = it.discipline)
                                Text(text = it.location)
                                when (it) {
                                    is LessonForTeacher -> Text(text = it.group)
                                    is LessonForStudent -> Text(text = it.teacher)
                                }
                                it.location.looksLikeAddress()?.let { address ->
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp, 32.dp)
                                            .padding(start = 10.dp)
                                            .clickable {
                                                val args = listOf("q" to address).formUrlEncode()
                                                val gmmIntentUri = Uri.parse("geo:0,0?$args")
                                                val mapIntent =
                                                    Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                mapIntent.setPackage("com.google.android.apps.maps")
                                                startActivity(mapIntent)

                                            },
                                        tint = Color.DarkGray
                                    )
                                }
                                if (it == lesson.first()) {
                                    val launcher = rememberLauncherForActivityResult(
                                        ActivityResultContracts.RequestPermission()
                                    ) { isGranted: Boolean ->
                                        if (isGranted) {
                                            // Permission Accepted: Do something
                                            Log.d("ExampleScreen", "PERMISSION GRANTED")
                                        } else {
                                            // Permission Denied: Do something
                                            Log.d("ExampleScreen", "PERMISSION DENIED")
                                        }
                                    }

                                    val context = LocalContext.current
                                    Icon(
                                        imageVector = Icons.Default.AlarmOn,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp, 32.dp)
                                            .padding(start = 10.dp)
                                            .clickable {
                                                when (PackageManager.PERMISSION_GRANTED) {
                                                    ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.SET_ALARM
                                                    ) -> {
                                                        // Some works that require permission
                                                        Log.d(
                                                            "ExampleScreen",
                                                            "Code requires permission"
                                                        )
                                                        val i = Intent(AlarmClock.ACTION_SET_ALARM)
                                                        //i.putExtra(AlarmClock.EXTRA_MESSAGE, "New Alarm")
                                                        i.putExtra(AlarmClock.EXTRA_HOUR, 11)
                                                        //i.putExtra(AlarmClock.EXTRA_MINUTES, 20)
                                                        i.putExtra(AlarmClock.EXTRA_DAYS, 20)
                                                        startActivity(i)
                                                    }
                                                    else -> {
                                                        // Asking for permission
                                                        launcher.launch(Manifest.permission.SET_ALARM)
                                                    }
                                                }

                                            },
                                        tint = Color.DarkGray
                                    )
                                }
                            }
                        }
                        Divider(
                            color = Color.DarkGray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    })
                }
            }

        }

    }

    @Composable
    private fun SearchGroup(navController: NavHostController) {
        var text by remember { mutableStateOf("") }
        //val suggestions by remember { mutableStateListOf<String>() }
        var suggestions by remember { mutableStateOf(listOf<SuggestionItem>()) }
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(value = text,
                onValueChange = {
                    text = it
                    if (it.length < 3)
                        return@TextField
                    loadTeacherSuggestions(it) {
                        suggestions = it
                    }
                },
                placeholder = { Text(text = "Номер группы", color = Color.DarkGray) })
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(suggestions, itemContent = {
                        Button(onClick = {
                            val pref = getPreferences(Context.MODE_PRIVATE).edit()
                            pref.putString("title", it.value)
                            pref.putInt("id", it.data)
                            pref.putString("type", UserType.STUDENT.toString())
                            pref.apply()
                            val navOptions = NavOptions.Builder()
                            navController.navigate(
                                "loadLectures",
                                navOptions.build()
                            )
                        }) {
                            Text(
                                text = it.value, modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()
                            )
                        }
                    })
                }
            }
        }
    }


    @Composable
    private fun SearchName(navController: NavHostController) {
        var text by remember { mutableStateOf("") }
        //val suggestions by remember { mutableStateListOf<String>() }
        var suggestions by remember { mutableStateOf(listOf<SuggestionItem>()) }
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(value = text,
                onValueChange = {
                    text = it
                    if (it.length < 3)
                        return@TextField
                    loadGroupSuggestions(it) {
                        suggestions = it
                    }
                },
                placeholder = { Text(text = "Фамилия", color = Color.DarkGray) })
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(suggestions, itemContent = {
                        Button(onClick = {

                            val pref = getPreferences(Context.MODE_PRIVATE).edit()
                            pref.putString("title", it.value)
                            pref.putInt("id", it.data)
                            pref.putString("type", UserType.TEACHER.toString())
                            pref.apply()
                            val navOptions = NavOptions.Builder()
                            navController.navigate(
                                "loadLectures",
                                navOptions.build()
                            )
                        }) {
                            Text(
                                text = it.value, modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()
                            )
                        }
                    })
                }
            }
        }
    }

    @Composable
    fun Start(navController: NavHostController) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Расписание УрФУ",
                textAlign = TextAlign.Center,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Text(text = "Привет!", textAlign = TextAlign.Center, fontSize = 40.sp)
            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Кто ты?", textAlign = TextAlign.Center, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                Button(onClick = {
                    navController.navigate("searchName")
                }) {
                    Text(
                        text = "Преподаватель",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { navController.navigate("searchGroup") }) {
                    Text(
                        text = "Студент",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = "Это не официальное приложение, оно сломается если УрФУ сменит API." +
                        "\nГарантий не даю, денег не прошу." +
                        "\nДанные храню только на телефоне, передаю только на сайт УрФУ." +
                        "\nОтказ от ответвенности и всё такое...",
                textAlign = TextAlign.Center
            )

        }
    }

}
