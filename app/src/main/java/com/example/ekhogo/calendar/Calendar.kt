package com.example.ekhogo.calendar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ekhogo.schedule.TimePickerDialogUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


fun getMonthGrid(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()

    // Sunday = 0, Monday = 1, ... Saturday = 6
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    val dates = mutableListOf<LocalDate?>()

    repeat(startOffset) {
        dates.add(null)
    }

    for (day in 1..daysInMonth) {
        dates.add(yearMonth.atDay(day))
    }
    while (dates.size < 42) {
        dates.add(null)
    }

    return dates
}

@Composable
fun MonthDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    hasEvents: Boolean,
    eventColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(6.dp)
            .clickable(enabled = date != null) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    when {
                        date == null -> Color.Transparent
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> Color.LightGray
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (date != null) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = if (isSelected) Color.White else Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(6.dp)
                    .background(eventColor, CircleShape)
            )
        }

    }

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun CalendarScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var checkLink by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    var events by remember { mutableStateOf<Map<LocalDate, List<Event>>>(emptyMap()) }

    var eventText by remember { mutableStateOf("") }
    var showAddEventDialog by remember { mutableStateOf(false) }

    var selectedColor by remember { mutableStateOf(Color.Red) }
    var showColorPicker by remember { mutableStateOf(false) }

    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var isStartAM by remember { mutableStateOf(true) }

    var endHour by remember { mutableStateOf(10) }
    var endMinute by remember { mutableStateOf(0) }
    var isEndAM by remember { mutableStateOf(true) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var selectedEvent by remember { mutableStateOf<Event?>(null) }

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
        if (account == null) {
            checkLink = false
        } else {
            checkLink = true
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedEvent = null // reset edit mode
                    eventText = "" // clear old text
                    showAddEventDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Event"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
        ) {

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous month"
                    )
                }

                Text(
                    text = "${
                        currentMonth.month.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                        )
                    } ${currentMonth.year}", fontSize = 22.sp
                )

                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next month"
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            val firstDayOfMonth = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val startOffset = firstDayOfMonth.dayOfWeek.value % 7
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1
                        val cellDate = if (dayNumber in 1..daysInMonth) {
                            currentMonth.atDay(dayNumber)
                        } else {
                            null
                        }
                        val isSelected = cellDate == selectedDate
                        val isToday = cellDate == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(2.dp)
                                .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                                .clickable(enabled = cellDate != null) {
                                    selectedDate = cellDate!!
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellDate != null) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> Color(0xFFFFE5E5)
                                                else -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isSelected) Color.White else Color.Black
                                    )

                                    if (cellDate != null && events.containsKey(cellDate)) {
                                        val eventDots = events[cellDate]?.take(3) ?: emptyList()

                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            eventDots.forEach { event ->

                                                val dotColor = when (event.color) {
                                                    "blue" -> Color.Blue
                                                    "green" -> Color.Green
                                                    "yellow" -> Color.Yellow
                                                    else -> Color.Red
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(dotColor, CircleShape)
                                                )

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            val formatter = DateTimeFormatter.ofPattern("EEE, MMMM d")

            Text(
                text = "Events for ${selectedDate.format(formatter)}",
                modifier = Modifier.padding(start = 16.dp),
                fontSize = 18.sp
            )

            val selectedEvents = events[selectedDate]

            if (selectedEvents != null) {
                for ((index, event) in selectedEvents.withIndex()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            .clickable {
                                selectedEvent = event
                                showAddEventDialog = true
                            }
                    ) {
                        val dotColor = when (event.color) {
                            "blue" -> Color.Blue
                            "green" -> Color.Green
                            "yellow" -> Color.Yellow
                            else -> Color.Red
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(dotColor, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = if (event.timeStart == "Start Time") {
                                "${event.title}"
                            } else {
                                "${event.title} : ${event.timeStart} - ${event.timeEnd}"
                            },
                            modifier = Modifier.weight(1f),
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        }
    }

    if (showAddEventDialog) {
        Dialog(onDismissRequest = { showAddEventDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight(0.82f)
                        .background(Color.White, shape = RoundedCornerShape(28.dp))
                        .padding(20.dp)
                ) {
                    LaunchedEffect(selectedEvent) {
                        selectedEvent?.let {
                            eventText = it.title

                            selectedColor = when (it.color) {
                                "blue" -> Color.Blue
                                "green" -> Color.Green
                                "yellow" -> Color.Yellow
                                else -> Color.Red
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(selectedColor, shape = CircleShape)
                                .clickable { showColorPicker = true }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = eventText,
                            onValueChange = { eventText = it },
                            placeholder = { Text("Add title") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (showColorPicker) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            listOf(
                                Color.Red,
                                Color.Blue,
                                Color.Green,
                                Color.Yellow
                            ).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(color, CircleShape)
                                        .clickable {
                                            selectedColor = color
                                            showColorPicker = false
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Start")

                        TextButton(onClick = { showStartPicker = true }) {
                            Text(
                                "${startHour}:${
                                    startMinute.toString().padStart(2, '0')
                                } ${if (isStartAM) "AM" else "PM"}"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("End")

                        TextButton(onClick = { showEndPicker = true }) {
                            Text(
                                "${endHour}:${
                                    endMinute.toString().padStart(2, '0')
                                } ${if (isEndAM) "AM" else "PM"}"
                            )
                        }
                    }

                    if (showStartPicker) {
                        TimePickerDialogUI(
                            onConfirm = { hour, minute ->
                                val isPM = hour >= 12
                                val displayHour = when {
                                    hour == 0 -> 12
                                    hour > 12 -> hour - 12
                                    else -> hour
                                }

                                startHour = displayHour
                                startMinute = minute
                                isStartAM = !isPM
                                showStartPicker = false
                            },
                            onDismiss = { showStartPicker = false }
                        )
                    }

                    if (showEndPicker) {
                        TimePickerDialogUI(
                            onConfirm = { hour, minute ->
                                val isPM = hour >= 12
                                val displayHour = when {
                                    hour == 0 -> 12
                                    hour > 12 -> hour - 12
                                    else -> hour
                                }

                                endHour = displayHour
                                endMinute = minute
                                isEndAM = !isPM
                                showEndPicker = false
                            },
                            onDismiss = { showEndPicker = false }
                        )
                    }

                    Button(
                        onClick = {
                            val user = FirebaseAuth.getInstance().currentUser
                            val uid = user?.uid
                            val db = FirebaseFirestore.getInstance()

                            val selectedTimeStart =
                                "${startHour}:${
                                    startMinute.toString().padStart(2, '0')
                                } ${if (isStartAM) "AM" else "PM"}"

                            val selectedTimeEnd =
                                "${endHour}:${
                                    endMinute.toString().padStart(2, '0')
                                } ${if (isEndAM) "AM" else "PM"}"

                            if (uid != null && eventText.isNotBlank()) {
                                val eventData = hashMapOf(
                                    "title" to eventText,
                                    "date" to selectedDate.toString(),
                                    "timeStart" to selectedTimeStart,
                                    "timeEnd" to selectedTimeEnd,
                                    "color" to when (selectedColor) {
                                        Color.Red -> "red"
                                        Color.Blue -> "blue"
                                        Color.Green -> "green"
                                        Color.Yellow -> "yellow"
                                        else -> "red"
                                    },
                                    "createdAt" to System.currentTimeMillis()
                                )

                                val eventsRef = db.collection("users")
                                    .document(uid)
                                    .collection("events")

                                if (selectedEvent != null) {
                                    eventsRef.document(selectedEvent!!.id)
                                        .set(eventData)
                                        .addOnSuccessListener {
                                            selectedEvent = null
                                            eventText = ""
                                            showAddEventDialog = false

                                            eventsFireBase { resultMap ->
                                                events = resultMap
                                            }
                                        }
                                } else {
                                    eventsRef.add(eventData)
                                        .addOnSuccessListener {
                                            eventText = ""
                                            showAddEventDialog = false

                                            eventsFireBase { resultMap ->
                                                events = resultMap
                                            }
                                        }
                                }
                            } else {
                                println("Save failed: uid=$uid, eventText='$eventText'")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save")
                    }
                    
                    if (selectedEvent != null) {
                        TextButton(
                            onClick = {
                                val user = FirebaseAuth.getInstance().currentUser
                                val uid = user?.uid
                                val db = FirebaseFirestore.getInstance()

                                if (uid != null) {
                                    db.collection("users")
                                        .document(uid)
                                        .collection("events")
                                        .document(selectedEvent!!.id)
                                        .delete()
                                        .addOnSuccessListener {
                                            selectedEvent = null
                                            eventText = ""
                                            showAddEventDialog = false

                                            eventsFireBase { resultMap ->
                                                events = resultMap
                                            }
                                        }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Event", color = Color.Red)
                        }
                    }

                    TextButton(
                        onClick = { showAddEventDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}




