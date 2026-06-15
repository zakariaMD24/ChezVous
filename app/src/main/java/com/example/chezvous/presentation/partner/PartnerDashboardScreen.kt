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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.R
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.PartnerMenuItemCard
import com.example.chezvous.ui.components.PartnerOrderCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.components.chezVousSheetPadding
import com.example.chezvous.ui.components.nextPartnerStatus
import com.example.chezvous.ui.theme.ChezVousSpacing
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardScreen(
    onBack: () -> Unit
) {
    val viewModel: PartnerDashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message, uiState.errorMessage) {
        if (uiState.message != null || uiState.errorMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = "Espace partenaire",
                onBack = onBack
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
                    onUpdateOrderStatus = viewModel::updateOrderStatus,
                    onAvailabilityChange = viewModel::updateItemAvailability,
                    onEditItem = {
                        editingItem = it
                        showEditor = true
                    },
                    onAddItem = {
                        editingItem = null
                        showEditor = true
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
            onSave = { itemId, name, description, category, price, imageUrl, extraOptions, removableIngredientOptions, spiceLevelOptions ->
                viewModel.saveMenuItem(
                    itemId = itemId,
                    name = name,
                    description = description,
                    category = category,
                    priceText = price,
                    imageUrl = imageUrl,
                    extraOptions = extraOptions,
                    removableIngredientOptions = removableIngredientOptions,
                    spiceLevelOptions = spiceLevelOptions
                )
                showEditor = false
            }
        )
    }
}

@Composable
private fun PartnerDashboardContent(
    uiState: PartnerDashboardUiState,
    onRestaurantSelected: (String) -> Unit,
    onUpdateOrderStatus: (com.example.chezvous.data.model.Order, com.example.chezvous.data.model.OrderStatus) -> Unit,
    onAvailabilityChange: (FoodItem, Boolean) -> Unit,
    onEditItem: (FoodItem) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.selectedRestaurant?.name ?: "Restaurant",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Role: ${uiState.role}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                items(uiState.restaurants) { restaurant ->
                    CategoryChip(
                        text = restaurant.name,
                        selected = uiState.selectedRestaurantId == restaurant.id,
                        onClick = { onRestaurantSelected(restaurant.id) }
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

        item {
            SectionTitle(text = "Commandes recues")
        }

        if (uiState.orders.isEmpty()) {
            item {
                EmptyPartnerSection(
                    text = "Aucune commande pour ce restaurant."
                )
            }
        }

        items(uiState.orders) { order ->
            PartnerOrderCard(
                order = order,
                nextStatus = order.status.nextPartnerStatus(),
                onUpdateStatus = { nextStatus ->
                    onUpdateOrderStatus(order, nextStatus)
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(
                    text = "Menu",
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(onClick = onAddItem) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("Ajouter")
                }
            }
        }

        if (uiState.menuItems.isEmpty()) {
            item {
                EmptyPartnerSection(
                    text = "Aucun plat pour ce restaurant."
                )
            }
        }

        items(uiState.menuItems) { foodItem ->
            PartnerMenuItemCard(
                foodItem = foodItem,
                onAvailabilityChange = { isAvailable ->
                    onAvailabilityChange(foodItem, isAvailable)
                },
                onEditClick = { onEditItem(foodItem) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
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
        spiceLevelOptions: List<CustomizationOption>
    ) -> Unit
) {
    var name by remember(item?.id) { mutableStateOf(item?.name.orEmpty()) }
    var description by remember(item?.id) { mutableStateOf(item?.description.orEmpty()) }
    var category by remember(item?.id) { mutableStateOf(item?.category ?: "Menu") }
    var imageUrl by remember(item?.id) { mutableStateOf(item?.imageUrl.orEmpty()) }
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
    var spiceLevelOptions by remember(item?.id) {
        val existingOptions = item?.spiceLevelOptions.orEmpty()
        val legacyLevels = item?.spiceLevels.orEmpty()
        mutableStateOf(
            existingOptions
                .ifEmpty {
                    legacyLevels.map { level ->
                        CustomizationOption(id = level.toOptionId(), name = level)
                    }
                }
                .map { it.toEditableOption() }
        )
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
                label = stringResource(R.string.food_name)
            )

            ChezVousTextField(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.description),
                singleLine = false
            )

            ChezVousTextField(
                value = category,
                onValueChange = { category = it },
                label = stringResource(R.string.category)
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
                imeAction = ImeAction.Done
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

            spiceLevelOptions.forEachIndexed { index, option ->
                ExtraOptionEditorCard(
                    option = option,
                    title = stringResource(R.string.spice_level_option),
                    nameLabel = stringResource(R.string.spice_level_name),
                    imageLabel = stringResource(R.string.spice_level_image_url),
                    descriptionLabel = stringResource(R.string.spice_level_description),
                    showPrice = false,
                    onOptionChange = { updatedOption ->
                        spiceLevelOptions = spiceLevelOptions.toMutableList().also { options ->
                            options[index] = updatedOption
                        }
                    },
                    onRemove = {
                        spiceLevelOptions = spiceLevelOptions.toMutableList().also { options ->
                            options.removeAt(index)
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    spiceLevelOptions = spiceLevelOptions + EditableCustomizationOption()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text(stringResource(R.string.add_spice_level))
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
                            item?.id.orEmpty(),
                            name,
                            description,
                            category,
                            price,
                            imageUrl,
                            extraOptions.mapNotNull { it.toCustomizationOption() },
                            removableIngredientOptions.mapNotNull { it.toCustomizationOption() },
                            spiceLevelOptions.mapNotNull { it.toCustomizationOption() }
                        )
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
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

private fun String.toPriceOrZero(): Double {
    return replace(",", ".").toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
}

private fun String.toOptionId(): String {
    return lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "option" }
}
