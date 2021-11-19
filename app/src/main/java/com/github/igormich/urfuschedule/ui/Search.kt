package com.github.igormich.urfuschedule.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.github.igormich.urfuschedule.model.SuggestionItem
import com.github.igormich.urfuschedule.model.UserType
import com.github.igormich.urfuschedule.net.loadGroupSuggestions
import com.github.igormich.urfuschedule.net.loadTeacherSuggestions
import com.github.igormich.urfuschedule.strings.STRINGS

@Composable
fun Activity.SearchGroup(navController: NavHostController) {
    var text by remember { mutableStateOf("") }
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
                loadTeacherSuggestions(it) {result->
                    suggestions = result
                }
            },
            placeholder = { Text(text = STRINGS.GROUP_NUMBER, color = Color.DarkGray) })
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
fun Activity.SearchName(navController: NavHostController) {
    var text by remember { mutableStateOf("") }
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
                loadGroupSuggestions(it) { result->
                    suggestions = result
                }
            },
            placeholder = { Text(text = STRINGS.FAMILY_NAME, color = Color.DarkGray) })
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