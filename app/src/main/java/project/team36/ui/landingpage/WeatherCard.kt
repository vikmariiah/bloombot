package project.team36.ui.landingpage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

//weatherCard that displays temperature, weather icon, weather description and precipitation
@Composable
fun WeatherCard(
    weatherUiState: WeatherUiState?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .height(100.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            weatherUiState != null -> {
                Row(
                    modifier     = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //weather description
                        Text(
                            text = weatherUiState.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        //weather icon is loaded directly from assets using Coil.
                        //The API returns the symbol name as a string already,
                        // so you don't have to do manual mapping (which would be needed if the icon was in drawable)
                        AsyncImage(
                            model = "file:///android_asset/${weatherUiState.iconName}.svg",
                            contentDescription = weatherUiState.description,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))

                    //Text that shows temp and precipitation
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = weatherUiState.temperature,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = weatherUiState.precipitation,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            //fallback if not connected to internet or if met API call fails
            else -> {
                Text(
                    text = "Kunne ikke hente vær",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}