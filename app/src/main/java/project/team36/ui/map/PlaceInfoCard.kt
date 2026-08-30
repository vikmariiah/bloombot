package project.team36.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import project.team36.model.location.SavedLocation

//a small card that shows up when user clicks on a pin on the map
//shows name, address and a button to navigate to myPlants screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoCard(
    location: SavedLocation,
    onDismiss: () -> Unit,
    navController: NavHostController,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(elevation = 4.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .width(316.dp)
                .height(200.dp)
                .background(color = Color(0xFFE8F8E3), shape = RoundedCornerShape(size = 15.dp))
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(text = location.name ?: "Ukjent", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = location.address ?: "Ingen adresse",
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF6F8069)
                )

                //button to navigate to myPlants screen, overview of plants on that location
                Button(
                    onClick = { navController.navigate("myPlants")},
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(193.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    Text(
                        text = "🌱Se mine planter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                ) {
                    Text("Lukk", color = Color(0xFF6F8069))
                }
            }
        }
    }
}