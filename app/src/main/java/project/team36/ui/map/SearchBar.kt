package project.team36.ui.map

import android.annotation.SuppressLint
import project.team36.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType



@SuppressLint("DefaultLocale")
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    suggestions: List<SearchSuggestion>,
    onSuggestionClicked: (SearchSuggestion) -> Unit,
    modifier: Modifier = Modifier,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Søk") },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(8.dp)
    )

    //shows list with suggestions
    if (suggestions.isNotEmpty()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .heightIn(max = 300.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                //removes categories, shows only addresses and names for places
                val filteredSuggestions = suggestions.filter { it.type !is SearchSuggestionType.Category }

                //uses full address if available, if not then fallback to region
                filteredSuggestions.forEachIndexed { index, suggestion ->
                    val addressText = suggestion.fullAddress
                        ?: listOfNotNull(
                            suggestion.address?.region,
                        ).joinToString(", ")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide() //hides keyboard when a suggestion is selected
                                onSuggestionClicked(suggestion)
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.search_result_marker),
                            contentDescription = "Mapbox Marker",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(24.dp)
                        )

                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = suggestion.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text(
                                text = addressText,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    //divider between suggestions
                    if (index < filteredSuggestions.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}