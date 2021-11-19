package com.github.igormich.urfuschedule.ui

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.github.igormich.urfuschedule.model.*
import com.github.igormich.urfuschedule.net.loadSchedule
import com.github.igormich.urfuschedule.strings.STRINGS
import java.text.DateFormat
import java.util.*

@Composable
fun Activity.LoadLectures(navController: NavHostController) {
    val (id, userType) = try {
        val pref = getPreferences(Context.MODE_PRIVATE)
        val id = pref.getInt("id", -1)
        require(id != -1)
        val userType = UserType.valueOf(pref.getString("type", "") ?: "")
        id to userType
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
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScheduleTitle(navController, date) {
                date = it
                loading = true
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
                    Text(
                        text = STRINGS.NO_LESSON_TODAY,
                        textAlign = TextAlign.Center,
                        fontSize = 40.sp
                    )
                }
                else -> LazyColumn(modifier = Modifier.padding(10.dp)) {
                    items(lesson, itemContent = {
                        BuildItem(it)
                    })
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            onClick = {navController.navigate("addCustomLection")},
        ) {
            Text(
                "+",
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun BuildItem(it: Lesson) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = CenterVertically
    ) {
        Column {
            it.time.let { time ->
            when (time) {
                is SimpleInterval -> {
                    Text(text = time.start.toString(), modifier = Modifier.padding(5.dp))
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(text =time.end.toString(), modifier = Modifier.padding(5.dp))
                }
                is FallbackLessonTime -> {
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(text = time.time, modifier = Modifier.padding(5.dp))
                    Spacer(modifier = Modifier.weight(1.0f))
                }
            }
        }
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
            /*it.location.looksLikeAddress()?.let { address ->
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
                Icon(
                    imageVector = Icons.Default.AlarmOn,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp, 32.dp)
                        .padding(start = 10.dp)
                        .clickable {
                            val i = Intent(AlarmClock.ACTION_SET_ALARM)
                            i.putExtra(AlarmClock.EXTRA_HOUR, 11)
                            i.putExtra(AlarmClock.EXTRA_DAYS, 20)
                            startActivity(i)
                        },
                    tint = Color.DarkGray
                )
            }*/
        }
    }
    Divider(
        color = Color.DarkGray,
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
fun Activity.ScheduleTitle(navController: NavHostController, date: Date, callback: (Date) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        var openDialog by remember { mutableStateOf(false) }
        Icon(
            imageVector = Icons.Default.ResetTv,
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
            ShowResetDialog(navController) { openDialog = false }
        val pref = getPreferences(Context.MODE_PRIVATE)
        val title = pref.getString("title", null) ?: throw IllegalStateException("No Title")
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
                    callback(calendar.time)
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
                        callback(Date(newDate))
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
                    callback(calendar.time)
                },
            tint = if (date.time < endDay()) Color.DarkGray else Color.LightGray
        )
    }
}

@Composable
fun Activity.ShowResetDialog(navController: NavHostController, onDismissRequest: () -> Unit) {
    AlertDialog(onDismissRequest = onDismissRequest,
        title = { Text(STRINGS.START_SCREEN) },
        text = { Text(STRINGS.ASK_RESET_SELECTION) },
        confirmButton = {
            Button(
                onClick = {
                    val pref = getPreferences(Context.MODE_PRIVATE)
                    pref.edit().clear().apply()
                    navController.navigate("start")
                }) {
                Text(STRINGS.YES)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text(STRINGS.PLEASE_NO)
            }
        }
    )
}
