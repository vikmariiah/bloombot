package project.team36.ui.findplants

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import project.team36.model.location.SavedLocation


// Dialog box to change garden
@Composable
fun SwitchLocationDialog (
    savedLocations: List<SavedLocation>,
    currentLocationName: String,
    onConfirm: (SavedLocation) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf(savedLocations.find { it.name == currentLocationName }) }

    AlertDialog (
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text("Endre hage") },
        text = {
            Column {
                if (savedLocations.isEmpty()) {
                    Text("Du har ingen lagrede hager enda. Legg til en ny hage for å komme i gang!")
                } else {
                    savedLocations.forEach { location ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedLocation == location,
                                onClick = {selectedLocation = location}
                            )
                            Text(
                                text = location.name?: "Ukjent",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (savedLocations.isEmpty()) {
                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = { onNavigateToMap(); onDismiss() }
                ) {
                    Text("Legg til")
                }
            } else {
                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    ),
                    onClick = { selectedLocation?.let { onConfirm(it) } },
                    enabled = selectedLocation != null
                ) { Text("Bekreft") }
            }
        },
        dismissButton = {
            if (savedLocations.isNotEmpty()) {
                OutlinedButton(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    onClick = onDismiss
                ) { Text("Avbryt") }
            }
        }
    )
}