package project.team36.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import project.team36.ui.findplants.FindPlantsScreen
import project.team36.ui.findplants.FindPlantsViewModel
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.myplants.MyPlantsScreen
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.plant.PlantDetailsScreen
import project.team36.ui.plant.PlantDetailsViewModel
import project.team36.ui.myplaces.MyPlacesScreen
import project.team36.ui.landingpage.LandingPageScreen
import project.team36.ui.landingpage.LandingPageViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

//Handles navigation for the whole screen
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    mapViewModel: MapViewModel,
    findPlantsViewModel: FindPlantsViewModel,
    myPlantsViewModel: MyPlantsViewModel,
    locationsViewModel: LocationsViewModel,
    plantDetailsViewModel: PlantDetailsViewModel,
    landingPageViewModel: LandingPageViewModel,
    isConnected: Boolean
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    modifier = Modifier
                        .padding(bottom = 105.dp)
                        .height(70.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = snackbarData.visuals.message)
                        IconButton(onClick = { snackbarData.dismiss() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Lukk",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "landingpage",
            modifier = modifier.padding(innerPadding)
        ){
            composable("map") {
                MapScreen(
                    modifier = modifier,
                    viewModel = mapViewModel,
                    navController = navController,
                    locationsViewModel = locationsViewModel
                )
            }
            composable ("myPlants"){
                MyPlantsScreen(
                    navController = navController,
                    locationsViewModel = locationsViewModel,
                    myPlantsViewModel = myPlantsViewModel,
                )
            }
            composable("findPlants/{locationName}") { backStackEntry ->
                val locationName = backStackEntry.arguments?.getString("locationName") ?: ""
                FindPlantsScreen(
                    findPlantsViewModel = findPlantsViewModel,
                    navController = navController,
                    locationsViewModel = locationsViewModel,
                    locationName = locationName
                )
            }
            composable("landingpage") {
                LandingPageScreen(
                    navController = navController,
                    locationsViewModel = locationsViewModel,
                    landingPageViewModel = landingPageViewModel,
                    myPlantsViewModel = myPlantsViewModel,
                    isConnected = isConnected
                )
            }
            composable ("plantDetailsScreen/{plantName}/{action}") { backStackEntry ->
                val plantName = backStackEntry.arguments?.getString("plantName") ?: return@composable
                val action = backStackEntry.arguments?.getString("action") ?: ""

                // Parses the action parameter to determine whether to load a new or saved plant
                LaunchedEffect(plantName) {
                    if (action == "add") {
                        plantDetailsViewModel.loadPlant(plantName)
                    } else {
                        plantDetailsViewModel.loadSavedPlant(plantName, myPlantsViewModel.plants.value)
                    }
                }

                PlantDetailsScreen(
                    navController = navController,
                    plantDetailsViewModel = plantDetailsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddPlant = if (action == "add") {
                        {
                            plantDetailsViewModel.plant.value?.let { plant ->
                                locationsViewModel.selectedLocation.value?.let { location ->
                                    findPlantsViewModel.addPlant(plant, location)
                                    navController.navigate("myPlants") {
                                        popUpTo("plantDetailsScreen/${plant.name}/add") { inclusive = true }
                                    }
                                }
                            }
                        }
                    } else null,
                    onDeletePlant = if (action == "delete") {
                        { myPlantsViewModel.removePlantByName(plantName) }
                    } else null
                )
            }
            composable("myPlaces") {
                MyPlacesScreen(
                    navController = navController,
                    locationsViewModel = locationsViewModel,
                    onLocationCardClick = { navController.navigate("myPlants") },
                    onAddPlaceClick = { navController.navigate("map") },
                )
            }
        }
    }
}