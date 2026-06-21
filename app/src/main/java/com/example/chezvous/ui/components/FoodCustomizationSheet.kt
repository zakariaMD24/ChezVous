package com.example.chezvous.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chezvous.R
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.ui.theme.ChezVousSpacing
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCustomizationSheet(
    foodItem: FoodItem,
    onDismiss: () -> Unit,
    onConfirm: (
        selectedExtras: List<CustomizationOption>,
        removedIngredients: List<String>,
        spiceLevel: String,
        instruction: String
    ) -> Unit
) {
    val extras = foodItem.availableExtraOptions()
    val removableIngredients = foodItem.availableRemovableIngredientOptions()
    val spiceOptions = foodItem.availableSpiceLevelOptions()

    var selectedExtraIds by remember(foodItem.id) { mutableStateOf(emptySet<String>()) }
    var removedIngredientIds by remember(foodItem.id) { mutableStateOf(emptySet<String>()) }
    var spiceIndex by remember(foodItem.id, spiceOptions.size) { mutableStateOf(0f) }
    var instruction by remember(foodItem.id) { mutableStateOf("") }

    val selectedExtras = extras.filter { it.optionKey in selectedExtraIds }
    val removedIngredientNames = removableIngredients
        .filter { it.optionKey in removedIngredientIds }
        .map { it.name }

    val safeSpiceIndex = spiceIndex.roundToInt()
        .coerceIn(0, spiceOptions.lastIndex.coerceAtLeast(0))

    val selectedSpice = spiceOptions.getOrNull(safeSpiceIndex)?.name.orEmpty()

    val extrasTotal = selectedExtras.sumOf { it.price }
    val finalUnitPrice = foodItem.price + extrasTotal

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ChezVousSpacing.sheetHorizontal)
                .padding(bottom = ChezVousSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            SheetHandle()

            CustomizationHeader(
                foodItem = foodItem,
                finalUnitPrice = finalUnitPrice
            )

            if (extras.isNotEmpty()) {
                CustomizationSectionHeader(
                    title = stringResource(R.string.extras),
                    subtitle = "Optionnel"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
                ) {
                    items(extras) { option ->
                        CustomizationOptionCard(
                            option = option,
                            selected = option.optionKey in selectedExtraIds,
                            selectedMode = OptionSelectedMode.Add,
                            showPrice = true,
                            onClick = {
                                selectedExtraIds = selectedExtraIds.toggle(option.optionKey)
                            }
                        )
                    }
                }
            }

            if (removableIngredients.isNotEmpty()) {
                CustomizationSectionHeader(
                    title = stringResource(R.string.remove_ingredients),
                    subtitle = "Optionnel"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
                ) {
                    items(removableIngredients) { option ->
                        CustomizationOptionCard(
                            option = option,
                            selected = option.optionKey in removedIngredientIds,
                            selectedMode = OptionSelectedMode.Remove,
                            showPrice = false,
                            onClick = {
                                removedIngredientIds = removedIngredientIds.toggle(option.optionKey)
                            }
                        )
                    }
                }

                Text(
                    text = "Sélectionnez les ingrédients à enlever.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (spiceOptions.isNotEmpty()) {
                CustomizationSectionHeader(
                    title = stringResource(R.string.spice_level),
                    subtitle = selectedSpice.ifBlank { "Optionnel" }
                )

                SpiceLevelSlider(
                    options = spiceOptions,
                    selectedIndex = safeSpiceIndex,
                    onSelectedIndexChange = { index ->
                        spiceIndex = index.toFloat()
                    }
                )
            }

            ChezVousTextField(
                value = instruction,
                onValueChange = { instruction = it.take(120) },
                label = stringResource(R.string.item_instruction_label),
                leadingIcon = Icons.Outlined.EditNote,
                imeAction = ImeAction.Done
            )

            PriceBreakdown(
                basePrice = foodItem.price,
                extrasTotal = extrasTotal,
                totalPrice = finalUnitPrice
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }

            ChezVousButton(
                text = stringResource(R.string.add_customized_item, finalUnitPrice.asDhPrice()),
                onClick = {
                    onConfirm(
                        selectedExtras,
                        removedIngredientNames,
                        selectedSpice,
                        instruction.trim()
                    )
                }
            )

            Spacer(modifier = Modifier.height(ChezVousSpacing.xs))
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(46.dp)
                .height(5.dp)
                .clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.outlineVariant
        ) {}
    }
}

@Composable
private fun CustomizationHeader(
    foodItem: FoodItem,
    finalUnitPrice: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            if (foodItem.displayFoodImageUrl().isNotBlank()) {
                AsyncImage(
                    model = foodItem.displayFoodImageUrl(),
                    contentDescription = foodItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PlaceholderImage(
                    modifier = Modifier.fillMaxSize(),
                    text = "NEW"
                )
            }
        }

        Spacer(modifier = Modifier.width(ChezVousSpacing.md))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
        ) {
            Text(
                text = stringResource(R.string.customize_item),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = foodItem.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = foodItem.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = finalUnitPrice.asDhPrice(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CustomizationSectionHeader(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private enum class OptionSelectedMode {
    Add,
    Remove
}

@Composable
private fun CustomizationOptionCard(
    option: CustomizationOption,
    selected: Boolean,
    selectedMode: OptionSelectedMode,
    showPrice: Boolean,
    onClick: () -> Unit
) {
    val activeColor = when (selectedMode) {
        OptionSelectedMode.Add -> MaterialTheme.colorScheme.primary
        OptionSelectedMode.Remove -> MaterialTheme.colorScheme.error
    }

    val containerColor = if (selected) {
        when (selectedMode) {
            OptionSelectedMode.Add -> MaterialTheme.colorScheme.primaryContainer
            OptionSelectedMode.Remove -> MaterialTheme.colorScheme.errorContainer
        }
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .width(112.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                CustomizationOptionImage(
                    option = option,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    fallbackTint = activeColor
                )

                if (selected) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp),
                        shape = MaterialTheme.shapes.large,
                        color = activeColor
                    ) {
                        Icon(
                            imageVector = if (selectedMode == OptionSelectedMode.Add) {
                                Icons.Outlined.Check
                            } else {
                                Icons.Outlined.RemoveCircleOutline
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

            Text(
                text = option.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (showPrice) {
                Text(
                    text = if (option.price > 0.0) "+${option.price.asDhPrice()}" else stringResource(R.string.included),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CustomizationOptionImage(
    option: CustomizationOption,
    modifier: Modifier,
    fallbackTint: Color
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (option.displayCustomizationImageUrl().isNotBlank()) {
            AsyncImage(
                model = option.displayCustomizationImageUrl(),
                contentDescription = option.name,
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
                    tint = fallbackTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaceholderImage(
    modifier: Modifier,
    text: String
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SpiceLevelSlider(
    options: List<CustomizationOption>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    if (options.isEmpty()) return

    val safeOptions = options
    val lastIndex = safeOptions.lastIndex.coerceAtLeast(0)
    val safeSelectedIndex = selectedIndex.coerceIn(0, lastIndex)
    val selectedOption = safeOptions[safeSelectedIndex]
    val tone = standardSpiceTone(safeSelectedIndex, lastIndex)
    val description = selectedOption.description.ifBlank {
        defaultSpiceDescription(safeSelectedIndex, lastIndex)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tone.container,
        border = BorderStroke(1.dp, tone.color)
    ) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = MaterialTheme.shapes.large,
                    color = Color.White.copy(alpha = 0.72f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tone.icon,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(ChezVousSpacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedOption.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = tone.color,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Slider(
                value = safeSelectedIndex.toFloat(),
                onValueChange = { value ->
                    onSelectedIndexChange(value.roundToInt().coerceIn(0, lastIndex))
                },
                valueRange = 0f..lastIndex.coerceAtLeast(1).toFloat(),
                steps = (safeOptions.size - 2).coerceAtLeast(0),
                colors = SliderDefaults.colors(
                    thumbColor = tone.color,
                    activeTrackColor = tone.color,
                    inactiveTrackColor = tone.color.copy(alpha = 0.25f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                safeOptions.forEachIndexed { index, option ->
                    val itemTone = standardSpiceTone(index, lastIndex)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = itemTone.icon,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (index == safeSelectedIndex) {
                                tone.color
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (index == safeSelectedIndex) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class SpiceTone(
    val color: Color,
    val container: Color,
    val icon: String
)

private fun spiceTone(index: Int, lastIndex: Int): SpiceTone {
    val ratio = if (lastIndex <= 0) 0f else index.toFloat() / lastIndex.toFloat()

    return when {
        ratio <= 0f -> SpiceTone(
            color = Color(0xFF78909C),
            container = Color(0xFFECEFF1),
            icon = "🥛"
        )
        ratio < 0.34f -> SpiceTone(
            color = Color(0xFF2E7D32),
            container = Color(0xFFE8F5E9),
            icon = "🫑"
        )
        ratio < 0.67f -> SpiceTone(
            color = Color(0xFFF57F17),
            container = Color(0xFFFFF8E1),
            icon = "🌶️"
        )
        ratio < 0.9f -> SpiceTone(
            color = Color(0xFFEF6C00),
            container = Color(0xFFFFF3E0),
            icon = "🔥"
        )
        else -> SpiceTone(
            color = Color(0xFFC62828),
            container = Color(0xFFFFEBEE),
            icon = "🌶️🔥"
        )
    }
}

private fun standardSpiceTone(index: Int, lastIndex: Int): SpiceTone {
    val ratio = if (lastIndex <= 0) 0f else index.toFloat() / lastIndex.toFloat()

    return when {
        ratio <= 0f -> SpiceTone(
            color = Color(0xFF2E7D32),
            container = Color(0xFFE8F5E9),
            icon = "\uD83C\uDF36\uFE0F"
        )
        ratio < 0.5f -> SpiceTone(
            color = Color(0xFFF57F17),
            container = Color(0xFFFFF8E1),
            icon = "\uD83C\uDF36\uFE0F"
        )
        ratio < 0.85f -> SpiceTone(
            color = Color(0xFFEF6C00),
            container = Color(0xFFFFF3E0),
            icon = "\uD83C\uDF36\uFE0F"
        )
        else -> SpiceTone(
            color = Color(0xFFC62828),
            container = Color(0xFFFFEBEE),
            icon = "\uD83C\uDF36\uFE0F\uD83D\uDD25"
        )
    }
}

@Composable
private fun defaultSpiceDescription(index: Int, lastIndex: Int): String {
    val ratio = if (lastIndex <= 0) 0f else index.toFloat() / lastIndex.toFloat()

    return when {
        ratio <= 0f -> stringResource(R.string.spice_description_mild)
        ratio < 0.5f -> stringResource(R.string.spice_description_medium)
        ratio < 0.85f -> stringResource(R.string.spice_description_hot)
        else -> stringResource(R.string.spice_description_very_hot)
    }
}

@Composable
private fun PriceBreakdown(
    basePrice: Double,
    extrasTotal: Double,
    totalPrice: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
    ) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            PriceLine(
                label = stringResource(R.string.base_price),
                value = basePrice.asDhPrice()
            )

            PriceLine(
                label = stringResource(R.string.extras),
                value = extrasTotal.asDhPrice()
            )

            PriceLine(
                label = stringResource(R.string.total),
                value = totalPrice.asDhPrice(),
                strong = true
            )
        }
    }
}

@Composable
private fun PriceLine(
    label: String,
    value: String,
    strong: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (strong) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = value,
            style = if (strong) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (strong) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private val CustomizationOption.optionKey: String
    get() = id.ifBlank { name }

private fun Set<String>.toggle(value: String): Set<String> {
    return if (value in this) this - value else this + value
}

private fun FoodItem.availableExtraOptions(): List<CustomizationOption> {
    return extraOptions
}

private fun FoodItem.availableRemovableIngredientOptions(): List<CustomizationOption> {
    return removableIngredientOptions.ifEmpty {
        removableIngredients.map { ingredient ->
            CustomizationOption(
                id = ingredient.toOptionKey(),
                name = ingredient
            )
        }
    }
}

@Composable
private fun FoodItem.availableSpiceLevelOptions(): List<CustomizationOption> {
    if (!hasSpiceLevelSelection()) return emptyList()

    if (spiceLevelOptions.isNotEmpty()) return spiceLevelOptions

    if (spiceLevels.isNotEmpty()) {
        return spiceLevels.map { level ->
            CustomizationOption(
                id = level.toOptionKey(),
                name = level
            )
        }
    }

    return listOf(
        CustomizationOption(
            id = "mild",
            name = stringResource(R.string.spice_mild),
            description = stringResource(R.string.spice_description_mild)
        ),
        CustomizationOption(
            id = "medium",
            name = stringResource(R.string.spice_normal),
            description = stringResource(R.string.spice_description_medium)
        ),
        CustomizationOption(
            id = "spicy",
            name = stringResource(R.string.spice_spicy),
            description = stringResource(R.string.spice_description_hot)
        ),
        CustomizationOption(
            id = "extra-spicy",
            name = stringResource(R.string.spice_very_spicy),
            description = stringResource(R.string.spice_description_very_hot)
        )
    )
}

private fun FoodItem.hasSpiceLevelSelection(): Boolean {
    return isSpiceLevelEnabled || spiceLevelOptions.isNotEmpty() || spiceLevels.isNotEmpty()
}

private fun String.toOptionKey(): String {
    return lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { this }
}
