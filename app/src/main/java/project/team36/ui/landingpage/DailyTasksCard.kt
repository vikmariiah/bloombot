package project.team36.ui.landingpage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.team36.R
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.ui.plant.InfoSection
import kotlin.collections.isNotEmpty


//card with all the plants that need watering today and are grouped by location
@Composable
fun DailyTasksCard(
    plantsByLocation: Map<SavedLocation, List<Pair<SavedPlant, String>>>,
    onMarkAsWatered: (SavedPlant) -> Unit,
) {
    //true if there's a location that has a plant that needs watering
    val hasAnyPlants = plantsByLocation.values.any { it.isNotEmpty() }

    Column {
        Text(
            text = "Dagens oppgaver",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
            color = MaterialTheme.colorScheme.onBackground
        )


        Card(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {

            //if there's no plants that need watering today
            if (!hasAnyPlants) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Ingen planter trenger vann i dag.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                //goes through all location and plants that need watering, skips location with no plants needing water
                plantsByLocation.forEach { (location, plantsWithText) ->
                    if (plantsWithText.isNotEmpty()) {
                        Text(
                            text = location.name ?: "Ukjent hage",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                        )

                        Card(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            //column med alle planter som trengs å vannes
                            Column(modifier = Modifier.padding(8.dp)) {
                                plantsWithText.forEach { (plant, wateringText) ->
                                    WateringPlantCard(
                                        plant = plant,
                                        wateringText = wateringText,
                                        onMarkAsWatered = { onMarkAsWatered(plant) },
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

//expandable card for a single plant that needs watering
@Composable
fun WateringPlantCard(
    plant: SavedPlant,
    wateringText: String,
    onMarkAsWatered: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    //fallback to default image (defaultplant)
    val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.defaultplant


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //picture of the plant
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = plant.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = plant.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            //"needs watering" status chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.wateringcan),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Må vannes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
        //expanded content, shows watering details and "mark as watered" button
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline
                )
                InfoSection(
                    title = "Vanningsbehov",
                    subtitle = wateringText,
                    buttonText = "Jeg har vannet",
                    buttonColor = MaterialTheme.colorScheme.primary,
                    onButtonClick = onMarkAsWatered
                )
            }
        }

        //arrow that show that the card can be expanded/collapsed
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(24.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Lukk" else "Åpne"
            )
        }
    }
}