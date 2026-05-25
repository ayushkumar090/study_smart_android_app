@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.lets_party_hizrugang

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView // NEW IMPORT FOR WEBVIEW
import com.example.lets_party_hizrugang.ui.theme.Lets_Party_HizrugangTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

// --- GLOBAL COLORS ---
val neonBlue = Color(0xFF00E5FF)
val deepBlue = Color(0xFF3F51B5)
val primaryBlue = Color(0xFF007AFF)
val cardOutline = Color(0xFFE5E5EA)
val bgLight = Color(0xFFF8FAFF)

val colorStreak = Color(0xFFFFD54F)
val colorStudied = Color(0xFF00E676)
val colorMissed = Color(0xFFFF5252)
val colorBreak = Color(0xFF448AFF)

// A helper to manage the phone's Do Not Disturb state
fun toggleDoNotDisturb(context: Context, enableDND: Boolean) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Check if the user has granted us permission to change DND settings
    if (notificationManager.isNotificationPolicyAccessGranted) {
        if (enableDND) {
            // Turns ON DND (Priority mode allows alarms and important calls)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } else {
            // Turns OFF DND (Back to normal)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    } else {
        // If we don't have permission, open the Android Settings screen to ask for it!
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        ContextCompat.startActivity(context, intent, null)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lets_Party_HizrugangTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    StudyAppNavigation()
                }
            }
        }
    }
}

// --- NAVIGATION HANDLER ---
@Composable
fun StudyAppNavigation() {
    var currentScreen by remember { mutableStateOf("Auth") }
    var finalSubjects by remember { mutableStateOf(listOf<String>()) }
    var studyMinsPerSubject by remember { mutableStateOf(0) }

    when (currentScreen) {
        "Auth" -> AuthScreen(onLoginSuccess = { currentScreen = "Dashboard" })
        "Dashboard" -> DashboardScreen(
            onNavigateToPlanner = { currentScreen = "Setup" },
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                currentScreen = "Auth"
            }
        )
        "Setup" -> StudySetupScreen(
            onStartTimer = { subjects, mins ->
                finalSubjects = subjects
                studyMinsPerSubject = mins
                currentScreen = "Timer"
            },
            onBack = { currentScreen = "Dashboard" }
        )
        "Timer" -> ActiveTimerScreen(
            subjects = finalSubjects,
            studyMins = studyMinsPerSubject,
            onStop = { currentScreen = "Dashboard" }
        )
    }
}

// --- AUTHENTICATION SCREEN ---
@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = remember { FirebaseAuth.getInstance() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.my_app_logo), contentDescription = "App Logo", modifier = Modifier.size(120.dp).padding(bottom = 16.dp))
        Text(text = if (isLoginMode) "Welcome Back" else "Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = email, onValueChange = { email = it; errorMessage = null }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it; errorMessage = null }, label = { Text("Password (Min 6 chars)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let { Text(text = it, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center); Spacer(modifier = Modifier.height(8.dp)) }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.length >= 6) {
                    isLoading = true
                    if (isLoginMode) {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) onLoginSuccess() else errorMessage = task.exception?.localizedMessage ?: "Login failed"
                        }
                    } else {
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) onLoginSuccess() else errorMessage = task.exception?.localizedMessage ?: "Registration failed"
                        }
                    }
                } else { errorMessage = "Please enter a valid email and a 6+ character password." }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryBlue), enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text(if (isLoginMode) "Login" else "Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { isLoginMode = !isLoginMode; errorMessage = null }) { Text(if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login", color = Color.Gray) }
    }
}

// --- DASHBOARD SCREEN & DATA MODELS ---
data class StudySession(val durationMins: Float, val timestamp: Long, val subject: String)

@Composable
fun DashboardScreen(onNavigateToPlanner: () -> Unit, onLogout: () -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var bottomNavIndex by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var totalHours by remember { mutableStateOf("0") }
    var chartDataPoints by remember { mutableStateOf(listOf<Float>()) }
    var totalSessions by remember { mutableStateOf("0") }
    var sessionHistory by remember { mutableStateOf(emptyList<StudySession>()) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/sessions")
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalMins = 0f
                    val points = mutableListOf<Float>()
                    val historyList = mutableListOf<StudySession>()

                    for (session in snapshot.children) {
                        val mins = session.child("durationMins").getValue(Float::class.java) ?: 0f
                        val time = session.child("timestamp").getValue(Long::class.java) ?: 0L
                        val subject = session.child("subject").getValue(String::class.java) ?: "General Study"

                        totalMins += mins
                        points.add(mins)
                        historyList.add(StudySession(mins, time, subject))
                    }
                    totalHours = String.format("%.1f", totalMins / 60f)
                    totalSessions = points.size.toString()
                    chartDataPoints = points.takeLast(10).ifEmpty { listOf(0f) }
                    sessionHistory = historyList.sortedByDescending { it.timestamp }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Spacer(Modifier.height(36.dp))
                Image(painter = painterResource(id = R.drawable.my_app_logo), contentDescription = "Logo", modifier = Modifier.size(80.dp).padding(start = 16.dp))
                Text("Study Smart", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Profile Settings") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.AccountCircle, "Profile") },
                    modifier = Modifier.padding(12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Help & Support") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Info, "Help") },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("STUDY SMART", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = { IconButton(onClick = onLogout) { Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    NavigationBarItem(selected = bottomNavIndex == 0, onClick = { bottomNavIndex = 0 }, icon = { Icon(Icons.Default.Home, contentDescription = "Home") })
                    NavigationBarItem(selected = bottomNavIndex == 1, onClick = { bottomNavIndex = 1 }, icon = { Icon(Icons.Default.Search, contentDescription = "Search") })
                    NavigationBarItem(selected = bottomNavIndex == 2, onClick = { bottomNavIndex = 2 }, icon = { Icon(Icons.Default.Refresh, contentDescription = "Sync") })
                    NavigationBarItem(selected = bottomNavIndex == 3, onClick = { bottomNavIndex = 3 }, icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") })
                }
            },
            floatingActionButton = {
                if (bottomNavIndex == 0) {
                    FloatingActionButton(onClick = onNavigateToPlanner, containerColor = primaryBlue, contentColor = Color.White) { Icon(Icons.Default.Add, contentDescription = "Plan Study") }
                }
            },
            containerColor = Color.White
        ) { paddingValues ->

            when (bottomNavIndex) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardTab("Stats", isActive = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 })
                            DashboardTab("History", isActive = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 })
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        if (selectedTabIndex == 0) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    StatCard(title = "Total Hours Studied", value = "$totalHours hrs", subtext = "All Time", modifier = Modifier.weight(1f))
                                    StatCard(title = "Total Sessions", value = totalSessions, subtext = "Completed", modifier = Modifier.weight(0.8f))
                                }
                                Spacer(modifier = Modifier.height(24.dp))

                                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, cardOutline), colors = CardDefaults.outlinedCardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Recent Study Activity (Mins)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        LineChartDynamic(dataPoints = chartDataPoints)
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))

                                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, cardOutline), colors = CardDefaults.outlinedCardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Your Subjects", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SubjectListItem("Data Communication")
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SubjectListItem("Operating System")
                                    }
                                }
                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        } else {
                            if (sessionHistory.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No study sessions recorded yet.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(sessionHistory) { session ->
                                        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                                        val dateString = sdf.format(Date(session.timestamp))

                                        OutlinedCard(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            colors = CardDefaults.outlinedCardColors(containerColor = bgLight),
                                            border = BorderStroke(1.dp, cardOutline)
                                        ) {
                                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(session.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = deepBlue)
                                                    Text(dateString, fontSize = 12.sp, color = Color.Gray)
                                                }
                                                Text("${String.format("%.0f", session.durationMins)} mins", fontWeight = FontWeight.Black, fontSize = 18.sp, color = primaryBlue)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> YouTubeSearchScreen(paddingValues) // NEW YOUTUBE IN-APP SEARCH
                2 -> PlaceholderScreen("Sync / Compare Screen", Icons.Default.Refresh, paddingValues)
                3 -> CalendarStreakScreen(sessionHistory, paddingValues)
            }
        }
    }
}

// --- NEW IN-APP YOUTUBE BROWSER ---
@Composable
fun YouTubeSearchScreen(paddingValues: PaddingValues) {
    var searchQuery by remember { mutableStateOf("") }
    // Start by loading the YouTube mobile homepage
    var urlToLoad by remember { mutableStateOf("https://m.youtube.com") }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        // Search Bar Area
        Surface(shadowElevation = 4.dp, color = Color.White) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Educational Videos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotEmpty()) {
                            // Format the search query correctly for a URL
                            val formattedQuery = searchQuery.replace(" ", "+")
                            urlToLoad = "https://m.youtube.com/results?search_query=$formattedQuery"
                        }
                    }
                )
            )
        }

        // The In-App Browser (WebView)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // Enable JavaScript so YouTube actually runs and plays videos
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    // This forces all clicks to happen INSIDE our app, instead of opening Chrome
                    webViewClient = WebViewClient()
                }
            },
            update = { webView ->
                // Whenever urlToLoad changes (like when they hit search), load the new page
                webView.loadUrl(urlToLoad)
            }
        )
    }
}

// --- CALENDAR STREAK SCREEN ---
@Composable
fun CalendarStreakScreen(history: List<StudySession>, paddingValues: PaddingValues) {
    var selectedDateStr by remember { mutableStateOf("") }
    var monthOffset by remember { mutableIntStateOf(0) }

    val sdfDateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sessionsByDate = history.groupBy { sdfDateOnly.format(Date(it.timestamp)) }

    val realTodayCal = Calendar.getInstance()
    val todayStr = sdfDateOnly.format(realTodayCal.time)
    var currentStreak = 0
    val streakDates = mutableSetOf<String>()

    for (i in 0..30) {
        val checkCal = Calendar.getInstance()
        checkCal.add(Calendar.DAY_OF_MONTH, -i)
        val checkStr = sdfDateOnly.format(checkCal.time)
        if (sessionsByDate.containsKey(checkStr)) {
            currentStreak++
            streakDates.add(checkStr)
        } else if (i != 0) {
            break
        }
    }

    val displayCal = Calendar.getInstance()
    displayCal.add(Calendar.MONTH, monthOffset)
    displayCal.set(Calendar.DAY_OF_MONTH, 1)

    val maxDays = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK) - 1
    val monthPrefix = SimpleDateFormat("yyyy-MM-", Locale.getDefault()).format(displayCal.time)
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayCal.time)

    val rawGridItems = MutableList(firstDayOfWeek) { 0 } + (1..maxDays).toList()
    val paddedWeeks = rawGridItems.chunked(7).map { week ->
        if (week.size < 7) week + List(7 - week.size) { 0 } else week
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { monthOffset-- }) { Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(monthName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = deepBlue)
                Text("Current Streak: $currentStreak Days \uD83D\uDD25", fontSize = 14.sp, color = if (currentStreak > 0) colorStreak else Color.Gray)
            }
            IconButton(onClick = { monthOffset++ }, enabled = monthOffset < 0) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month", tint = if (monthOffset < 0) Color.Black else Color.Transparent)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            LegendItem(colorStudied, "Studied")
            LegendItem(colorStreak, "Streak")
            LegendItem(colorBreak, "Break")
            LegendItem(colorMissed, "Missed")
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = Color.White), border = BorderStroke(1.dp, cardOutline)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                paddedWeeks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        week.forEachIndexed { dayOfWeek, dayNum ->
                            if (dayNum == 0) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                            } else {
                                val dateStr = monthPrefix + String.format("%02d", dayNum)
                                val hasStudied = sessionsByDate.containsKey(dateStr)
                                val isStreak = streakDates.contains(dateStr)
                                val isWeekend = dayOfWeek == 0 || dayOfWeek == 6
                                val isFuture = dateStr > todayStr
                                val isToday = dateStr == todayStr

                                val bgColor = when {
                                    isFuture -> Color(0xFFF0F0F5)
                                    isStreak -> colorStreak
                                    hasStudied -> colorStudied
                                    isWeekend -> colorBreak
                                    else -> colorMissed
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .border(if (isToday) 2.dp else 0.dp, if (isToday) Color.Black else Color.Transparent, CircleShape)
                                        .clickable { if (!isFuture) selectedDateStr = dateStr },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isStreak) {
                                        Icon(Icons.Default.Star, contentDescription = "Trophy", tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(dayNum.toString(), color = if (isFuture) Color.Gray else Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedDateStr.isNotEmpty()) {
            Text("Details for $selectedDateStr", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = deepBlue)
            Spacer(modifier = Modifier.height(8.dp))

            val daySessions = sessionsByDate[selectedDateStr]
            if (daySessions.isNullOrEmpty()) {
                Text("No study sessions recorded. Rest day!", color = Color.Gray)
            } else {
                daySessions.forEach { session ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.outlinedCardColors(containerColor = bgLight)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(session.subject, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.0f", session.durationMins)} mins", color = primaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.Gray)
    }
}

// --- SUPPORTING UI COMPONENTS ---
@Composable
fun DashboardTab(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isActive) primaryBlue else Color(0xFFF0F0F5)).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 8.dp)) { Text(text, color = if (isActive) Color.White else Color.Black, fontWeight = FontWeight.Medium) }
}

@Composable
fun PlaceholderScreen(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, paddingValues: PaddingValues) {
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("Coming soon...", fontSize = 14.sp, color = Color.LightGray)
    }
}

@Composable
fun StatCard(title: String, value: String, subtext: String, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, cardOutline), colors = CardDefaults.outlinedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtext, fontSize = 12.sp, color = Color(0xFF00BFA5), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SubjectListItem(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Column { Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("Know more>", fontSize = 14.sp, color = Color.Gray) }
    }
}

@Composable
fun LineChartDynamic(dataPoints: List<Float>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val padding = 40f

        for (i in 0..5) {
            val y = height - padding - (i * (height - padding * 2) / 5)
            drawLine(color = Color(0xFFF0F0F5), start = Offset(padding, y), end = Offset(width, y), strokeWidth = 2f)
        }

        if (dataPoints.isEmpty()) return@Canvas

        val maxDataValue = dataPoints.maxOrNull() ?: 10f
        val yMax = if (maxDataValue == 0f) 10f else maxDataValue
        val xStep = (width - padding * 2) / maxOf(dataPoints.size - 1, 1)
        val path = Path()
        var lastPoint = Offset(0f, 0f)

        dataPoints.forEachIndexed { index, value ->
            val x = padding + (index * xStep)
            val y = height - padding - ((value / yMax) * (height - padding * 2))
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            lastPoint = Offset(x, y)
        }

        drawPath(path = path, color = primaryBlue, style = Stroke(width = 6f, cap = StrokeCap.Round))
        drawCircle(color = primaryBlue, radius = 10f, center = lastPoint)
    }
}

@Composable
fun StudySetupScreen(onStartTimer: (List<String>, Int) -> Unit, onBack: () -> Unit) {
    var hours by remember { mutableStateOf("") }
    var subjectCountStr by remember { mutableStateOf("") }
    val subjectNames = remember { mutableStateListOf<String>() }
    var showResults by remember { mutableStateOf(false) }
    val suggestions = listOf("Data Structures", "Operating Systems", "Data Communication", "Database Management", "Algorithms")

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        Image(painter = painterResource(id = R.drawable.my_app_logo), contentDescription = "App Logo", modifier = Modifier.size(90.dp).padding(bottom = 8.dp))
        Text(text = "Study Plan", fontSize = 34.sp, fontWeight = FontWeight.Black, color = deepBlue, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(value = hours, onValueChange = { hours = it; showResults = false }, label = { Text("Total Hours") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = subjectCountStr, onValueChange = { subjectCountStr = it; val count = it.toIntOrNull() ?: 0; subjectNames.clear(); repeat(count) { subjectNames.add("") }; showResults = false }, label = { Text("Number of Subjects") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(subjectNames) { index, currentName ->
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.padding(vertical = 4.dp)) {
                    OutlinedTextField(value = currentName, onValueChange = { subjectNames[index] = it; expanded = it.isNotEmpty() }, label = { Text("Subject ${index + 1}") }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    val filteredOptions = suggestions.filter { it.contains(currentName, ignoreCase = true) }
                    if (filteredOptions.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            filteredOptions.forEach { selectionOption -> DropdownMenuItem(text = { Text(selectionOption) }, onClick = { subjectNames[index] = selectionOption; expanded = false }) }
                        }
                    }
                }
            }
        }

        Button(onClick = { showResults = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(60.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(deepBlue, neonBlue))), contentAlignment = Alignment.Center) { Text("Finalize Plan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }

        if (showResults) {
            val h = hours.toIntOrNull() ?: 0
            val s = subjectNames.size
            if (s > 0 && h > 0) {
                val mins = (h * 60 - (s - 1) * 15) / s
                Button(onClick = { onStartTimer(subjectNames.filter { it.isNotEmpty() }, mins) }, colors = ButtonDefaults.buttonColors(containerColor = primaryBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Start Now ▶", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
        }
    }
}

// --- TIMER SCREEN WITH FIREBASE SAVING & DND ---
@Composable
fun ActiveTimerScreen(subjects: List<String>, studyMins: Int, onStop: () -> Unit) {
    val context = LocalContext.current // Grab the context for the DND helper

    var currentSubjectIndex by remember { mutableStateOf(0) }
    var isBreak by remember { mutableStateOf(false) }
    val initialSeconds = studyMins * 60
    var timeLeftSeconds by remember { mutableStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(true) }

    // --- NEW: DND AUTOMATION ---
    // Automatically turn ON DND when studying, and OFF when on a break or finished.
    LaunchedEffect(isRunning, isBreak) {
        val shouldMute = isRunning && !isBreak
        toggleDoNotDisturb(context, enableDND = shouldMute)
    }

    // --- NEW: DND SAFETY NET ---
    // If the user force-closes the app or swipes back, guarantee DND turns off!
    DisposableEffect(Unit) {
        onDispose {
            toggleDoNotDisturb(context, enableDND = false)
        }
    }

    fun saveSessionToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/sessions")
            val minsCompleted = (initialSeconds - timeLeftSeconds) / 60f

            if (minsCompleted > 0f && !isBreak) {
                val currentSubject = subjects.getOrNull(currentSubjectIndex) ?: "General Study"
                val sessionData = mapOf(
                    "durationMins" to minsCompleted,
                    "timestamp" to System.currentTimeMillis(),
                    "subject" to currentSubject
                )
                dbRef.push().setValue(sessionData)
            }
        }
    }

    LaunchedEffect(key1 = timeLeftSeconds, key2 = isRunning) {
        if (isRunning && timeLeftSeconds > 0) {
            delay(1000L)
            timeLeftSeconds--
        } else if (isRunning && timeLeftSeconds <= 0) {
            saveSessionToFirebase()
            if (!isBreak) {
                if (currentSubjectIndex < subjects.size - 1) {
                    isBreak = true
                    timeLeftSeconds = 15 * 60 // 15 minute break
                } else {
                    isRunning = false
                }
            } else {
                isBreak = false
                currentSubjectIndex++
                timeLeftSeconds = initialSeconds
            }
        }
    }

    val timeString = String.format("%02d:%02d", timeLeftSeconds / 60, timeLeftSeconds % 60)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = if (isRunning) "Focus Mode Active" else "Session Complete!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(40.dp))

        Box(modifier = Modifier.size(250.dp).background(brush = Brush.radialGradient(colors = if (isBreak) listOf(Color(0xFF00E676), Color.Transparent) else listOf(neonBlue.copy(alpha = 0.2f), Color.Transparent)), shape = CircleShape), contentAlignment = Alignment.Center) {
            Text(text = timeString, fontSize = 64.sp, fontWeight = FontWeight.Black, color = deepBlue)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(text = if (isBreak) "Relax ☕" else subjects.getOrNull(currentSubjectIndex) ?: "", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = if (isBreak) Color(0xFF00C853) else deepBlue)
        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {
                saveSessionToFirebase()
                // DND will automatically turn off thanks to the DisposableEffect!
                onStop()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
        ) {
            Text("End Session", color = Color.White)
        }
    }
}