package com.example.ekhogo.calendar

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.common.api.Scope
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.ekhogo.schedule.TimePickerDialogUI
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CalendarScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var checkLink by remember { mutableStateOf(false) }

    var showDialogStart by remember { mutableStateOf(false) }
    var selectedTimeStart by remember { mutableStateOf("Start Time") }
    var startHour by remember { mutableStateOf(0) }
    var startMinute by remember { mutableStateOf(0) }

    var showDialogEnd by remember { mutableStateOf(false) }
    var selectedTimeEnd by remember { mutableStateOf("End Time") }
    var endHour by remember { mutableStateOf(0) }
    var endMinute by remember { mutableStateOf(0) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentWeekStart by remember {
        mutableStateOf(
            selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
        )
    }

    var events: Map<LocalDate, List<Event>> by remember {     mutableStateOf<Map<LocalDate, List<Event>>>(emptyMap())
    }
    var eventText by remember { mutableStateOf("") }


    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    Scope("https://www.googleapis.com/auth/calendar.events")
                )
                .build()
        )
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            val email = account.email
            checkLink = true

        } catch (e: Exception) {
        }
    }

    LaunchedEffect(Unit) {
        eventsFireBase { result ->
            events = result
        }
    }

    LaunchedEffect(Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if(account == null){
            checkLink = false
        } else{
            checkLink = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    if (!checkLink) {
                        val signInIntent = googleSignInClient.signInIntent
                        signInLauncher.launch(signInIntent)
                    } else {
                        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .signOut()
                        checkLink = false
                    }
                },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(if (!checkLink) "Link to Google" else "Unlink Google")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                elevation = null
            ) {
                Text("<<--")
            }

            Text(
                text = "${
                    currentWeekStart.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    )
                } ${currentWeekStart.year}", fontSize = 30.sp,
                modifier = Modifier.padding(top = 6.dp)

            )

            Button(
                onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                elevation = null
            ) {
                Text("-->>")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0..6) {
                val date = currentWeekStart.plusDays(i.toLong())
                val isSelected = date == selectedDate
                val dayEvent = events[date]

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedDate = date }
                        .padding(8.dp)
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }

                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        val formatter = DateTimeFormatter.ofPattern("EEE, MMMM d")
        Text(
            text = "Events for ${selectedDate.format(formatter)}",
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 25.sp
        )
        val selectedEvents = events[selectedDate]

        if (selectedEvents != null) {
            for ((index, event) in selectedEvents.withIndex()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = if (event.timeStart == "Start Time") {"${event.title}"} else {"${event.title} : ${event.timeStart} - ${event.timeEnd}"},
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp
                    )
                    Button(
                        onClick = { // deletes directly from firestore
                            val user = FirebaseAuth.getInstance().currentUser
                            val uid = user?.uid
                            val db = FirebaseFirestore.getInstance()

                            if (uid != null) {
                                db.collection("users")
                                    .document(uid)
                                    .collection("events")
                                    .document(event.id)   // <-- use the ID
                                    .delete()
                                    .addOnSuccessListener {
                                        eventsFireBase { resultMap ->
                                            events = resultMap
                                        }
                                    }
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventText,
            onValueChange = { eventText = it },
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text("Add Event") }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = { showDialogStart = true },
                modifier = Modifier
                    .weight(1f),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Red
                )
            )
            {
                Text(
                    text = selectedTimeStart,
                )
            }

            var amPmStart = "AM"
            var pmHourStart = 0

            if (showDialogStart) {
                TimePickerDialogUI(
                    onConfirm = { hour, minute ->

                        startHour = hour
                        startMinute = minute

                        if (hour >= 12) {
                            pmHourStart = hour - 12
                            amPmStart = "PM"
                        }else if(hour < 12){
                            amPmStart = "AM"
                            pmHourStart = hour
                        }
                        selectedTimeStart = String.format("%02d:%02d %s", pmHourStart, minute, amPmStart)
                        showDialogStart = false
                    },
                    onDismiss = {
                        showDialogStart = false
                    }
                )
            }

            TextButton(
                onClick = { showDialogEnd = true },
                modifier = Modifier
                    .weight(1f),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Red
                )
            )
            {
                Text(
                    text = selectedTimeEnd,
                )
            }

            var amPmEnd = "AM"
            var pmHourEnd = 0

            if (showDialogEnd) {
                TimePickerDialogUI(
                    onConfirm = { hour, minute ->
                        endHour = hour
                        endMinute = minute

                        if (hour >= 12) {
                            pmHourEnd = hour - 12
                            amPmEnd = "PM"
                        }else if(hour < 12){
                            amPmEnd = "AM"
                            pmHourEnd = hour
                        }
                        selectedTimeEnd = String.format("%02d:%02d %s", pmHourEnd, minute, amPmEnd)
                        showDialogEnd = false
                    },
                    onDismiss = {
                        showDialogEnd = false
                    }
                )
            }

        }
            Button(
                onClick = {

                    val user = FirebaseAuth.getInstance().currentUser
                    val uid = user?.uid
                    val db = FirebaseFirestore.getInstance()

                    val eventData = hashMapOf(
                        "title" to eventText,
                        "date" to selectedDate.toString(),
                        "timeStart" to selectedTimeStart,
                        "timeEnd" to selectedTimeEnd
                    )

                    if (uid != null) {
                        db.collection("users")
                            .document(uid)
                            .collection("events")
                            .add(eventData)
                            .addOnSuccessListener {
                                eventsFireBase { resultMap ->
                                    events = resultMap
                                }
                            }
                    }

                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    val titleGoogle = eventText
                    if (account != null) {

                        scope.launch {
                            googleEvent(
                                context,
                                account,
                                titleGoogle,
                                selectedDate,
                                selectedTimeStart,
                                selectedTimeEnd,
                                startHour,
                                startMinute,
                                endHour,
                                endMinute
                            )
                        }
                    }
                    eventText = ""
                },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text("Add Event")
            }

    }


}

