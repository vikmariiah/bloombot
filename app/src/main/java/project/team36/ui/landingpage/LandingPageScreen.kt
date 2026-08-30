package project.team36.ui.landingpage

import android.Manifest
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.navigation.BottomNavBar
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.edit
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.data.mcp.WeatherAlert
import project.team36.R
import project.team36.ui.myplaces.EmptyLocationCard

/*Opening screen for the app. On first time open, a guide is opened that can later be accessed as well.
* Shows weather for location and plants that need water, as well as AI-button.
*/
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LandingPageScreen(
    navController: NavController,
    locationsViewModel: LocationsViewModel,
    landingPageViewModel: LandingPageViewModel,
    myPlantsViewModel: MyPlantsViewModel,
    isConnected: Boolean
) {

    val uiState by landingPageViewModel.uiState.collectAsStateWithLifecycle()
    val savedLocations by locationsViewModel.savedLocations.collectAsStateWithLifecycle()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showNoNetworkDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    //tracks whether the user has seen the onboarding info or not
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    //shows onboarding dialog automatically on first launch
    LaunchedEffect(Unit) {
        if (!prefs.getBoolean("has_seen_intro", false)) {
            landingPageViewModel.showInfoDialog()
            prefs.edit { putBoolean("has_seen_intro", true) }
        }
    }

    //Updates the UI based on internet connection automatically
    LaunchedEffect(isConnected) {
        if (isConnected and savedLocations.isNotEmpty()) {
            landingPageViewModel.loadDataForLocations(savedLocations)
        }
    }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    //loads again which plants need watering when the list of savedlocations changes
    val plantsByLocation by remember(savedLocations) {
        myPlantsViewModel.getPlantsNeedingWaterByLocation(savedLocations)
    }.collectAsStateWithLifecycle(initialValue = emptyMap())

    //requests location permission
    LaunchedEffect(Unit) { locationPermission.launchPermissionRequest() }

    /*cancels launchedEffect block early if there are no saved locations
    * fetches data for saved locations
    * sets selectedLocation to first tab when the screen loads in,
    * so recommended plants can display for this garden right away
    * And loads precipitation for location
    */
    LaunchedEffect(savedLocations) {
        if (savedLocations.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }
        landingPageViewModel.loadDataForLocations(savedLocations)
        if (locationsViewModel.selectedLocation.value == null) {
            locationsViewModel.selectLocation(savedLocations[0])
        }
        val location = locationsViewModel.selectedLocation.value ?: savedLocations[0]
        val lat = location.lat ?: return@LaunchedEffect
        val lon = location.lon ?: return@LaunchedEffect
        myPlantsViewModel.loadPrecipitationForLocation(lat, lon)
        isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hjem",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { landingPageViewModel.showInfoDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Guide",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0) //removes extra space on top of the screen
            )
        },
        bottomBar = { BottomNavBar(navController = navController, locationsViewModel) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            //Show loading indicator while data from database is loading
            if (isLoading){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            //Show an empty card if no places are saved, that directs to map
            else if (savedLocations.isEmpty()) {
                EmptyLocationCard (
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onAddPlaceClick = { navController.navigate("map") }
                )
            } else {
                //horizontally scrollable tab row, for saved locations
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                    divider = {},
                    indicator = {}
                ) {
                    savedLocations.forEachIndexed { index, location ->
                        val alerts = uiState.locationAlerts[location.id] ?: emptyList()
                        val hasAlert = alerts.isNotEmpty()
                        //uses the alerts own color, fallback to red
                        val alertColor = alerts.firstOrNull()?.color
                            ?: Color.Red
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index; locationsViewModel.selectLocation(
                                location
                            ) },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(40.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.tertiary,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                ),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = location.name ?: "Hage",
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                                        else MaterialTheme.colorScheme.onTertiary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    //if the location has an alert, shows a warning icon on the tab
                                    if (hasAlert) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = alertColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                val selectedLocation =
                    savedLocations.getOrNull(selectedTabIndex) ?: savedLocations[0]
                val alertsForSelectedLocation: List<WeatherAlert> =
                    uiState.locationAlerts[selectedLocation.id] ?: emptyList()

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    //weatherAlerts banner for selected location
                    if (alertsForSelectedLocation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column {
                            alertsForSelectedLocation.forEach { alert ->
                                LocationAlertBanner(alert)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    WeatherCard(
                        weatherUiState = uiState.locationForecasts[selectedLocation.id],
                        isLoading = uiState.isWeatherLoading,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        //ai-advice button
                        Button(
                            onClick = {
                                if (isConnected) {
                                    landingPageViewModel.fetchAiAdvice(selectedLocation)
                                } else {
                                    showNoNetworkDialog = true
                                }
                            },
                            modifier = Modifier
                                .wrapContentWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6DBDD7),
                                            Color(0xFF69CE64)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            //is disabled when a request is in progress
                            enabled = !uiState.isAiLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            shape = CircleShape
                        ) {
                            if (uiState.isAiLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.lightbulb),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Få råd fra Bloombot",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }


                    //dialogbox that displays the ai-advice response
                    if (uiState.aiResponse != null) {
                        AlertDialog(
                            onDismissRequest = { landingPageViewModel.dismissAiAdvice() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.bloombot_yap),
                                        contentDescription = "Bloombot",
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bloombot sier:", fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (uiState.aiResponse!!.plantAdvice.isNotEmpty()) {
                                        Column {
                                            Text(
                                                "Dine planter:",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            val recommendText =
                                                uiState.aiResponse!!.plantAdvice.firstOrNull()?.recommend
                                            if (!recommendText.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = recommendText,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                            }
                                            uiState.aiResponse!!.plantAdvice.forEach { advice ->
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "• ${advice.plantName}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    advice.advice,
                                                    modifier = Modifier.padding(start = 12.dp),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                    if (uiState.aiResponse!!.generalTasks.isNotEmpty()) {
                                        Column {
                                            Text(
                                                "Hageoppgaver:",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            uiState.aiResponse!!.generalTasks.forEach { task ->
                                                Text(
                                                    "- $task",
                                                    modifier = Modifier.padding(start = 4.dp),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { landingPageViewModel.dismissAiAdvice() }) {
                                    Text("Lukk", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        )
                    }
                    // AlertDialog for if there is no internet connection.
                    if (showNoNetworkDialog) {
                        AlertDialog(
                            onDismissRequest = { showNoNetworkDialog = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.bloombot_yap),
                                        contentDescription = "Bloombot",
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bloombot sier:", fontWeight = FontWeight.Bold)
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    Column {
                                        Text(
                                            "Bloombot trenger tilgang til internett for å kunne gi deg tips om hagen. Vennligst sjekk tilkoblingen din.",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showNoNetworkDialog = false }) {
                                    Text("Lukk", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //list of plants that need watering today, grouped by location
                    DailyTasksCard(
                        plantsByLocation = plantsByLocation,
                        onMarkAsWatered = { myPlantsViewModel.markAsWatered(it) }
                    )
                }
            }
        }
    }

    //onboarding dialog, shown on first launch or when info button is clicked
    //short intro for the app
    if (uiState.showInfoDialog) {
        OnboardingDialog(
            onDismiss = { landingPageViewModel.dismissInfoDialog() },
            onFinish = {
                landingPageViewModel.dismissInfoDialog()
                navController.navigate("myPlaces")
            }
        )
    }
}

//metAlerts banner for selected location, shows when there is an active alert for that location
@Composable
fun LocationAlertBanner(alert: WeatherAlert) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(alert.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, alert.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = alert.color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text(
                text = alert.event.uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 0.5.sp
            )
            Text(
                text = alert.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 18.sp
            )
        }
    }
}