package com.example.chezvous.presentation.restaurant

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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chezvous.R
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.ui.components.ActiveFilterChipItem
import com.example.chezvous.ui.components.ActiveFilterChipRow
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousSearchBar
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.FilterOptionChipItem
import com.example.chezvous.ui.components.FilterOptionChipRow
import com.example.chezvous.ui.components.FilterSortActionRow
import com.example.chezvous.ui.components.FoodCustomizationSheet
import com.example.chezvous.ui.components.FoodItemCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.SheetOptionRow
import com.example.chezvous.ui.components.SheetSectionTitle
import com.example.chezvous.ui.components.asDhPrice
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.components.chezVousSheetPadding
import com.example.chezvous.ui.components.isDrinkItem
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    restaurantId: String,
    onBack: () -> Unit,
    onOpenCart: () -> Unit
) {
    val viewModel: RestaurantDetailsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var customizingItem by remember { mutableStateOf<FoodItem?>(null) }

    LaunchedEffect(restaurantId) {
        viewModel.loadRestaurant(restaurantId)
    }

    LaunchedEffect(uiState.cartMessage, uiState.cartMessageResId) {
        if (uiState.cartMessage != null || uiState.cartMessageResId != null) {
            delay(2500)
            viewModel.clearCartMessage()
        }
    }

    RestaurantDetailsContent(
        uiState = uiState,
        activeFilters = menuActiveFilterItems(uiState, viewModel),
        onBack = onBack,
        onOpenCart = onOpenCart,
        onCustomizeItem = { customizingItem = it },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterClick = { showFilterSheet = true },
        onSortClick = { showSortSheet = true },
        onClearFilters = viewModel::clearFilters
    )

    if (showFilterSheet) {
        MenuFilterSheet(
            uiState = uiState,
            onDismiss = { showFilterSheet = false },
            onCategorySelected = viewModel::onCategorySelected,
            onAvailabilityFilterSelected = viewModel::onAvailabilityFilterSelected,
            onMaxPriceChange = viewModel::onMaxPriceChange,
            onClearFilters = viewModel::clearFilters
        )
    }

    if (showSortSheet) {
        MenuSortSheet(
            selectedSort = uiState.sortOption,
            onDismiss = { showSortSheet = false },
            onSortSelected = {
                viewModel.onSortSelected(it)
                showSortSheet = false
            }
        )
    }

    customizingItem?.let { foodItem ->
        FoodCustomizationSheet(
            foodItem = foodItem,
            onDismiss = { customizingItem = null },
            onConfirm = { selectedExtras, removedIngredients, spiceLevel, instruction ->
                viewModel.addToCart(
                    foodItem = foodItem,
                    selectedExtras = selectedExtras,
                    removedIngredients = removedIngredients,
                    spiceLevel = spiceLevel,
                    specialInstruction = instruction
                )
                customizingItem = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestaurantDetailsContent(
    uiState: RestaurantDetailsUiState,
    activeFilters: List<ActiveFilterChipItem>,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    onCustomizeItem: (FoodItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onClearFilters: () -> Unit
) {
    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = uiState.restaurant?.name ?: stringResource(R.string.restaurant),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onOpenCart) {
                        if (uiState.cartItemCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(uiState.cartItemCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.ShoppingCart,
                                    contentDescription = stringResource(R.string.cart)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Outlined.ShoppingCart,
                                contentDescription = stringResource(R.string.cart)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.restaurant == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            val cartMessage = uiState.cartMessageResId?.let { resId ->
                stringResource(resId)
            } ?: uiState.cartMessage
            val drinkItems = uiState.menuItems.filter { it.isDrinkItem() }
            val regularMenuItems = uiState.menuItems.filterNot { it.isDrinkItem() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .chezVousScreenPadding(),
                verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    RestaurantHeader(restaurant = uiState.restaurant)
                }

                item {
                    ChezVousSearchBar(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = stringResource(R.string.search_menu_placeholder)
                    )
                }

                item {
                    FilterSortActionRow(
                        filterCount = uiState.activeFilterCount,
                        onFilterClick = onFilterClick,
                        onSortClick = onSortClick
                    )
                }

                item {
                    ActiveFilterChipRow(
                        filters = activeFilters,
                        onClearAll = onClearFilters
                    )
                }

                if (cartMessage != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = cartMessage,
                                modifier = Modifier.padding(ChezVousSpacing.sm),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (!uiState.canOrder) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = stringResource(R.string.partner_order_disabled),
                                modifier = Modifier.padding(ChezVousSpacing.sm),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                item {
                    SectionTitle(text = stringResource(R.string.menu))
                }

                if (uiState.menuItems.isEmpty()) {
                    item {
                        EmptyMenuState()
                    }
                }

                items(regularMenuItems) { foodItem ->
                    FoodItemCard(
                        foodItem = foodItem,
                        onAddClick = if (uiState.canOrder) {
                            { onCustomizeItem(foodItem) }
                        } else {
                            null
                        }
                    )
                }

                if (drinkItems.isNotEmpty()) {
                    item {
                        SectionTitle(text = stringResource(R.string.drinks))
                    }

                    items(drinkItems) { foodItem ->
                        FoodItemCard(
                            foodItem = foodItem,
                            onAddClick = if (uiState.canOrder) {
                                { onCustomizeItem(foodItem) }
                            } else {
                                null
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuFilterSheet(
    uiState: RestaurantDetailsUiState,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onAvailabilityFilterSelected: (MenuAvailabilityFilter) -> Unit,
    onMaxPriceChange: (Double) -> Unit,
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
                text = stringResource(R.string.filter_menu),
                style = MaterialTheme.typography.titleLarge
            )

            SheetSectionTitle(text = stringResource(R.string.category))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                items(uiState.categories) { category ->
                    CategoryChip(
                        text = category.localizedMenuCategoryLabel(),
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            SheetSectionTitle(text = stringResource(R.string.availability))
            FilterOptionChipRow(
                options = MenuAvailabilityFilter.entries.map { filter ->
                    FilterOptionChipItem(
                        label = filter.localizedLabel(),
                        selected = uiState.availabilityFilter == filter,
                        onClick = { onAvailabilityFilterSelected(filter) }
                    )
                }
            )

            SheetSectionTitle(text = stringResource(R.string.maximum_price))
            FilterOptionChipRow(
                options = listOf(
                    FilterOptionChipItem(
                        label = stringResource(R.string.all_prices),
                        selected = uiState.maxPrice == DEFAULT_MAX_MENU_PRICE,
                        onClick = { onMaxPriceChange(DEFAULT_MAX_MENU_PRICE) }
                    ),
                    FilterOptionChipItem(
                        label = "40 DH",
                        selected = uiState.maxPrice == 40.0,
                        onClick = { onMaxPriceChange(40.0) }
                    ),
                    FilterOptionChipItem(
                        label = "60 DH",
                        selected = uiState.maxPrice == 60.0,
                        onClick = { onMaxPriceChange(60.0) }
                    ),
                    FilterOptionChipItem(
                        label = "90 DH",
                        selected = uiState.maxPrice == 90.0,
                        onClick = { onMaxPriceChange(90.0) }
                    )
                )
            )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuSortSheet(
    selectedSort: MenuSortOption,
    onDismiss: () -> Unit,
    onSortSelected: (MenuSortOption) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .chezVousSheetPadding()
                .padding(bottom = ChezVousSpacing.xl)
        ) {
            Text(
                text = stringResource(R.string.sort_menu),
                style = MaterialTheme.typography.titleLarge
            )

            MenuSortOption.entries.forEach { option ->
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
private fun menuActiveFilterItems(
    uiState: RestaurantDetailsUiState,
    viewModel: RestaurantDetailsViewModel
): List<ActiveFilterChipItem> {
    return buildList {
        if (uiState.selectedCategory != ALL_CATEGORIES) {
            add(
                ActiveFilterChipItem(uiState.selectedCategory.localizedMenuCategoryLabel()) {
                    viewModel.clearCategoryFilter()
                }
            )
        }

        if (uiState.availabilityFilter != MenuAvailabilityFilter.ALL) {
            add(
                ActiveFilterChipItem(uiState.availabilityFilter.localizedLabel()) {
                    viewModel.clearAvailabilityFilter()
                }
            )
        }

        if (uiState.maxPrice < DEFAULT_MAX_MENU_PRICE) {
            add(
                ActiveFilterChipItem(
                    stringResource(
                        R.string.maximum_filter_label,
                        uiState.maxPrice.asMenuPriceFilterLabel()
                    )
                ) {
                    viewModel.clearPriceFilter()
                }
            )
        }
    }
}

@Composable
private fun String.localizedMenuCategoryLabel(): String {
    return if (this == ALL_CATEGORIES) {
        stringResource(R.string.all_categories)
    } else {
        this
    }
}

@Composable
private fun MenuAvailabilityFilter.localizedLabel(): String {
    return when (this) {
        MenuAvailabilityFilter.ALL -> stringResource(R.string.all_food_items)
        MenuAvailabilityFilter.AVAILABLE -> stringResource(R.string.available)
        MenuAvailabilityFilter.UNAVAILABLE -> stringResource(R.string.unavailable)
    }
}

@Composable
private fun MenuSortOption.localizedLabel(): String {
    return when (this) {
        MenuSortOption.DEFAULT -> stringResource(R.string.sort_recommended)
        MenuSortOption.PRICE_LOW_HIGH -> stringResource(R.string.sort_price_low_high)
        MenuSortOption.PRICE_HIGH_LOW -> stringResource(R.string.sort_price_high_low)
        MenuSortOption.NAME_A_Z -> stringResource(R.string.sort_name_a_z)
        MenuSortOption.AVAILABLE_FIRST -> stringResource(R.string.sort_available_first)
    }
}

@Composable
private fun RestaurantHeader(restaurant: Restaurant?) {
    ChezVousCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .height(156.dp)
                .padding(ChezVousSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RestaurantHeaderImage(restaurant = restaurant)

            Spacer(modifier = Modifier.width(ChezVousSpacing.sm))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = restaurant?.name ?: stringResource(R.string.restaurant),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = restaurant?.cuisineType.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

                RestaurantInfoRow(restaurant = restaurant)

                Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

                Text(
                    text = stringResource(
                        R.string.minimum_order_format,
                        (restaurant?.minimumOrder ?: 0.0).asDhPrice()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun RestaurantHeaderImage(restaurant: Restaurant?) {
    Surface(
        modifier = Modifier
            .width(124.dp)
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
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(ChezVousSize.iconLg),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RestaurantInfoRow(restaurant: Restaurant?) {
    val ratingText = when {
        restaurant == null -> "-"
        restaurant.ratingCount > 0 -> stringResource(
            R.string.restaurant_rating_with_count,
            restaurant.rating,
            restaurant.ratingCount
        )
        else -> stringResource(R.string.restaurant_no_reviews_yet)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = ratingText,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = restaurant?.deliveryTime.orEmpty(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun EmptyMenuState() {
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
            text = stringResource(R.string.no_menu_item_found),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.try_other_menu_search),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
