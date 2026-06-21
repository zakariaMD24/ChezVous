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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.R
import com.example.chezvous.ui.components.ActiveFilterChipRow
import com.example.chezvous.ui.components.ActiveFilterChipItem
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousSearchBar
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.RestaurantCard
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.theme.ChezVousElevation
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun AllRestaurantsScreen(
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    onRestaurantClick: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = stringResource(R.string.all_restaurants),
                onBack = if (showBackButton) onBack else null
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AllRestaurantsControls(
                uiState = uiState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onFilterClick = { showFilterSheet = true },
                onSortClick = { showSortSheet = true },
                onCuisineSelected = viewModel::onCuisineSelected,
                activeFilters = homeActiveFilterItems(uiState, viewModel),
                onClearFilters = viewModel::clearFilters
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .chezVousScreenPadding(),
                verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
            ) {
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
                    Spacer(modifier = Modifier.height(ChezVousSpacing.lg))
                }
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

@Composable
private fun AllRestaurantsControls(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onCuisineSelected: (String) -> Unit,
    activeFilters: List<ActiveFilterChipItem>,
    onClearFilters: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = ChezVousElevation.card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .chezVousScreenPadding()
                .padding(bottom = ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                ChezVousSearchBar(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = stringResource(R.string.search_short),
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.Outlined.Sort,
                        contentDescription = stringResource(R.string.sort),
                        modifier = Modifier.size(ChezVousSize.iconMd)
                    )
                }

                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.filter),
                        modifier = Modifier.size(ChezVousSize.iconMd)
                    )
                }
            }

            ActiveFilterChipRow(
                filters = activeFilters,
                onClearAll = onClearFilters
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                items(uiState.cuisineTypes) { cuisine ->
                    CategoryChip(
                        text = cuisine.localizedCuisineLabel(),
                        selected = uiState.selectedCuisine == cuisine,
                        onClick = { onCuisineSelected(cuisine) }
                    )
                }
            }
        }
    }
}
