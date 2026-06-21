package com.example.chezvous.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.ui.components.FoodItemCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.isDrinkItem
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun AdminRestaurantMenuScreen(
    viewModel: AdminRestaurantMenuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ChezVousSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        modifier = Modifier.padding(ChezVousSpacing.md),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        uiState.items.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyMenuContent()
            }
        }

        else -> {
            MenuContent(items = uiState.items)
        }
    }
}

@Composable
private fun MenuContent(items: List<com.example.chezvous.data.model.FoodItem>) {
    val drinkItems = items.filter { it.isDrinkItem() }
    val regularItems = items.filterNot { it.isDrinkItem() }

    // Group regular items by category for easier admin scanning
    val regularByCategory = regularItems
        .groupBy { it.category.ifBlank { "Divers" } }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm),
        contentPadding = PaddingValues(
            horizontal = ChezVousSpacing.screenHorizontal,
            vertical = ChezVousSpacing.md
        )
    ) {
        // Count badge header
        item {
            MenuCountHeader(total = items.size)
        }

        // Regular items grouped by category
        regularByCategory.forEach { (category, categoryItems) ->
            item(key = "header_$category") {
                SectionTitle(text = category)
            }
            items(categoryItems, key = { it.id }) { foodItem ->
                FoodItemCard(
                    foodItem = foodItem,
                    onAddClick = null
                )
            }
        }

        // Drinks section at the end
        if (drinkItems.isNotEmpty()) {
            item(key = "header_drinks") {
                SectionTitle(text = "Boissons")
            }
            items(drinkItems, key = { it.id }) { foodItem ->
                FoodItemCard(
                    foodItem = foodItem,
                    onAddClick = null
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(ChezVousSpacing.lg))
        }
    }
}

@Composable
private fun MenuCountHeader(total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = "$total plat${if (total > 1) "s" else ""} au menu",
            modifier = Modifier.padding(ChezVousSpacing.md),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EmptyMenuContent() {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Aucun plat au menu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Ce restaurant n'a pas encore de plats enregistrés.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = ChezVousSpacing.xl)
        )
    }
}
