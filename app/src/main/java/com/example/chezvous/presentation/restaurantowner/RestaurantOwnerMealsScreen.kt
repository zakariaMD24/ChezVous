package com.example.chezvous.presentation.restaurantowner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun RestaurantOwnerMealsScreen(
    viewModel: RestaurantOwnerMenuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val myItems = uiState.myRestaurantItems

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Mes plats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = ChezVousSpacing.screenHorizontal,
                vertical = ChezVousSpacing.md
            )
        )
        HorizontalDivider()

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            myItems.isEmpty() -> {
                OwnerMenuEmptyState(
                    message = if (uiState.myRestaurantId.isBlank())
                        "Aucun restaurant n'est associé à votre compte."
                    else
                        "Aucun plat trouvé pour votre restaurant."
                )
            }
            else -> {
                OwnerMenuItemList(
                    items = myItems,
                    restaurantNames = uiState.restaurantNames
                )
            }
        }
    }
}
