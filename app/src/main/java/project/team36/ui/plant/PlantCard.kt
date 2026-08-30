package project.team36.ui.plant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import project.team36.R
import project.team36.model.location.SavedPlant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
Plant card that displays information on the saved plant from database, and gives functionality to water and fertilize.
 */
@Composable
fun PlantCard(
    plant: SavedPlant,
    needsWater: Boolean,
    daysUntilWatering: Long,
    daysSinceFertilizing: Long,
    onMarkAsWatered: () -> Unit,
    onMarkAsFertilized: () -> Unit,
    onNavigateToDetails: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.defaultplant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Header that is always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = "Bilde av ${plant.name}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plant.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Chip for watering status
            if (needsWater) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            RoundedCornerShape(50)
                        )
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

            // Utvidet kort
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
                    // Counts down from date when it was last watered either manually or with rain
                    InfoSection(
                        title = "Vanningsbehov",
                        subtitle = when (daysUntilWatering) {
                            0L -> "Må vannes nå!"
                            1L -> "$daysUntilWatering dag til neste vanning"
                            else -> "$daysUntilWatering dager til neste vanning"
                        },
                        buttonText = "Jeg har vannet",
                        buttonColor = MaterialTheme.colorScheme.primary,
                        onButtonClick = onMarkAsWatered
                    )

                    // Fertilizing, displaying last date for tracking
                    InfoSection(
                        title = "Gjødsel",
                        subtitle = if (plant.lastFertilizationDate == 0L) {
                            "Ikke gjødslet enda"
                        } else {
                            "$daysSinceFertilizing dager siden sist gjødsling (${plant.lastFertilizationDate.toDisplayDate()})"
                        },
                        buttonText = "Jeg har gjødslet",
                        buttonColor = MaterialTheme.colorScheme.primary,
                        onButtonClick = onMarkAsFertilized
                    )
                    // Button to navigate to details screen for more information on the plant
                    Button(
                        onClick = onNavigateToDetails,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 14.dp, top = 8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Se detaljer",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            // Button for expansion of the card
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
}
//Infosection for displaying information about the plant with same format for each section
@Composable
fun InfoSection(
    title: String,
    subtitle: String,
    buttonText: String,
    buttonColor: Color,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        //FlowRow to accommodate larger fonts so the button is placed on a new line when display is scaled up
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(buttonColor),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = buttonText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Helping function to translate the date saved in database to a better displayable text
fun Long.toDisplayDate(): String {
    if (this == 0L) return "Ikke gjødslet enda"
    val sdf = SimpleDateFormat("dd.MMMM yyyy", Locale.forLanguageTag("no"))
    return sdf.format(Date(this))
}