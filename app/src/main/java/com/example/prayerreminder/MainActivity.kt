
package com.example.prayerreminder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prayerreminder.model.PrayerTime
import com.example.prayerreminder.ui.theme.PrayerTheme
import com.example.prayerreminder.vm.PrayerVM
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {

    private lateinit var fused: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fused = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            PrayerTheme {
                Surface(Modifier.fillMaxSize()) {
                    val vm: PrayerVM = viewModel(factory = PrayerVM.Factory(application))
                    MainScreen(vm = vm, requestLocation = { getLocation(vm) }, requestExactAlarms = { openExactAlarmSettings() })
                }
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${packageName}")
            }
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(vm: PrayerVM) {
        val cts = CancellationTokenSource()
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) vm.loadTimings(loc.latitude, loc.longitude)
            }
    }
}

@Composable
fun MainScreen(vm: PrayerVM, requestLocation: () -> Unit, requestExactAlarms: () -> Unit) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    var needLocation by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) { requestLocation(); needLocation = false }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Prayer Reminder") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.scheduleTodayReminders(ctx) },
                text = { Text("Enable today's reminders") }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp).fillMaxSize()) {
            if (needLocation && state.location == null) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Get times by your current location", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("We use the Umm al-Qura (Makkah) method via AlAdhan API.")
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                permissionLauncher.launch(arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            }) { Text("Use my location") }
                            OutlinedButton(onClick = requestExactAlarms) { Text("Exact alarms settings") }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            when {
                state.loading -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                state.timings.isNotEmpty() -> {
                    Text("Today's prayers", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Lat: ${"%.4f".format(state.location?.first ?: 0.0)}, Lng: ${"%.4f".format(state.location?.second ?: 0.0)}")
                    Spacer(Modifier.height(16.dp))
                    if (state.next != null) {
                        Text("Next: ${state.next!!.name} at ${state.next!!.timeDisplay}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                    }
                    LazyColumn(Modifier.fillMaxWidth()) {
                        items(state.timings) { p: PrayerTime ->
                            ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(p.name)
                                    Text(p.timeDisplay, fontWeight = if (p == state.next) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
                else -> { Text("Tap “Use my location” to load your timings.") }
            }
        }
    }
}
