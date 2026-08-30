package project.team36.ui.findplants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.plant.SimplePlantCard
import project.team36.ui.navigation.BottomNavBar

/*
Screen that shows recommended and not-recommended plants based on climate zones.
User can change chosen location to get different recommendations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPlantsScreen(
    findPlantsViewModel: FindPlantsViewModel,
    navController: NavController,
    locationsViewModel: LocationsViewModel,
    locationName: String
) {

    val plants by findPlantsViewModel.plants.collectAsState()
    val notRecommendedPlants by findPlantsViewModel.notRecommendedPlants.collectAsState()
    val savedLocations by locationsViewModel.savedLocations.collectAsState()
    val selectedLocation by locationsViewModel.selectedLocation.collectAsState()
    val showSwitchLocationDialog = rememberSaveable { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Anbefalte planter", "Andre planter")

    // Loads in recommended plants
    LaunchedEffect(selectedLocation, savedLocations) {
        if (savedLocations.isEmpty() || selectedLocation == null) {
            findPlantsViewModel.clearPlants()
        } else {
            selectedLocation?.let { findPlantsViewModel.loadRecommendedPlants(it) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Finn nye planter",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0)
            )
        },
        bottomBar = { BottomNavBar(navController = navController, locationsViewModel) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (selectedLocation == null || savedLocations.isEmpty() ) {
                    Text(
                        text = "Legg til eller velg en hage for å få tilpassede planteanbefalinger🌱",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                                        shape = CircleShape
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                text = {
                                    Text(
                                        text = title,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                    val location = selectedLocation
                    val infoText = when (selectedTabIndex) {
                        0 -> "Planter som egner seg for hagens herdighetssone og vokseforhold. Din herdighetssone for ${location?.name} er ${location?.zone}, dermed vil planter med sone ${location?.zone} til 8 trives godt her🌱"
                        1 -> "Disse plantene egner seg ikke like godt for din hage, men kan likevel trives om du har et lunt hjørne plantene kan vokse i."
                        else -> ""
                    }

                    val displayedPlants = if (selectedTabIndex == 0) plants else notRecommendedPlants

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 100.dp
                        )
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = infoText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        // Shows plants that fit or do not fit
                        items(displayedPlants) { plant ->
                            SimplePlantCard(
                                plant = plant,
                                onNavigateToPlantDetails = {
                                    navController.navigate("plantDetailsScreen/${plant.name}/add")
                                }
                            )
                        }
                    }
                }
            }

            // Chip + FAB floating over content
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Hage", tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedLocation?.name ?: locationName.ifEmpty { "Ingen hage valgt" },
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Button to change garden
                ExtendedFloatingActionButton(
                    onClick = { showSwitchLocationDialog.value = true },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Bytt hage") },
                    text = { Text("Bytt hage") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(50)
                )
            }
        }
    }

    if (showSwitchLocationDialog.value) {
        SwitchLocationDialog(
            savedLocations = savedLocations,
            currentLocationName = locationName,
            onConfirm = { location ->
                locationsViewModel.selectLocation(location)
                navController.navigate("findPlants/${location.name}")
            },
            onDismiss = { showSwitchLocationDialog.value = false },
            onNavigateToMap = { navController.navigate("map") }
        )
    }
}