package com.example.chezvous.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.ui.components.CategoryChip
import com.example.chezvous.ui.components.ChezVousSearchBar
import com.example.chezvous.ui.components.RestaurantCard
import com.example.chezvous.ui.components.SectionTitle

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = viewModel()
    val restaurants by viewModel.restaurants.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }

    val categories = listOf("Tous", "Fast Food", "Pizza", "Healthy")

    Scaffold(
        topBar = {
            HomeTopBar()
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
                    text = "Bonjour 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Que voulez-vous manger aujourd’hui ?",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                ChezVousSearchBar(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchRestaurants(it)
                    }
                )
            }

            item {
                LazyRow {
                    items(categories) { category ->
                        CategoryChip(
                            text = category,
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category

                                if (category == "Tous") {
                                    viewModel.searchRestaurants(searchQuery)
                                } else {
                                    searchQuery = category
                                    viewModel.searchRestaurants(category)
                                }
                            }
                        )
                    }
                }
            }

            item {
                SectionTitle(text = "Restaurants partenaires")
            }

            items(restaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = {
                        // Plus tard: aller vers RestaurantDetailsScreen
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
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
                        text = "Livraison à domicile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}