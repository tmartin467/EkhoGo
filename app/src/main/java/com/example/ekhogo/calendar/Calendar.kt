package com.example.ekhogo.calendar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


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
    eventColor: String?,
    onClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val isToday = date == LocalDate.now()

    val dayBackgroundColor = when {
        date == null -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> if (isDarkTheme) Color(0xFF4A1F1F) else Color(0xFFFFD6D6)
        else -> Color.Transparent
    }

    val dayTextColor = when {
        date == null -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> if (isDarkTheme) Color.White else Color(0xFF4A1F1F)
        else -> MaterialTheme.colorScheme.onBackground
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(enabled = date != null) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(dayBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (date != null) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = dayTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (eventColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(getEventColor(eventColor))
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}


@Composable
fun CalendarScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var checkLink by remember { mutableStateOf(false) }
    var isGoogleLinking by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    var events by remember { mutableStateOf<Map<LocalDate, List<Event>>>(emptyMap()) }

    var eventText by remember { mutableStateOf("") }
    var showAddEventDialog by remember { mutableStateOf(false) }

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

    var isAllDay by remember { mutableStateOf(false) }

    var eventLocation by remember { mutableStateOf("") }
    var eventNotes by remember { mutableStateOf("") }

    var eventStartDate by remember { mutableStateOf(selectedDate) }
    var eventEndDate by remember { mutableStateOf(selectedDate) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedColorName by remember { mutableStateOf("tomato") }

    var viewMode by remember { mutableStateOf(CalendarViewMode.Month) }

    var eventErrorMessage by remember { mutableStateOf<String?>(null) }

    var selectedView by remember { mutableStateOf("Month") }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }

    val dialogDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

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

            scope.launch {
                val accessToken = getAccessToken(context, account)

                if (accessToken != null) {
                    checkLink = true
                    fetchGoogleEventsAndSaveToFirestore(accessToken)

                    kotlinx.coroutines.delay(800)

                    eventsFireBase { resultMap ->
                        events = resultMap
                        isGoogleLinking = false
                    }
                } else {
                    println("Google access token was null")
                    checkLink = false
                    isGoogleLinking = false
                }
            }

        } catch (e: Exception) {
            println("Google sign-in failed: ${e.message}")
            e.printStackTrace()
            checkLink = false
            isGoogleLinking = false
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
                    eventLocation = ""
                    eventNotes = ""
                    isAllDay = false
                    eventStartDate = selectedDate
                    eventEndDate = selectedDate
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
                .verticalScroll(rememberScrollState())
                .pointerInput(viewMode) {
                    var totalDrag = 0f

                    detectVerticalDragGestures(
                        onDragStart = {
                            totalDrag = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                        },
                        onDragEnd = {
                            if (totalDrag < -80 && viewMode == CalendarViewMode.Month) {
                                viewMode = CalendarViewMode.Week
                            } else if (totalDrag > 80 && viewMode == CalendarViewMode.Week) {
                                viewMode = CalendarViewMode.Month
                            }
                        }
                    )
                },
            verticalArrangement = Arrangement.Top,
        ) {
            var menuOpen by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ☰ Hamburger
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Calendar view menu")
                    }

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        CalendarViewMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                onClick = {
                                    viewMode = mode
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }
                Button(
                    enabled = !isGoogleLinking,
                    onClick = {
                        if (!checkLink) {
                            isGoogleLinking = true
                            val signInIntent = googleSignInClient.signInIntent
                            signInLauncher.launch(signInIntent)
                        } else {
                            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .signOut()

                            checkLink = false

                            val user = FirebaseAuth.getInstance().currentUser
                            val uid = user?.uid
                            val db = FirebaseFirestore.getInstance()

                            if (uid != null) {
                                db.collection("users")
                                    .document(uid)
                                    .collection("events")
                                    .whereEqualTo("source", "google")
                                    .get()
                                    .addOnSuccessListener { result ->
                                        for (doc in result) {
                                            doc.reference.delete()
                                        }

                                        eventsFireBase { resultMap ->
                                            events = resultMap
                                        }
                                    }
                            }
                        }
                    },
                ) {
                    Text(if (!checkLink) "Link to Google" else "Unlink Google")
                }
            }

            CalendarTopBar(
                currentMonth = currentMonth,
                viewMode = viewMode,
                onPreviousMonth = {
                    currentMonth = currentMonth.minusMonths(1)

                    selectedDate =
                        if (currentMonth.year == LocalDate.now().year &&
                            currentMonth.month == LocalDate.now().month
                        ) {
                            LocalDate.now()
                        } else {
                            currentMonth.atDay(1)
                        }
                },

                onNextMonth = {
                    currentMonth = currentMonth.plusMonths(1)

                    selectedDate =
                        if (currentMonth.year == LocalDate.now().year &&
                            currentMonth.month == LocalDate.now().month
                        ) {
                            LocalDate.now()
                        } else {
                            currentMonth.atDay(1)
                        }
                },
                onViewModeChange = { viewMode = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (viewMode) {
                CalendarViewMode.Month -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                                val isDarkTheme = isSystemInDarkTheme()

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .padding(2.dp)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(8.dp)
                                        )
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
                                                        isToday -> if (isDarkTheme) Color(
                                                            0xFF4A1F1F
                                                        ) else Color(
                                                            0xFFFFD6D6
                                                        )

                                                        else -> Color.Transparent
                                                    }
                                                ),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    isToday -> if (isDarkTheme) Color.White else Color(
                                                        0xFF4A1F1F
                                                    )

                                                    else -> MaterialTheme.colorScheme.onBackground
                                                },
                                                modifier = Modifier.padding(
                                                    start = 4.dp,
                                                    top = 2.dp
                                                )
                                            )

                                            if (cellDate != null) {

                                                val eventsForDay =
                                                    events.values.flatten().filter { event ->
                                                        val start =
                                                            LocalDate.parse(event.startDate)
                                                        val end = LocalDate.parse(event.endDate)
                                                        cellDate >= start && cellDate <= end
                                                    }

                                                if (eventsForDay.isNotEmpty()) {
                                                    val eventBars = eventsForDay.take(3)

                                                    val singleDayEvents = eventBars.filter {
                                                        LocalDate.parse(it.startDate) == LocalDate.parse(
                                                            it.endDate
                                                        )
                                                    }

                                                    val multiDayEvents = eventBars.filter {
                                                        LocalDate.parse(it.startDate) != LocalDate.parse(
                                                            it.endDate
                                                        )
                                                    }

                                                    Column(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(bottom = 3.dp),
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            2.dp
                                                        ),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                2.dp
                                                            ),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            singleDayEvents.forEach { event ->
                                                                val barColor =
                                                                    getEventColor(event.color)

                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(5.dp)
                                                                        .background(
                                                                            barColor,
                                                                            CircleShape
                                                                        )
                                                                )
                                                            }
                                                        }

                                                        multiDayEvents.forEach { event ->
                                                            val start =
                                                                LocalDate.parse(event.startDate)
                                                            val end =
                                                                LocalDate.parse(event.endDate)

                                                            val barColor =
                                                                getEventColor(event.color)

                                                            val shape = when {
                                                                cellDate == start -> RoundedCornerShape(
                                                                    topStart = 50.dp,
                                                                    bottomStart = 50.dp
                                                                )

                                                                cellDate == end -> RoundedCornerShape(
                                                                    topEnd = 50.dp,
                                                                    bottomEnd = 50.dp
                                                                )

                                                                else -> RoundedCornerShape(0.dp)
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .width(26.dp)
                                                                    .height(5.dp)
                                                                    .background(barColor, shape)
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
                    }
                }

                CalendarViewMode.Week -> {
                    WeekCalendarView(
                        selectedDate = selectedDate,
                        events = events,
                        onDateSelected = {
                            selectedDate = it
                        }
                    )
                }

                CalendarViewMode.Day -> {
                    DayView(
                        selectedDay = selectedDate,
                        events = events
                    )
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            val formatter = DateTimeFormatter.ofPattern("EEE, MMMM d")

            Text(
                text = "Events for ${selectedDate.format(formatter)}",
                modifier = Modifier.padding(start = 16.dp),
                fontSize = 18.sp
            )

            val selectedEvents = events.values.flatten().filter { event ->
                val start = LocalDate.parse(event.startDate)
                val end = LocalDate.parse(event.endDate)
                !selectedDate.isBefore(start) && !selectedDate.isAfter(end)
            }

            if (selectedEvents.isNotEmpty()) {
                for ((index, event) in selectedEvents.withIndex()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable {
                                selectedEvent = event

                                eventText = event.title

                                eventStartDate =
                                    LocalDate.parse(event.startDate.ifBlank { event.date })
                                eventEndDate = LocalDate.parse(event.endDate.ifBlank { event.date })

                                selectedColorName = event.color.ifBlank { "tomato" }

                                eventLocation = event.location
                                eventNotes = event.notes
                                isAllDay = event.isAllDay

                                showAddEventDialog = true
                            }
                    ) {
                        val dotColor = getEventColor(event.color)

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(dotColor, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = if (event.isAllDay) {
                                "${event.title} • All day"
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

            val dialogTextColor = MaterialTheme.colorScheme.onSurface
            val dialogMutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            val dialogAccentColor = MaterialTheme.colorScheme.primary
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
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(20.dp)
                ) {

                    LaunchedEffect(selectedEvent) {
                        selectedEvent?.let {
                            eventText = it.title

                            selectedColorName = it.color.ifBlank { "tomato" }
                            isAllDay = it.isAllDay
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(getEventColor(selectedColorName), shape = CircleShape)
                                .clickable { showColorPicker = true }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = eventText,
                            onValueChange = {
                                eventText = it
                                eventErrorMessage = null
                            },
                            placeholder = { Text("Add title") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = eventLocation,
                        onValueChange = { eventLocation = it },
                        placeholder = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = eventNotes,
                        onValueChange = { eventNotes = it },
                        placeholder = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("All day", color = dialogTextColor)

                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Start date", color = dialogTextColor)
                        Text(
                            text = eventStartDate.format(dialogDateFormatter),
                            color = dialogAccentColor,
                            modifier = Modifier.clickable {
                                showStartDatePicker = true
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("End date")
                        Text(
                            text = eventEndDate.format(dialogDateFormatter),
                            color = dialogAccentColor,
                            modifier = Modifier.clickable {
                                showEndDatePicker = true
                            }
                        )
                    }

                    if (showColorPicker) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            listOf(
                                "tomato",
                                "flamingo",
                                "tangerine",
                                "banana",
                                "sage",
                                "basil",
                                "peacock",
                                "blueberry",
                                "lavender",
                                "grape",
                                "graphite"
                            ).forEach { colorName ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(getEventColor(colorName))
                                        .border(
                                            width = if (selectedColorName == colorName) 3.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedColorName = colorName
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isAllDay) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Start", color = dialogTextColor)

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
                            Text("End", color = dialogTextColor)

                            TextButton(onClick = { showEndPicker = true }) {
                                Text(
                                    "${endHour}:${
                                        endMinute.toString().padStart(2, '0')
                                    } ${if (isEndAM) "AM" else "PM"}"
                                )
                            }
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

                    eventErrorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            eventErrorMessage = null

                            if (eventText.isBlank()) {
                                eventErrorMessage = "Please enter an event title."
                                return@Button
                            }

                            if (eventEndDate.isBefore(eventStartDate)) {
                                eventErrorMessage =
                                    "Incorrect placement. Please place logical dates."
                                return@Button
                            }

                            val titleToSave = eventText
                            val locationToSave = eventLocation
                            val notesToSave = eventNotes
                            val startDateToSave = eventStartDate
                            val endDateToSave = eventEndDate
                            val colorToSave = selectedColorName
                            val allDayToSave = isAllDay
                            val editingEvent = selectedEvent

                            val selectedTimeStart = if (allDayToSave) "" else {
                                "${startHour}:${
                                    startMinute.toString().padStart(2, '0')
                                } ${if (isStartAM) "AM" else "PM"}"
                            }

                            val selectedTimeEnd = if (allDayToSave) "" else {
                                "${endHour}:${
                                    endMinute.toString().padStart(2, '0')
                                } ${if (isEndAM) "AM" else "PM"}"
                            }

                            showAddEventDialog = false
                            eventText = ""
                            eventLocation = ""
                            eventNotes = ""
                            selectedEvent = null

                            val user = FirebaseAuth.getInstance().currentUser
                            val uid = user?.uid
                            val db = FirebaseFirestore.getInstance()

                            if (uid != null) {
                                val eventData = hashMapOf(
                                    "title" to titleToSave,
                                    "date" to startDateToSave.toString(),
                                    "startDate" to startDateToSave.toString(),
                                    "endDate" to endDateToSave.toString(),
                                    "timeStart" to selectedTimeStart,
                                    "timeEnd" to selectedTimeEnd,
                                    "color" to colorToSave,
                                    "isAllDay" to allDayToSave,
                                    "location" to locationToSave,
                                    "notes" to notesToSave,
                                    "createdAt" to System.currentTimeMillis()
                                )

                                val eventsRef = db.collection("users")
                                    .document(uid)
                                    .collection("events")

                                val updatedEvent = Event(
                                    id = editingEvent?.id ?: "",
                                    title = titleToSave,
                                    date = startDateToSave.toString(),
                                    startDate = startDateToSave.toString(),
                                    endDate = endDateToSave.toString(),
                                    timeStart = selectedTimeStart,
                                    timeEnd = selectedTimeEnd,
                                    color = colorToSave,
                                    isAllDay = allDayToSave,
                                    location = locationToSave,
                                    notes = notesToSave,
                                    source = editingEvent?.source ?: "manual"
                                )

                                events = if (editingEvent != null) {
                                    events.mapValues { entry ->
                                        entry.value.map { event ->
                                            if (event.id == editingEvent.id) updatedEvent else event
                                        }
                                    }
                                } else {
                                    events + (startDateToSave to ((events[startDateToSave]
                                        ?: emptyList()) + updatedEvent))
                                }

                                if (editingEvent != null) {
                                    eventsRef.document(editingEvent.id)
                                        .set(eventData)
                                        .addOnSuccessListener {
                                            eventsFireBase { resultMap ->
                                                events = resultMap
                                            }
                                        }
                                } else {
                                    eventsRef.add(eventData)
                                        .addOnSuccessListener {
                                            eventsFireBase { resultMap ->
                                                events = resultMap
                                            }
                                        }
                                }
                            } else {
                                println("Save failed: uid=$uid, eventText='$eventText'")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save")
                    }

                    if (selectedEvent != null) {
                        TextButton(
                            onClick = {
                                val eventToDelete = selectedEvent

                                if (eventToDelete != null) {
                                    events = events.mapValues { entry ->
                                        entry.value.filter { it.id != eventToDelete.id }
                                    }.filterValues { it.isNotEmpty() }
                                }

                                selectedEvent = null
                                eventText = ""
                                eventLocation = ""
                                eventNotes = ""
                                eventErrorMessage = null
                                showAddEventDialog = false

                                val user = FirebaseAuth.getInstance().currentUser
                                val uid = user?.uid
                                val db = FirebaseFirestore.getInstance()

                                if (uid != null && eventToDelete != null) {
                                    db.collection("users")
                                        .document(uid)
                                        .collection("events")
                                        .document(eventToDelete.id)
                                        .delete()
                                        .addOnSuccessListener {
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
                        onClick = {
                            showAddEventDialog = false
                            selectedEvent = null
                            eventText = ""
                            eventLocation = ""
                            eventNotes = ""
                            isAllDay = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
                if (showStartDatePicker) {
                    DatePickerDialog(
                        onDateSelected = { date ->
                            eventStartDate = date
                            showStartDatePicker = false
                        },
                        onDismiss = { showStartDatePicker = false }
                    )
                }

                if (showEndDatePicker) {
                    DatePickerDialog(
                        onDateSelected = { date ->
                            eventEndDate = date
                            showEndDatePicker = false
                        },
                        onDismiss = { showEndDatePicker = false }
                    )
                }

            }
        }
    }
}






