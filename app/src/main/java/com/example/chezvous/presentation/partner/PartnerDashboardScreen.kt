package com.example.chezvous.presentation.partner

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.R
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.model.User
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.PartnerMenuItemCard
import com.example.chezvous.ui.components.PartnerOrderCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.components.chezVousSheetPadding
import com.example.chezvous.ui.components.customerLabel
import com.example.chezvous.ui.components.nextPartnerStatus
import com.example.chezvous.ui.theme.ChezVousSpacing
import kotlinx.coroutines.delay

private enum class PartnerDashboardSection {
    SETTINGS,
    MENU,
    COMMANDS,
    DRIVERS,
    USERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardScreen(
    onBack: () -> Unit,
    showBackButton: Boolean = true
) {
    val viewModel: PartnerDashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var editingDriver by remember { mutableStateOf<Driver?>(null) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showDriverEditor by remember { mutableStateOf(false) }
    var showUserEditor by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf<PartnerDashboardSection?>(null) }

    LaunchedEffect(uiState.message, uiState.errorMessage) {
        if (uiState.message != null || uiState.errorMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.selectedRestaurantId) {
        selectedSection = null
    }

    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = "Espace partenaire",
                onBack = if (showBackButton) onBack else null
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            !uiState.isAuthorized -> {
                PartnerAccessState(
                    message = uiState.errorMessage.orEmpty(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                )
            }

            else -> {
                PartnerDashboardContent(
                    uiState = uiState,
                    onRestaurantSelected = viewModel::selectRestaurant,
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSection = it },
                    onChangeRestaurant = {
                        selectedSection = null
                        viewModel.clearRestaurantSelection()
                    },
                    onBackToSections = {
                        selectedSection = null
                    },
                    onUpdateOrderStatus = viewModel::updateOrderStatus,
                    onSaveRestaurantSettings = viewModel::saveRestaurantSettings,
                    onAvailabilityChange = viewModel::updateItemAvailability,
                    onEditItem = {
                        editingItem = it
                        showEditor = true
                    },
                    onAddItem = {
                        editingItem = null
                        showEditor = true
                    },
                    onEditDriver = {
                        editingDriver = it
                        showDriverEditor = true
                    },
                    onAddDriver = {
                        editingDriver = null
                        showDriverEditor = true
                    },
                    onDriverAvailabilityChange = viewModel::updateDriverAvailability,
                    onEditUser = {
                        editingUser = it
                        showUserEditor = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    if (showEditor) {
        MenuItemEditorSheet(
            item = editingItem,
            onDismiss = { showEditor = false },
            onSave = { itemId, name, description, category, price, imageUrl, extraOptions, removableIngredientOptions, isSpiceLevelEnabled ->
                viewModel.saveMenuItem(
                    itemId = itemId,
                    name = name,
                    description = description,
                    category = category,
                    priceText = price,
                    imageUrl = imageUrl,
                    extraOptions = extraOptions,
                    removableIngredientOptions = removableIngredientOptions,
                    isSpiceLevelEnabled = isSpiceLevelEnabled
                )
                showEditor = false
            }
        )
    }

    if (showDriverEditor) {
        DriverEditorSheet(
            driver = editingDriver,
            onDismiss = { showDriverEditor = false },
            onSave = { driverId, fullName, phone, vehicleType, rating, isAvailable ->
                viewModel.saveDriver(
                    driverId = driverId,
                    fullName = fullName,
                    phone = phone,
                    vehicleType = vehicleType,
                    ratingText = rating,
                    isAvailable = isAvailable
                )
                showDriverEditor = false
            }
        )
    }

    if (showUserEditor) {
        val user = editingUser
        if (user != null) {
            UserAccessEditorSheet(
                user = user,
                restaurants = uiState.restaurants,
                drivers = uiState.drivers,
                onDismiss = { showUserEditor = false },
                onSave = { role, restaurantIds, driverId ->
                    viewModel.saveUserAccess(
                        user = user,
                        role = role,
                        managedRestaurantIds = restaurantIds,
                        driverId = driverId
                    )
                    showUserEditor = false
                }
            )
        }
    }
}

@Composable
private fun PartnerDashboardContent(
    uiState: PartnerDashboardUiState,
    onRestaurantSelected: (String) -> Unit,
    selectedSection: PartnerDashboardSection?,
    onSectionSelected: (PartnerDashboardSection) -> Unit,
    onChangeRestaurant: () -> Unit,
    onBackToSections: () -> Unit,
    onUpdateOrderStatus: (Order, OrderStatus) -> Unit,
    onSaveRestaurantSettings: (
        name: String,
        cuisineType: String,
        deliveryTime: String,
        imageUrl: String,
        isOpen: Boolean
    ) -> Unit,
    onAvailabilityChange: (FoodItem, Boolean) -> Unit,
    onEditItem: (FoodItem) -> Unit,
    onAddItem: () -> Unit,
    onEditDriver: (Driver) -> Unit,
    onAddDriver: () -> Unit,
    onDriverAvailabilityChange: (Driver, Boolean) -> Unit,
    onEditUser: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.selectedRestaurant == null -> {
            RestaurantSelectionContent(
                uiState = uiState,
                onRestaurantSelected = onRestaurantSelected,
                modifier = modifier
            )
        }

        selectedSection == null -> {
            PartnerSectionPickerContent(
                uiState = uiState,
                onSectionSelected = onSectionSelected,
                onChangeRestaurant = onChangeRestaurant,
                modifier = modifier
            )
        }

        selectedSection == PartnerDashboardSection.COMMANDS -> {
            CommandsManagementContent(
                uiState = uiState,
                onBack = onBackToSections,
                onChangeRestaurant = onChangeRestaurant,
                onUpdateOrderStatus = onUpdateOrderStatus,
                modifier = modifier
            )
        }

        selectedSection == PartnerDashboardSection.SETTINGS -> {
            RestaurantSettingsContent(
                uiState = uiState,
                onBack = onBackToSections,
                onChangeRestaurant = onChangeRestaurant,
                onSaveRestaurantSettings = onSaveRestaurantSettings,
                modifier = modifier
            )
        }

        selectedSection == PartnerDashboardSection.MENU -> {
            MenuManagementContent(
                uiState = uiState,
                onBack = onBackToSections,
                onChangeRestaurant = onChangeRestaurant,
                onAvailabilityChange = onAvailabilityChange,
                onEditItem = onEditItem,
                onAddItem = onAddItem,
                modifier = modifier
            )
        }

        selectedSection == PartnerDashboardSection.DRIVERS -> {
            DriversManagementContent(
                uiState = uiState,
                onBack = onBackToSections,
                onChangeRestaurant = onChangeRestaurant,
                onEditDriver = onEditDriver,
                onAddDriver = onAddDriver,
                onAvailabilityChange = onDriverAvailabilityChange,
                modifier = modifier
            )
        }

        selectedSection == PartnerDashboardSection.USERS -> {
            UsersManagementContent(
                uiState = uiState,
                onBack = onBackToSections,
                onChangeRestaurant = onChangeRestaurant,
                onEditUser = onEditUser,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun RestaurantSelectionContent(
    uiState: PartnerDashboardUiState,
    onRestaurantSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.partner_choose_restaurant),
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = stringResource(R.string.partner_choose_restaurant_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            PartnerMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        if (uiState.restaurants.isEmpty()) {
            item {
                EmptyPartnerSection(text = stringResource(R.string.partner_no_restaurant_available))
            }
        }

        items(uiState.restaurants) { restaurant ->
            RestaurantSelectionCard(
                restaurant = restaurant,
                onClick = { onRestaurantSelected(restaurant.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PartnerSectionPickerContent(
    uiState: PartnerDashboardUiState,
    onSectionSelected: (PartnerDashboardSection) -> Unit,
    onChangeRestaurant: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            PartnerDashboardHeader(
                title = uiState.selectedRestaurant?.name.orEmpty(),
                subtitle = stringResource(R.string.partner_choose_management_area),
                onChangeRestaurant = onChangeRestaurant
            )
        }

        item {
            PartnerMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        item {
            ManagementActionCard(
                title = stringResource(R.string.partner_restaurant_settings),
                subtitle = stringResource(R.string.partner_restaurant_settings_summary),
                icon = Icons.Outlined.Storefront,
                onClick = { onSectionSelected(PartnerDashboardSection.SETTINGS) }
            )
        }

        item {
            ManagementActionCard(
                title = stringResource(R.string.partner_manage_menu),
                subtitle = stringResource(
                    R.string.partner_manage_menu_summary,
                    uiState.menuItems.size
                ),
                icon = Icons.Outlined.MenuBook,
                onClick = { onSectionSelected(PartnerDashboardSection.MENU) }
            )
        }

        item {
            ManagementActionCard(
                title = stringResource(R.string.partner_received_orders),
                subtitle = stringResource(
                    R.string.partner_received_orders_summary,
                    uiState.orders.size
                ),
                icon = Icons.Outlined.ReceiptLong,
                onClick = { onSectionSelected(PartnerDashboardSection.COMMANDS) }
            )
        }

        if (UserRoles.hasGlobalRestaurantAccess(uiState.role)) {
            item {
                ManagementActionCard(
                    title = stringResource(R.string.partner_drivers),
                    subtitle = stringResource(
                        R.string.partner_drivers_summary,
                        uiState.drivers.count { it.isAvailable },
                        uiState.drivers.size
                    ),
                    icon = Icons.Outlined.DeliveryDining,
                    onClick = { onSectionSelected(PartnerDashboardSection.DRIVERS) }
                )
            }

            item {
                ManagementActionCard(
                    title = stringResource(R.string.partner_users_roles),
                    subtitle = stringResource(
                        R.string.partner_users_roles_summary,
                        uiState.users.count { it.role != UserRoles.CUSTOMER }
                    ),
                    icon = Icons.Outlined.Person,
                    onClick = { onSectionSelected(PartnerDashboardSection.USERS) }
                )
            }
        }
    }
}

@Composable
private fun RestaurantSettingsContent(
    uiState: PartnerDashboardUiState,
    onBack: () -> Unit,
    onChangeRestaurant: () -> Unit,
    onSaveRestaurantSettings: (
        name: String,
        cuisineType: String,
        deliveryTime: String,
        imageUrl: String,
        isOpen: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val restaurant = uiState.selectedRestaurant
    var name by remember(restaurant?.id) { mutableStateOf(restaurant?.name.orEmpty()) }
    var cuisineType by remember(restaurant?.id) { mutableStateOf(restaurant?.cuisineType.orEmpty()) }
    var deliveryTime by remember(restaurant?.id) { mutableStateOf(restaurant?.deliveryTime.orEmpty()) }
    var imageUrl by remember(restaurant?.id) { mutableStateOf(restaurant?.imageUrl.orEmpty()) }
    var isOpen by remember(restaurant?.id) { mutableStateOf(restaurant?.isOpen ?: true) }

    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            SectionScreenHeader(
                title = stringResource(R.string.partner_restaurant_settings),
                subtitle = restaurant?.name.orEmpty(),
                onBack = onBack,
                onChangeRestaurant = onChangeRestaurant
            )
        }

        item {
            PartnerMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        item {
            ChezVousTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.restaurant_name)
            )
        }

        item {
            ChezVousTextField(
                value = cuisineType,
                onValueChange = { cuisineType = it },
                label = stringResource(R.string.cuisine_type)
            )
        }

        item {
            ChezVousTextField(
                value = deliveryTime,
                onValueChange = { deliveryTime = it },
                label = stringResource(R.string.delivery_time)
            )
        }

        item {
            ChezVousTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = stringResource(R.string.image_url),
                imeAction = ImeAction.Done
            )
        }

        item {
            ChezVousCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ChezVousSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.restaurant_open_setting),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = if (isOpen) {
                                stringResource(R.string.restaurant_open_hint)
                            } else {
                                stringResource(R.string.restaurant_closed_hint)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isOpen,
                        onCheckedChange = { isOpen = it },
                        enabled = !uiState.isSaving
                    )
                }
            }
        }

        item {
            ChezVousButton(
                text = stringResource(R.string.save),
                loadingText = "Enregistrement...",
                isLoading = uiState.isSaving,
                onClick = {
                    onSaveRestaurantSettings(
                        name,
                        cuisineType,
                        deliveryTime,
                        imageUrl,
                        isOpen
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CommandsManagementContent(
    uiState: PartnerDashboardUiState,
    onBack: () -> Unit,
    onChangeRestaurant: () -> Unit,
    onUpdateOrderStatus: (Order, OrderStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember(uiState.selectedRestaurantId) {
        mutableStateOf<OrderStatus?>(null)
    }
    val visibleOrders = selectedStatus?.let { status ->
        uiState.orders.filter { it.status == status }
    } ?: uiState.orders

    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            SectionScreenHeader(
                title = stringResource(R.string.partner_received_orders),
                subtitle = uiState.selectedRestaurant?.name.orEmpty(),
                onBack = onBack,
                onChangeRestaurant = onChangeRestaurant
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                item {
                    CategoryChip(
                        text = stringResource(R.string.partner_all_orders),
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null }
                    )
                }

                items(OrderStatus.entries) { status ->
                    CategoryChip(
                        text = status.customerLabel(),
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status }
                    )
                }
            }
        }

        item {
            PartnerMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        if (visibleOrders.isEmpty()) {
            item {
                EmptyPartnerSection(text = stringResource(R.string.partner_no_orders_for_restaurant))
            }
        }

        items(visibleOrders) { order ->
            PartnerOrderCard(
                order = order,
                nextStatus = order.status.nextPartnerStatus(),
                onUpdateStatus = { nextStatus ->
                    onUpdateOrderStatus(order, nextStatus)
                },
                isSaving = uiState.isSaving
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MenuManagementContent(
    uiState: PartnerDashboardUiState,
    onBack: () -> Unit,
    onChangeRestaurant: () -> Unit,
    onAvailabilityChange: (FoodItem, Boolean) -> Unit,
    onEditItem: (FoodItem) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember(uiState.selectedRestaurantId) {
        mutableStateOf<String?>(null)
    }
    val menuCategories = uiState.menuItems
        .map { it.category }
        .filter { it.isNotBlank() }
        .distinct()
    val safeSelectedCategory = selectedCategory.takeIf { it in menuCategories }
    val visibleMenuItems = safeSelectedCategory?.let { category ->
        uiState.menuItems.filter { it.category == category }
    } ?: uiState.menuItems

    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            SectionScreenHeader(
                title = stringResource(R.string.partner_manage_menu),
                subtitle = uiState.selectedRestaurant?.name.orEmpty(),
                onBack = onBack,
                onChangeRestaurant = onChangeRestaurant
            )
        }

        if (menuCategories.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
                ) {
                    item {
                        CategoryChip(
                            text = stringResource(R.string.all_categories),
                            selected = safeSelectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                    }

                    items(menuCategories) { category ->
                        CategoryChip(
                            text = category,
                            selected = safeSelectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        }

        item {
            PartnerMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(
                    text = stringResource(R.string.partner_dishes),
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = onAddItem,
                    enabled = !uiState.isSaving
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text(stringResource(R.string.add_menu_item))
                }
            }
        }

        if (visibleMenuItems.isEmpty()) {
            item {
                EmptyPartnerSection(text = stringResource(R.string.partner_no_menu_for_restaurant))
            }
        }

        items(visibleMenuItems) { foodItem ->
            PartnerMenuItemCard(
                foodItem = foodItem,
                onAvailabilityChange = { isAvailable ->
                    onAvailabilityChange(foodItem, isAvailable)
                },
                onEditClick = { onEditItem(foodItem) },
                isSaving = uiState.isSaving
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DriversManagementContent(
    uiState: PartnerDashboardUiState,
    onBack: () -> Unit,
    onChangeRestaurant: () -> Unit,
    onEditDriver: (Driver) -> Unit,
    onAddDriver: () -> Unit,
    onAvailabilityChange: (Driver, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            SectionScreenHeader(
                title = stringResource(R.string.partner_drivers),
                subtitle = uiState.selectedRestaurant?.name.orEmpty(),
                onBack = onBack,
                onChangeRestaurant = onChangeRestaurant
            )
        }

        item {
            PartnerMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(
                    text = stringResource(R.string.partner_driver_list),
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = onAddDriver,
                    enabled = !uiState.isSaving
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text(stringResource(R.string.partner_add_driver))
                }
            }
        }

        if (uiState.drivers.isEmpty()) {
            item {
                EmptyPartnerSection(text = stringResource(R.string.partner_no_drivers))
            }
        }

        items(uiState.drivers) { driver ->
            DriverManagementCard(
                driver = driver,
                onEdit = { onEditDriver(driver) },
                onAvailabilityChange = { isAvailable ->
                    onAvailabilityChange(driver, isAvailable)
                },
                isSaving = uiState.isSaving
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DriverManagementCard(
    driver: Driver,
    onEdit: () -> Unit,
    onAvailabilityChange: (Boolean) -> Unit,
    isSaving: Boolean
) {
    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeliveryDining,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driver.fullName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${driver.vehicleType} - ${driver.phone}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isSaving
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
                    Text(stringResource(R.string.edit))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.partner_driver_rating, driver.rating),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = stringResource(R.string.partner_driver_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(ChezVousSpacing.xs))

                Switch(
                    checked = driver.isAvailable,
                    onCheckedChange = onAvailabilityChange,
                    enabled = !isSaving
                )
            }
        }
    }
}

@Composable
private fun UsersManagementContent(
    uiState: PartnerDashboardUiState,
    onBack: () -> Unit,
    onChangeRestaurant: () -> Unit,
    onEditUser: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchingUser by remember { mutableStateOf(false) }
    val elevatedUsers = uiState.users.filter { it.role != UserRoles.CUSTOMER }
    val searchResults = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        uiState.users.filter { user ->
            user.email.contains(searchQuery.trim(), ignoreCase = true) ||
                    user.fullName.contains(searchQuery.trim(), ignoreCase = true)
        }
    }
    val visibleUsers = if (isSearchingUser) searchResults else elevatedUsers

    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            SectionScreenHeader(
                title = stringResource(R.string.partner_users_roles),
                subtitle = if (isSearchingUser) {
                    stringResource(R.string.partner_user_search_mode)
                } else {
                    stringResource(R.string.partner_users_roles_summary, elevatedUsers.size)
                },
                onBack = onBack,
                onChangeRestaurant = onChangeRestaurant
            )
        }

        item {
            PartnerMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        isSearchingUser = !isSearchingUser
                        searchQuery = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (isSearchingUser) {
                            stringResource(R.string.partner_active_roles)
                        } else {
                            stringResource(R.string.partner_add_user)
                        }
                    )
                }
            }
        }

        if (isSearchingUser) {
            item {
                ChezVousTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = stringResource(R.string.partner_search_user)
                )
            }
        }

        if (visibleUsers.isEmpty()) {
            item {
                EmptyPartnerSection(
                    text = if (isSearchingUser) {
                        stringResource(R.string.partner_no_search_results)
                    } else {
                        stringResource(R.string.partner_no_active_roles)
                    }
                )
            }
        }

        items(visibleUsers) { user ->
            UserAccessCard(
                user = user,
                restaurants = uiState.restaurants,
                drivers = uiState.drivers,
                isSaving = uiState.isSaving,
                onEdit = { onEditUser(user) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun UserAccessCard(
    user: User,
    restaurants: List<Restaurant>,
    drivers: List<Driver>,
    isSaving: Boolean,
    onEdit: () -> Unit
) {
    val assignedRestaurants = restaurants
        .filter { it.id in user.managedRestaurantIds }
        .joinToString { it.name }
        .ifBlank { "Aucun restaurant" }
    val linkedDriver = drivers.firstOrNull { it.id == user.driverId }?.fullName
        ?: "Aucun livreur"

    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName.ifBlank { user.email.ifBlank { user.id.take(8) } },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = user.email.ifBlank { user.id },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isSaving
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
                    Text(stringResource(R.string.edit))
                }
            }

            Text(
                text = "Role: ${user.role}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (
                user.role == UserRoles.PARTNER ||
                user.role == UserRoles.RESTAURANT_ADMIN ||
                user.role == UserRoles.CHEF
            ) {
                Text(
                    text = "Restaurants: $assignedRestaurants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (user.role == UserRoles.DRIVER) {
                Text(
                    text = "Fiche livreur: $linkedDriver",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RestaurantSelectionCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    ChezVousCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${restaurant.cuisineType} - ${restaurant.deliveryTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PartnerDashboardHeader(
    title: String,
    subtitle: String,
    onChangeRestaurant: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)) {
        Text(
            text = title.ifBlank { "Restaurant" },
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(onClick = onChangeRestaurant) {
            Icon(Icons.Outlined.Storefront, contentDescription = null)
            Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
            Text(stringResource(R.string.partner_change_restaurant))
        }
    }
}

@Composable
private fun SectionScreenHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onChangeRestaurant: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
                Text(stringResource(R.string.partner_back))
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onChangeRestaurant) {
                Text(stringResource(R.string.partner_change))
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ManagementActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ChezVousCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PartnerMessageArea(
    message: String?,
    errorMessage: String?
) {
    val text = errorMessage ?: message ?: return
    val isError = errorMessage != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    }
}

@Composable
private fun EmptyPartnerSection(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PartnerAccessState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(54.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Acces partenaire requis",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemEditorSheet(
    item: FoodItem?,
    onDismiss: () -> Unit,
    onSave: (
        itemId: String,
        name: String,
        description: String,
        category: String,
        price: String,
        imageUrl: String,
        extraOptions: List<CustomizationOption>,
        removableIngredientOptions: List<CustomizationOption>,
        isSpiceLevelEnabled: Boolean
    ) -> Unit
) {
    var name by remember(item?.id) { mutableStateOf(item?.name.orEmpty()) }
    var description by remember(item?.id) { mutableStateOf(item?.description.orEmpty()) }
    var category by remember(item?.id) { mutableStateOf(item?.category.orEmpty()) }
    var imageUrl by remember(item?.id) { mutableStateOf(item?.imageUrl.orEmpty()) }
    var attemptedSave by remember(item?.id) { mutableStateOf(false) }
    var price by remember(item?.id) {
        mutableStateOf(
            item?.price?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            }.orEmpty()
        )
    }
    var extraOptions by remember(item?.id) {
        mutableStateOf(
            item?.extraOptions
                ?.map { it.toEditableOption() }
                .orEmpty()
        )
    }
    var removableIngredientOptions by remember(item?.id) {
        val existingOptions = item?.removableIngredientOptions.orEmpty()
        val legacyIngredients = item?.removableIngredients.orEmpty()
        mutableStateOf(
            existingOptions
                .ifEmpty {
                    legacyIngredients.map { ingredient ->
                        CustomizationOption(id = ingredient.toOptionId(), name = ingredient)
                    }
                }
                .map { it.toEditableOption() }
        )
    }
    var isSpiceLevelEnabled by remember(item?.id) {
        mutableStateOf(item?.hasSpiceLevelSelection() == true)
    }
    val parsedPrice = price.replace(",", ".").toDoubleOrNull()
    val nameHasError = attemptedSave && name.trim().isBlank()
    val descriptionHasError = attemptedSave && description.trim().isBlank()
    val categoryHasError = attemptedSave && category.trim().isBlank()
    val priceHasError = attemptedSave && (parsedPrice == null || parsedPrice <= 0.0)
    val canSubmit = name.trim().isNotBlank() &&
            description.trim().isNotBlank() &&
            category.trim().isNotBlank() &&
            parsedPrice != null &&
            parsedPrice > 0.0

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .chezVousSheetPadding()
                .padding(bottom = ChezVousSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Text(
                text = if (item == null) {
                    stringResource(R.string.add_menu_item)
                } else {
                    stringResource(R.string.edit_menu_item)
                },
                style = MaterialTheme.typography.titleLarge
            )

            ChezVousTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.food_name),
                isError = nameHasError,
                supportingText = if (nameHasError) {
                    stringResource(R.string.field_required)
                } else {
                    null
                }
            )

            ChezVousTextField(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.description),
                singleLine = false,
                isError = descriptionHasError,
                supportingText = if (descriptionHasError) {
                    stringResource(R.string.field_required)
                } else {
                    null
                }
            )

            ChezVousTextField(
                value = category,
                onValueChange = { category = it },
                label = stringResource(R.string.category),
                isError = categoryHasError,
                supportingText = if (categoryHasError) {
                    stringResource(R.string.field_required)
                } else {
                    null
                }
            )

            ChezVousTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = stringResource(R.string.image_url)
            )

            ChezVousTextField(
                value = price,
                onValueChange = {
                    price = it.filter { char ->
                        char.isDigit() || char == '.' || char == ','
                    }
                },
                label = stringResource(R.string.price),
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                isError = priceHasError,
                supportingText = if (priceHasError) {
                    stringResource(R.string.valid_price_required)
                } else {
                    null
                }
            )

            SectionTitle(text = stringResource(R.string.partner_customization))

            extraOptions.forEachIndexed { index, option ->
                ExtraOptionEditorCard(
                    option = option,
                    title = stringResource(R.string.extra_option),
                    nameLabel = stringResource(R.string.extra_option_name),
                    imageLabel = stringResource(R.string.extra_option_image_url),
                    descriptionLabel = stringResource(R.string.option_description),
                    showPrice = true,
                    onOptionChange = { updatedOption ->
                        extraOptions = extraOptions.toMutableList().also { options ->
                            options[index] = updatedOption
                        }
                    },
                    onRemove = {
                        extraOptions = extraOptions.toMutableList().also { options ->
                            options.removeAt(index)
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    extraOptions = extraOptions + EditableCustomizationOption()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text(stringResource(R.string.add_extra_option))
            }

            SectionTitle(text = stringResource(R.string.remove_ingredients))

            removableIngredientOptions.forEachIndexed { index, option ->
                ExtraOptionEditorCard(
                    option = option,
                    title = stringResource(R.string.removable_ingredient),
                    nameLabel = stringResource(R.string.ingredient_name),
                    imageLabel = stringResource(R.string.ingredient_image_url),
                    descriptionLabel = stringResource(R.string.option_description),
                    showPrice = false,
                    onOptionChange = { updatedOption ->
                        removableIngredientOptions = removableIngredientOptions.toMutableList().also { options ->
                            options[index] = updatedOption
                        }
                    },
                    onRemove = {
                        removableIngredientOptions = removableIngredientOptions.toMutableList().also { options ->
                            options.removeAt(index)
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    removableIngredientOptions = removableIngredientOptions + EditableCustomizationOption()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text(stringResource(R.string.add_removable_ingredient))
            }

            SectionTitle(text = stringResource(R.string.spice_level))

            ChezVousCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ChezVousSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.enable_spice_level),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(R.string.enable_spice_level_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isSpiceLevelEnabled,
                        onCheckedChange = { isSpiceLevelEnabled = it }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }

                TextButton(
                    onClick = {
                        attemptedSave = true
                        if (canSubmit) {
                            onSave(
                                item?.id.orEmpty(),
                                name,
                                description,
                                category,
                                price,
                                imageUrl,
                                extraOptions.mapNotNull { it.toCustomizationOption() },
                                removableIngredientOptions.mapNotNull { it.toCustomizationOption() },
                                isSpiceLevelEnabled
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverEditorSheet(
    driver: Driver?,
    onDismiss: () -> Unit,
    onSave: (
        driverId: String,
        fullName: String,
        phone: String,
        vehicleType: String,
        rating: String,
        isAvailable: Boolean
    ) -> Unit
) {
    var fullName by remember(driver?.id) { mutableStateOf(driver?.fullName.orEmpty()) }
    var phone by remember(driver?.id) { mutableStateOf(driver?.phone.orEmpty()) }
    var vehicleType by remember(driver?.id) { mutableStateOf(driver?.vehicleType.orEmpty()) }
    var rating by remember(driver?.id) {
        mutableStateOf(
            driver?.rating?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: "5"
        )
    }
    var isAvailable by remember(driver?.id) {
        mutableStateOf(driver?.isAvailable ?: true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .chezVousSheetPadding()
                .padding(bottom = ChezVousSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Text(
                text = if (driver == null) {
                    stringResource(R.string.partner_add_driver)
                } else {
                    stringResource(R.string.partner_edit_driver)
                },
                style = MaterialTheme.typography.titleLarge
            )

            ChezVousTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = stringResource(R.string.partner_driver_name)
            )

            ChezVousTextField(
                value = phone,
                onValueChange = { phone = it },
                label = stringResource(R.string.partner_driver_phone),
                keyboardType = KeyboardType.Phone
            )

            ChezVousTextField(
                value = vehicleType,
                onValueChange = { vehicleType = it },
                label = stringResource(R.string.partner_driver_vehicle)
            )

            ChezVousTextField(
                value = rating,
                onValueChange = { value ->
                    rating = value.filter { char ->
                        char.isDigit() || char == '.' || char == ','
                    }
                },
                label = stringResource(R.string.partner_driver_rating_label),
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )

            ChezVousCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ChezVousSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.partner_driver_available),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }

                TextButton(
                    onClick = {
                        onSave(
                            driver?.id.orEmpty(),
                            fullName,
                            phone,
                            vehicleType,
                            rating,
                            isAvailable
                        )
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserAccessEditorSheet(
    user: User,
    restaurants: List<Restaurant>,
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onSave: (
        role: String,
        restaurantIds: List<String>,
        driverId: String
    ) -> Unit
) {
    var selectedRole by remember(user.id) { mutableStateOf(user.role) }
    var selectedRestaurantIds by remember(user.id) {
        mutableStateOf(user.managedRestaurantIds.toSet())
    }
    var selectedDriverId by remember(user.id) {
        mutableStateOf(user.driverId)
    }
    val needsRestaurants = selectedRole == UserRoles.PARTNER ||
            selectedRole == UserRoles.RESTAURANT_ADMIN ||
            selectedRole == UserRoles.CHEF
    val needsDriver = selectedRole == UserRoles.DRIVER

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .chezVousSheetPadding()
                .padding(bottom = ChezVousSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Text(
                text = "Modifier l'acces",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = user.fullName.ifBlank { user.email.ifBlank { user.id } },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionTitle(text = "Role")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                items(editableRoles()) { role ->
                    CategoryChip(
                        text = role.roleDisplayLabel(),
                        selected = selectedRole == role,
                        onClick = {
                            selectedRole = role
                            if (
                                role != UserRoles.PARTNER &&
                                role != UserRoles.RESTAURANT_ADMIN &&
                                role != UserRoles.CHEF
                            ) {
                                selectedRestaurantIds = emptySet()
                            }
                            if (role != UserRoles.DRIVER) {
                                selectedDriverId = ""
                            }
                        }
                    )
                }
            }

            if (needsRestaurants) {
                SectionTitle(text = "Restaurants assignes")
                restaurants.forEach { restaurant ->
                    CategoryChip(
                        text = restaurant.name,
                        selected = restaurant.id in selectedRestaurantIds,
                        onClick = {
                            selectedRestaurantIds = if (restaurant.id in selectedRestaurantIds) {
                                selectedRestaurantIds - restaurant.id
                            } else {
                                selectedRestaurantIds + restaurant.id
                            }
                        }
                    )
                }
            }

            if (needsDriver) {
                SectionTitle(text = "Fiche livreur")
                if (drivers.isEmpty()) {
                    Text(
                        text = "Ajoutez d'abord un livreur dans la section Livreurs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                drivers.forEach { driver ->
                    CategoryChip(
                        text = driver.fullName,
                        selected = selectedDriverId == driver.id,
                        onClick = { selectedDriverId = driver.id }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }

                TextButton(
                    onClick = {
                        onSave(
                            selectedRole,
                            selectedRestaurantIds.toList(),
                            selectedDriverId
                        )
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

private fun String.roleDisplayLabel(): String {
    return when (this) {
        UserRoles.CUSTOMER -> "Client"
        UserRoles.PARTNER -> "Partenaire"
        UserRoles.RESTAURANT_ADMIN -> "Admin restaurant"
        UserRoles.CHEF -> "Chef"
        UserRoles.DRIVER -> "Livreur"
        UserRoles.ADMIN -> "Admin global"
        else -> this
    }
}

private fun Double.toEditableNumber(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}

@Composable
private fun ExtraOptionEditorCard(
    option: EditableCustomizationOption,
    title: String,
    nameLabel: String,
    imageLabel: String,
    descriptionLabel: String,
    showPrice: Boolean,
    onOptionChange: (EditableCustomizationOption) -> Unit,
    onRemove: () -> Unit
) {
    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.delete))
                }
            }

            ChezVousTextField(
                value = option.name,
                onValueChange = { onOptionChange(option.copy(name = it)) },
                label = nameLabel
            )

            if (showPrice) {
                ChezVousTextField(
                    value = option.price,
                    onValueChange = { value ->
                        onOptionChange(
                            option.copy(
                                price = value.filter { char ->
                                    char.isDigit() || char == '.' || char == ','
                                }
                            )
                        )
                    },
                    label = stringResource(R.string.extra_option_price),
                    keyboardType = KeyboardType.Number
                )
            }

            ChezVousTextField(
                value = option.imageUrl,
                onValueChange = { onOptionChange(option.copy(imageUrl = it)) },
                label = imageLabel
            )

            ChezVousTextField(
                value = option.description,
                onValueChange = { onOptionChange(option.copy(description = it)) },
                label = descriptionLabel,
                singleLine = false
            )
        }
    }
}

private data class EditableCustomizationOption(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val description: String = ""
)

private fun CustomizationOption.toEditableOption(): EditableCustomizationOption {
    return EditableCustomizationOption(
        id = id,
        name = name,
        price = if (price % 1.0 == 0.0) price.toInt().toString() else price.toString(),
        imageUrl = imageUrl,
        description = description
    )
}

private fun EditableCustomizationOption.toCustomizationOption(): CustomizationOption? {
    val cleanName = name.trim()
    if (cleanName.isBlank()) return null

    return CustomizationOption(
        id = id.ifBlank { cleanName.toOptionId() },
        name = cleanName,
        price = price.toPriceOrZero(),
        imageUrl = imageUrl.trim(),
        description = description.trim()
    )
}

private fun FoodItem.hasSpiceLevelSelection(): Boolean {
    return isSpiceLevelEnabled || spiceLevelOptions.isNotEmpty() || spiceLevels.isNotEmpty()
}

private fun String.toPriceOrZero(): Double {
    return replace(",", ".").toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
}

private fun String.toOptionId(): String {
    return lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "option" }
}
