package project.team36.ui.landingpage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import project.team36.R

//Onboarding dialog gives a tutorial for how to use the app for new users
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    data class OnboardingStep(
        val title: String,
        val description: String,
        val imageRes: Int,
        val imageHeight: Int
    )

    val steps = listOf(
        OnboardingStep( "Velkommen til Bloombot", "Din digitale medgartner og plantedagbok for å hjelpe deg med å lykkes i din hage!", R.drawable.bloombot_logo_med_navnetrekk, 120),
        OnboardingStep("Registrer din hage", "Lagre en lokasjon for å ha oversikt over din hage. Vi gir deg også værvarsel tilpasset akkurat din hage.", R.drawable.minesteder, 180),
        OnboardingStep("Velg dine planter", "Få anbefalinger om planter som vil trives i akkurat din hage.", R.drawable.finnplanter, 180),
        OnboardingStep("Tilpasset KI-råd", "Få tilpasset KI-råd for dine planter! \nVi gir tips som når de kan plantes ute, eller hvilke hageoppgaver som kan prioriteres basert på værdata.", R.drawable.aibloom, 120)
    )

    var currentStep by remember { mutableIntStateOf(0) }
    val isLastStep = currentStep == steps.size - 1
    val step = steps[currentStep]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Skip button if user wishes to not go through onboarding
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Text("Hopp over", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    }
                }

                // Illustration
                Image(
                    painter = painterResource(id = step.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(step.imageHeight.dp)
                        .padding(horizontal = 24.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Animated step dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        steps.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (index == currentStep) 24.dp else 6.dp)
                                    .background(
                                        color = if (index == currentStep) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Title
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Description
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Next-button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.wrapContentWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text("Tilbake", color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                        Button(
                            onClick = { if (isLastStep) onFinish() else currentStep++ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLastStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary //Color(0xFFB8D9B0)
                            ),

                        ) {
                            Text(
                                text = if (isLastStep) "Kom i gang!" else "Neste",
                                color = if (isLastStep) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}