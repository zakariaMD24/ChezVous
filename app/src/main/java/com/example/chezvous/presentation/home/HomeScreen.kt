package com.example.chezvous.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.ui.components.ActiveFilterChipItem
import com.example.chezvous.ui.components.ActiveFilterChipRow
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousSearchBar
import com.example.chezvous.ui.components.FilterSortActionRow
import com.example.chezvous.ui.components.RestaurantCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.SheetOptionRow
import com.example.chezvous.ui.components.SheetSectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRestaurantClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCartClick: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeTopBar(
                cartItemCount = uiState.cartItemCount,
                onCartClick = onCartClick,
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bonjour",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Que voulez-vous manger aujourd'hui ?",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                ChezVousSearchBar(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = "Rechercher restaurant, plat, categorie"
                )
            }

            item {
                FilterSortActionRow(
                    filterCount = uiState.activeFilterCount,
                    onFilterClick = { showFilterSheet = true },
                    onSortClick = { showSortSheet = true }
                )
            }

            item {
                ActiveFilterChipRow(
                    filters = homeActiveFilterItems(uiState, viewModel),
                    onClearAll = viewModel::clearFilters
                )
            }

            item {
                LazyRow {
                    items(uiState.cuisineTypes) { cuisine ->
                        CategoryChip(
                            text = cuisine,
                            selected = uiState.selectedCuisine == cuisine,
                            onClick = { viewModel.onCuisineSelected(cuisine) }
                        )
                    }
                }
            }

            item {
                SectionTitle(text = "Restaurants partenaires")
            }

            if (uiState.restaurants.isEmpty()) {
                item {
                    EmptyRestaurantsState()
                }
            }

            items(uiState.restaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = {
                        onRestaurantClick(restaurant.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showFilterSheet) {
        HomeFilterSheet(
            uiState = uiState,
            onDismiss = { showFilterSheet = false },
            onCuisineSelected = viewModel::onCuisineSelected,
            onMinimumRatingChange = viewModel::onMinimumRatingChange,
            onMaxDeliveryMinutesChange = viewModel::onMaxDeliveryMinutesChange,
            onMaxMinimumOrderChange = viewModel::onMaxMinimumOrderChange,
            onOnlyOpenChange = viewModel::onOnlyOpenChange,
            onClearFilters = viewModel::clearFilters
        )
    }

    if (showSortSheet) {
        HomeSortSheet(
            selectedSort = uiState.sortOption,
            onDismiss = { showSortSheet = false },
            onSortSelected = {
                viewModel.onSortSelected(it)
                showSortSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFilterSheet(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onCuisineSelected: (String) -> Unit,
    onMinimumRatingChange: (Double) -> Unit,
    onMaxDeliveryMinutesChange: (Int) -> Unit,
    onMaxMinimumOrderChange: (Double) -> Unit,
    onOnlyOpenChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Filtrer les restaurants",
                style = MaterialTheme.typography.titleLarge
            )

            SheetSectionTitle(text = "Cuisine")
            LazyRow {
                items(uiState.cuisineTypes) { cuisine ->
                    CategoryChip(
                        text = cuisine,
                        selected = uiState.selectedCuisine == cuisine,
                        onClick = { onCuisineSelected(cuisine) }
                    )
                }
            }

            SheetSectionTitle(text = "Note minimale")
            FilterValueHeader(
                label = "Afficher les restaurants notes",
                value = if (uiState.minimumRating > MIN_RATING_FILTER) {
                    uiState.minimumRating.asRatingFilterLabel()
                } else {
                    "Toutes les notes"
                }
            )
            Slider(
                value = uiState.minimumRating.toFloat(),
                onValueChange = {
                    onMinimumRatingChange(it.toDouble().roundToHalf())
                },
                valueRange = MIN_RATING_FILTER.toFloat()..MAX_RATING_FILTER.toFloat(),
                steps = 9
            )

            SheetSectionTitle(text = "Temps de livraison")
            FilterValueHeader(
                label = "Delai maximum",
                value = if (uiState.maxDeliveryMinutes < DEFAULT_MAX_DELIVERY_MINUTES) {
                    "${uiState.maxDeliveryMinutes} min ou moins"
                } else {
                    "Tous les delais"
                }
            )
            Slider(
                value = uiState.maxDeliveryMinutes.toFloat(),
                onValueChange = {
                    onMaxDeliveryMinutesChange(it.roundToNearestFive())
                },
                valueRange = MIN_DELIVERY_FILTER.toFloat()..MAX_DELIVERY_FILTER.toFloat(),
                steps = 7
            )

            SheetSectionTitle(text = "Commande minimum")
            FilterValueHeader(
                label = "Commande minimum max",
                value = if (uiState.maxMinimumOrder < DEFAULT_MAX_MINIMUM_ORDER) {
                    uiState.maxMinimumOrder.asOrderFilterLabel()
                } else {
                    "Tous les minimums"
                }
            )
            Slider(
                value = uiState.maxMinimumOrder.toFloat(),
                onValueChange = {
                    onMaxMinimumOrderChange(it.roundToNearestFive().toDouble())
                },
                valueRange = MINIMUM_ORDER_FILTER_MIN.toFloat()..MINIMUM_ORDER_FILTER_MAX.toFloat(),
                steps = 15
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Restaurants ouverts seulement",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = uiState.onlyOpen,
                    onCheckedChange = onOnlyOpenChange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClearFilters) {
                    Text("Reinitialiser")
                }

                TextButton(onClick = onDismiss) {
                    Text("Appliquer")
                }
            }
        }
    }
}

@Composable
private fun FilterValueHeader(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSortSheet(
    selectedSort: RestaurantSortOption,
    onDismiss: () -> Unit,
    onSortSelected: (RestaurantSortOption) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Trier les restaurants",
                style = MaterialTheme.typography.titleLarge
            )

            RestaurantSortOption.entries.forEach { option ->
                SheetOptionRow(
                    text = option.label,
                    selected = selectedSort == option,
                    onClick = { onSortSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun EmptyRestaurantsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Aucun restaurant trouve",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Essayez une autre recherche ou retirez un filtre.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun homeActiveFilterItems(
    uiState: HomeUiState,
    viewModel: HomeViewModel
): List<ActiveFilterChipItem> {
    return buildList {
        if (uiState.minimumRating > MIN_RATING_FILTER) {
            add(
                ActiveFilterChipItem("Note ${uiState.minimumRating.asRatingFilterLabel()}") {
                    viewModel.clearRatingFilter()
                }
            )
        }

        if (uiState.maxDeliveryMinutes < DEFAULT_MAX_DELIVERY_MINUTES) {
            add(
                ActiveFilterChipItem("${uiState.maxDeliveryMinutes} min ou moins") {
                    viewModel.clearDeliveryFilter()
                }
            )
        }

        if (uiState.maxMinimumOrder < DEFAULT_MAX_MINIMUM_ORDER) {
            add(
                ActiveFilterChipItem("Min ${uiState.maxMinimumOrder.asOrderFilterLabel()}") {
                    viewModel.clearMinimumOrderFilter()
                }
            )
        }

        if (uiState.onlyOpen) {
            add(
                ActiveFilterChipItem("Ouvert") {
                    viewModel.clearOpenFilter()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    cartItemCount: Int,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "ChezVous",
                    style = MaterialTheme.typography.titleLarge
                )

                Row {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Livraison a domicile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onCartClick) {
                if (cartItemCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(cartItemCount.toString())
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Panier")
                    }
                } else {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Panier")
                }
            }

            IconButton(onClick = onProfileClick) {
                Icon(Icons.Outlined.Person, contentDescription = "Profil")
            }
        }
    )
}
