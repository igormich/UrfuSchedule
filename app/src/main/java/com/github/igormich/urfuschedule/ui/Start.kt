package com.github.igormich.urfuschedule.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.github.igormich.urfuschedule.strings.STRINGS

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
            text = STRINGS.TITLE,
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(text = STRINGS.HELLO, textAlign = TextAlign.Center, fontSize = 40.sp)
        // Add a vertical space between the author and message texts
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = STRINGS.WHO_ARE_YOU, textAlign = TextAlign.Center, fontSize = 40.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Column(modifier = Modifier.width(IntrinsicSize.Min)) {
            Button(onClick = {
                navController.navigate("searchName")
            }) {
                Text(
                    text = STRINGS.TEACHER,
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
                    text = STRINGS.STUDENT,
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
            text = STRINGS.ABOUT,
            textAlign = TextAlign.Justify
        )
    }
}