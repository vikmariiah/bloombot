package project.team36.ui.myplants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.plant.PlantCard
import project.team36.ui.navigation.BottomNavBar

//Screen to display all plants per location
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen(
    navController: NavController,
    locationsViewModel: LocationsViewModel,
    myPlantsViewModel: MyPlantsViewModel,
) {

    val selectedLocation by locationsViewModel.selectedLocation.collectAsStateWithLifecycle()

    //fetches plants for a savedlocation
    val plants by myPlantsViewModel.plants.collectAsStateWithLifecycle()

    //reloads rain data whenever selected location changes
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            val lat = it.lat ?: return@let
            val lon = it.lon ?: return@let
            myPlantsViewModel.loadPrecipitationForLocation(lat, lon)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedLocation?.name ?: "Mine planter",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("myPlaces")} ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    //ui state for edit dropdown
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
                    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
                    var newLocationName by rememberSaveable { mutableStateOf("") }

                    Box {
                        IconButton(onClick = {expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rediger hage",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false},
                            containerColor = MaterialTheme.colorScheme.background
                        ) {
                            DropdownMenuItem(
                                text = { Text("Bytt navn") },
                                onClick = {
                                    expanded = false
                                    newLocationName = selectedLocation?.name ?: ""
                                    showRenameDialog = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Slett hage") },
                                onClick = {
                                    showDeleteDialog = true
                                    expanded = false
                                }
                            )
                        }
                        //delete confirmation dialogbox
                        if (showDeleteDialog) {
                            AlertDialog(
                                title = { Text("Ønsker du å slette hagen?") },
                                text = { Text("Du vil miste alle lagrede planter i denne hagen.") },
                                onDismissRequest = { showDeleteDialog = false},
                                confirmButton = {
                                    OutlinedButton (
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        onClick = {
                                            selectedLocation?.let { myPlantsViewModel.deleteLocation(it) }
                                            navController.navigate("myPlaces")
                                            locationsViewModel.updateSelectedLocationName("Ingen hage valgt")
                                            showDeleteDialog = false
                                        }
                                    ) { Text("Slett") }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = { showDeleteDialog = false },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) { Text("Behold hage", color = Color.White) }
                                },
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        }
                        //renamedialogbox
                        if (showRenameDialog) {
                            AlertDialog(
                                title = { Text("Bytt navn på hagen") },
                                containerColor = MaterialTheme.colorScheme.surface,
                                text = {
                                    OutlinedTextField(
                                        value = newLocationName,
                                        onValueChange = { newLocationName = it },
                                        label = { Text("Nytt navn") },
                                        singleLine = true
                                    )
                                },
                                onDismissRequest = { showRenameDialog = false },
                                confirmButton = {
                                    OutlinedButton(
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        onClick = {
                                            selectedLocation?.let {
                                                myPlantsViewModel.renameLocation(it, newLocationName)
                                            }
                                            locationsViewModel.updateSelectedLocationName(newLocationName)
                                            showRenameDialog = false
                                        },
                                        enabled = newLocationName.isNotBlank()
                                    ) { Text("Lagre") }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        onClick =  { showRenameDialog = false }
                                    ) {
                                        Text("Avbryt")
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0) //removes exra space at the top of the screen
            )
        },

        bottomBar = { BottomNavBar(navController = navController, locationsViewModel)},

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate("findPlants/${selectedLocation?.name 
                        ?: "Ingen hage valgt"}") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Ny hage") },
                text = { Text("Ny plante") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //if garden has no plants yet, shows emptyplantcard
            if (plants.isEmpty()) {
                item {
                    EmptyPlantCard(
                        onClick = {
                            navController.navigate("findPlants/${selectedLocation?.name 
                            ?: "Ingen hage valgt"}")}
                    )
                }
            } else {
                items(plants){ plant ->
                    PlantCard(
                        plant = plant,
                        needsWater = myPlantsViewModel.needsWater(plant),
                        daysUntilWatering = myPlantsViewModel.daysUntilWatering(plant),
                        daysSinceFertilizing = myPlantsViewModel.daysSinceFertilizing(plant),
                        onMarkAsWatered = { myPlantsViewModel.markAsWatered(plant) },
                        onMarkAsFertilized = { myPlantsViewModel.markAsFertilized(plant) },
                        onNavigateToDetails = {
                            navController.navigate("plantDetailsScreen/${plant.name}/delete") }
                    )
                }
            }
        }
    }
}

// Empty card that shows up when no plants are added to garden

@Composable
fun EmptyPlantCard(
    onClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Ikon av plante",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ingen planter i denne hagen enda.\nGro din første plante!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = onClick ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Legg til")
                    Spacer(Modifier.width(6.dp))
                    Text("Ny Plante", color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}