package com.example.chezvous.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chezvous.R
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.ui.components.ActiveFilterChipItem
import com.example.chezvous.ui.components.ActiveFilterChipRow
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousSearchBar
import com.example.chezvous.ui.components.FilterOptionChipItem
import com.example.chezvous.ui.components.FilterOptionChipRow
import com.example.chezvous.ui.components.FilterSortActionRow
import com.example.chezvous.ui.components.RestaurantCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.SheetOptionRow
import com.example.chezvous.ui.components.SheetSectionTitle
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.components.chezVousSheetPadding
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRestaurantClick: (String) -> Unit = {},
    onViewAllRestaurants: () -> Unit = {},
    onPartnerClick: () -> Unit = {},
    onAdminDetected: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAdminUser) {
        if (uiState.isAdminUser) onAdminDetected()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                showPartnerDashboard = uiState.showPartnerDashboard,
                onPartnerClick = onPartnerClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .chezVousScreenPadding(),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            item {
                Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(R.string.home_question),
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                HomeFoodHero(restaurant = uiState.restaurants.firstOrNull())
            }

            item {
                ChezVousSearchBar(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = stringResource(R.string.search_restaurant_food_category)
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
                ) {
                    items(uiState.cuisineTypes) { cuisine ->
                        CategoryChip(
                            text = cuisine.localizedCuisineLabel(),
                            selected = uiState.selectedCuisine == cuisine,
                            onClick = { viewModel.onCuisineSelected(cuisine) }
                        )
                    }
                }
            }

            item {
                RestaurantsSectionHeader(
                    onViewAllClick = onViewAllRestaurants
                )
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
                Spacer(modifier = Modifier.height(ChezVousSpacing.lg))
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
fun HomeFilterSheet(
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
                .chezVousSheetPadding()
                .padding(bottom = ChezVousSpacing.xl)
        ) {
            Text(
                text = stringResource(R.string.filter_restaurants),
                style = MaterialTheme.typography.titleLarge
            )

            SheetSectionTitle(text = stringResource(R.string.cuisine))
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

            SheetSectionTitle(text = stringResource(R.string.minimum_rating))
            FilterOptionChipRow(
                options = listOf(
                    FilterOptionChipItem(
                        label = stringResource(R.string.all_ratings),
                        selected = uiState.minimumRating == MIN_RATING_FILTER,
                        onClick = { onMinimumRatingChange(MIN_RATING_FILTER) }
                    ),
                    FilterOptionChipItem(
                        label = "4+",
                        selected = uiState.minimumRating == 4.0,
                        onClick = { onMinimumRatingChange(4.0) }
                    ),
                    FilterOptionChipItem(
                        label = "4.5+",
                        selected = uiState.minimumRating == TOP_RATING_FILTER,
                        onClick = { onMinimumRatingChange(TOP_RATING_FILTER) }
                    )
                )
            )

            SheetSectionTitle(text = stringResource(R.string.delivery_time))
            FilterOptionChipRow(
                options = listOf(
                    FilterOptionChipItem(
                        label = stringResource(R.string.all_delays),
                        selected = uiState.maxDeliveryMinutes == DEFAULT_MAX_DELIVERY_MINUTES,
                        onClick = { onMaxDeliveryMinutesChange(DEFAULT_MAX_DELIVERY_MINUTES) }
                    ),
                    FilterOptionChipItem(
                        label = stringResource(R.string.minutes_or_less, 30),
                        selected = uiState.maxDeliveryMinutes == 30,
                        onClick = { onMaxDeliveryMinutesChange(30) }
                    ),
                    FilterOptionChipItem(
                        label = stringResource(R.string.minutes_or_less, 40),
                        selected = uiState.maxDeliveryMinutes == 40,
                        onClick = { onMaxDeliveryMinutesChange(40) }
                    ),
                    FilterOptionChipItem(
                        label = stringResource(R.string.minutes_or_less, 50),
                        selected = uiState.maxDeliveryMinutes == 50,
                        onClick = { onMaxDeliveryMinutesChange(50) }
                    )
                )
            )

            SheetSectionTitle(text = stringResource(R.string.minimum_order))
            FilterOptionChipRow(
                options = listOf(
                    FilterOptionChipItem(
                        label = stringResource(R.string.all_minimums),
                        selected = uiState.maxMinimumOrder == DEFAULT_MAX_MINIMUM_ORDER,
                        onClick = { onMaxMinimumOrderChange(DEFAULT_MAX_MINIMUM_ORDER) }
                    ),
                    FilterOptionChipItem(
                        label = "30 DH",
                        selected = uiState.maxMinimumOrder == 30.0,
                        onClick = { onMaxMinimumOrderChange(30.0) }
                    ),
                    FilterOptionChipItem(
                        label = "50 DH",
                        selected = uiState.maxMinimumOrder == 50.0,
                        onClick = { onMaxMinimumOrderChange(50.0) }
                    ),
                    FilterOptionChipItem(
                        label = "80 DH",
                        selected = uiState.maxMinimumOrder == 80.0,
                        onClick = { onMaxMinimumOrderChange(80.0) }
                    )
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChezVousSize.inputMinHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.open_restaurants_only),
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
                    Text(stringResource(R.string.reset))
                }

                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }
}

@Composable
fun FilterValueHeader(
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
fun HomeSortSheet(
    selectedSort: RestaurantSortOption,
    onDismiss: () -> Unit,
    onSortSelected: (RestaurantSortOption) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .chezVousSheetPadding()
                .padding(bottom = ChezVousSpacing.xl)
        ) {
            Text(
                text = stringResource(R.string.sort),
                style = MaterialTheme.typography.titleLarge
            )

            RestaurantSortOption.entries.forEach { option ->
                SheetOptionRow(
                    text = option.localizedLabel(),
                    selected = selectedSort == option,
                    onClick = { onSortSelected(option) }
                )
            }
        }
    }
}

@Composable
fun EmptyRestaurantsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ChezVousSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

        Text(
            text = stringResource(R.string.no_restaurant_found),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.try_other_search_or_filter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun homeActiveFilterItems(
    uiState: HomeUiState,
    viewModel: HomeViewModel
): List<ActiveFilterChipItem> {
    return buildList {
        if (uiState.minimumRating > MIN_RATING_FILTER) {
            add(
                ActiveFilterChipItem(
                    stringResource(
                        R.string.rating_filter_label,
                        uiState.minimumRating.asRatingFilterLabel()
                    )
                ) {
                    viewModel.clearRatingFilter()
                }
            )
        }

        if (uiState.maxDeliveryMinutes < DEFAULT_MAX_DELIVERY_MINUTES) {
            add(
                ActiveFilterChipItem(
                    stringResource(R.string.minutes_or_less, uiState.maxDeliveryMinutes)
                ) {
                    viewModel.clearDeliveryFilter()
                }
            )
        }

        if (uiState.maxMinimumOrder < DEFAULT_MAX_MINIMUM_ORDER) {
            add(
                ActiveFilterChipItem(
                    stringResource(
                        R.string.minimum_filter_label,
                        uiState.maxMinimumOrder.asOrderFilterLabel()
                    )
                ) {
                    viewModel.clearMinimumOrderFilter()
                }
            )
        }

        if (uiState.onlyOpen) {
            add(
                ActiveFilterChipItem(stringResource(R.string.open_filter_label)) {
                    viewModel.clearOpenFilter()
                }
            )
        }
    }
}

@Composable
fun String.localizedCuisineLabel(): String {
    return if (this == ALL_CUISINES) {
        stringResource(R.string.all_cuisines)
    } else {
        this
    }
}

@Composable
private fun RestaurantSortOption.localizedLabel(): String {
    return when (this) {
        RestaurantSortOption.RECOMMENDED -> stringResource(R.string.sort_recommended)
        RestaurantSortOption.TOP_RATED -> stringResource(R.string.sort_top_rated)
        RestaurantSortOption.FASTEST_DELIVERY -> stringResource(R.string.sort_fastest_delivery)
        RestaurantSortOption.LOWEST_MINIMUM_ORDER -> stringResource(R.string.sort_lowest_minimum_order)
        RestaurantSortOption.NAME_A_Z -> stringResource(R.string.sort_name_a_z)
    }
}

@Composable
private fun HomeFoodHero(restaurant: Restaurant?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.home_hero_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.xxs))

                Text(
                    text = stringResource(R.string.home_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
                ) {
                    HomeHeroChip(text = stringResource(R.string.home_hero_fast))
                    HomeHeroChip(text = stringResource(R.string.home_hero_fresh))
                }
            }

            Spacer(modifier = Modifier.width(ChezVousSpacing.md))

            Surface(
                modifier = Modifier
                    .width(108.dp)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface
            ) {
                if (restaurant?.imageUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = restaurant.imageUrl,
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeliveryDining,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = ChezVousSpacing.xs,
                vertical = ChezVousSpacing.xxs
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    showPartnerDashboard: Boolean,
    onPartnerClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
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
                        text = stringResource(R.string.delivery_larache),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            if (showPartnerDashboard) {
                IconButton(onClick = onPartnerClick) {
                    Icon(
                        Icons.Outlined.Restaurant,
                        contentDescription = stringResource(R.string.partner_space)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun RestaurantsSectionHeader(
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(
            text = stringResource(R.string.restaurants_partners),
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onViewAllClick) {
            Text(stringResource(R.string.view_all))
        }
    }
}
