package project.team36.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import project.team36.R
import project.team36.ui.map.LocationsViewModel

// Sets the values for nav-bar so it can be edited from one place for all screens
@Composable
fun BottomNavBar(navController: NavController, locationsViewModel : LocationsViewModel) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val activeRoute = currentRoute?.destination?.route

    val selectedLocation by locationsViewModel.selectedLocation.collectAsState()
    //passes the current location into findplants route
    val locationName = selectedLocation?.name ?: ""


    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onSecondary,
        unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = MaterialTheme.colorScheme.secondary
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        windowInsets = WindowInsets(0) //removes extra space under the navbar
    ) {
        NavigationBarItem(
            selected = activeRoute == "landingpage",
            onClick = {navController.navigate("landingpage")},
            colors = navItemColors,
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (activeRoute == "landingpage") R.drawable.home_icon_thick
                        else R.drawable.home_icon
                    ),
                    contentDescription = "Hjem"

                )
            },
            label = {Text("Hjem")}
        )

        NavigationBarItem(
            selected = activeRoute == "myPlaces",
            onClick = {navController.navigate("myPlaces")},
            colors = navItemColors,
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (activeRoute == "myPlaces") R.drawable.plant_icon_thick
                        else R.drawable.plant_icon
                    ),
                    contentDescription = "Mine hager"

                )
            },
            label = {Text("Mine hager")}
        )

        NavigationBarItem(
            selected = activeRoute?.startsWith("findPlants") == true,
            onClick = { navController.navigate("findPlants/$locationName") },
            colors = navItemColors,
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (activeRoute?.startsWith("findPlants") == true) R.drawable.search_icon_thick
                        else R.drawable.search_icon
                    ),
                    contentDescription = "Oppdag"
                )
            },
            label = { Text("Oppdag") }
        )
    }
}