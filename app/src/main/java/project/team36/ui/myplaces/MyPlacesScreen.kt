package project.team36.ui.myplaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.House
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import project.team36.model.location.SavedLocation
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.navigation.BottomNavBar

//Screen to give a display of all saved places
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyPlacesScreen (
    locationsViewModel: LocationsViewModel,
    onLocationCardClick: (SavedLocation) -> Unit,
    onAddPlaceClick: () -> Unit,
    navController: NavController,

) {
    val savedLocations by locationsViewModel.savedLocations.collectAsState()
    val showMaxLocationsDialog by locationsViewModel.showMaxLocationsDialog.collectAsState()

    if (showMaxLocationsDialog) {
        AlertDialog(
            onDismissRequest = { locationsViewModel.dismissMaxLocationsDialog() },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Maks antall hager nådd") },
            text = { Text("Du kan maksimum ha 5 lagrede hager. Slett en hage for å legge til en ny.") },
            confirmButton = {
                TextButton(onClick = { locationsViewModel.dismissMaxLocationsDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mine hager",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0) //removes extra space of the top of the screen
            )
        },

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (locationsViewModel.onAddLocationClicked()) {
                        onAddPlaceClick()
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Ny hage") },
                text = { Text("Ny hage") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50)
            )
        },
            bottomBar = { BottomNavBar(navController = navController, locationsViewModel)}

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (savedLocations.isEmpty()) {
                    EmptyLocationCard (
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onAddPlaceClick = { navController.navigate("map") }
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(savedLocations) { location ->
                            LocationCard(
                                location = location,
                                onClick = {
                                    locationsViewModel.selectLocation(location)
                                    onLocationCardClick(location)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Card displayed when app has no saved locations
@Composable
fun EmptyLocationCard(
    modifier: Modifier = Modifier,
    onAddPlaceClick: () -> Unit
){
    Card(
        modifier = modifier
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
                    imageVector = Icons.Default.House,
                    contentDescription = "Ikon av hus",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Du har ingen lagrede hager enda.\nLegg til din første hage!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = onAddPlaceClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Legg til hage")
                    Spacer(Modifier.width(6.dp))
                    Text("Ny hage")
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// Card displaying location name
@Composable
fun LocationCard (
    location: SavedLocation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\uD83C\uDFE0", // house-emoji
                fontSize = 36.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name ?: "Ukjent hage",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = location.address ?: "Ingen adresse funnet",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Gå til $location.name",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
