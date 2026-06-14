package com.example.chezvous.presentation.restaurant

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.ui.components.ActiveFilterChipItem
import com.example.chezvous.ui.components.ActiveFilterChipRow
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousSearchBar
import com.example.chezvous.ui.components.FilterSortActionRow
import com.example.chezvous.ui.components.FoodItemCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.SheetOptionRow
import com.example.chezvous.ui.components.SheetSectionTitle
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

    LaunchedEffect(restaurantId) {
        viewModel.loadRestaurant(restaurantId)
    }

    LaunchedEffect(uiState.cartMessage) {
        if (uiState.cartMessage != null) {
            delay(2500)
            viewModel.clearCartMessage()
        }
    }

    RestaurantDetailsContent(
        uiState = uiState,
        activeFilters = menuActiveFilterItems(uiState, viewModel),
        onBack = onBack,
        onOpenCart = onOpenCart,
        onAddToCart = viewModel::addToCart,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestaurantDetailsContent(
    uiState: RestaurantDetailsUiState,
    activeFilters: List<ActiveFilterChipItem>,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    onAddToCart: (FoodItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onClearFilters: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.restaurant?.name ?: "Restaurant")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                    }
                },
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
                                Icon(Icons.Outlined.ShoppingCart, contentDescription = "Panier")
                            }
                        } else {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Panier")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    RestaurantHeader(restaurant = uiState.restaurant)
                }

                item {
                    ChezVousSearchBar(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = "Rechercher un plat ou categorie"
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

                if (uiState.cartMessage != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = uiState.cartMessage.orEmpty(),
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    SectionTitle(text = "Menu")
                }

                if (uiState.menuItems.isEmpty()) {
                    item {
                        EmptyMenuState()
                    }
                }

                items(uiState.menuItems) { foodItem ->
                    FoodItemCard(
                        foodItem = foodItem,
                        onAddClick = {
                            onAddToCart(foodItem)
                        }
                    )
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Filtrer le menu",
                style = MaterialTheme.typography.titleLarge
            )

            SheetSectionTitle(text = "Categorie")
            LazyRow {
                items(uiState.categories) { category ->
                    CategoryChip(
                        text = category,
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            SheetSectionTitle(text = "Disponibilite")
            LazyRow {
                items(MenuAvailabilityFilter.entries) { filter ->
                    CategoryChip(
                        text = filter.label,
                        selected = uiState.availabilityFilter == filter,
                        onClick = { onAvailabilityFilterSelected(filter) }
                    )
                }
            }

            SheetSectionTitle(text = "Prix maximum")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Afficher les plats jusqu'a",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (uiState.maxPrice < DEFAULT_MAX_MENU_PRICE) {
                        uiState.maxPrice.asMenuPriceFilterLabel()
                    } else {
                        "Tous les prix"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = uiState.maxPrice.toFloat(),
                onValueChange = {
                    onMaxPriceChange(it.roundMenuPriceToNearestFive())
                },
                valueRange = MIN_MENU_PRICE_FILTER.toFloat()..MAX_MENU_PRICE_FILTER.toFloat(),
                steps = 19
            )

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
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Trier le menu",
                style = MaterialTheme.typography.titleLarge
            )

            MenuSortOption.entries.forEach { option ->
                SheetOptionRow(
                    text = option.label,
                    selected = selectedSort == option,
                    onClick = { onSortSelected(option) }
                )
            }
        }
    }
}

private fun menuActiveFilterItems(
    uiState: RestaurantDetailsUiState,
    viewModel: RestaurantDetailsViewModel
): List<ActiveFilterChipItem> {
    return buildList {
        if (uiState.selectedCategory != "Tous") {
            add(
                ActiveFilterChipItem(uiState.selectedCategory) {
                    viewModel.clearCategoryFilter()
                }
            )
        }

        if (uiState.availabilityFilter != MenuAvailabilityFilter.ALL) {
            add(
                ActiveFilterChipItem(uiState.availabilityFilter.label) {
                    viewModel.clearAvailabilityFilter()
                }
            )
        }

        if (uiState.maxPrice < DEFAULT_MAX_MENU_PRICE) {
            add(
                ActiveFilterChipItem("Max ${uiState.maxPrice.asMenuPriceFilterLabel()}") {
                    viewModel.clearPriceFilter()
                }
            )
        }
    }
}

@Composable
private fun RestaurantHeader(restaurant: Restaurant?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.padding(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = restaurant?.name ?: "Restaurant",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = restaurant?.cuisineType.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                RestaurantInfoRow(restaurant = restaurant)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Commande min. ${restaurant?.minimumOrder ?: 0.0} DH",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun RestaurantInfoRow(restaurant: Restaurant?) {
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
            text = restaurant?.rating?.toString() ?: "-",
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
            text = "Aucun plat trouve",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Essayez une autre categorie ou recherche.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
