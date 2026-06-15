package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.chezvous.R
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun FilterSortActionRow(
    filterCount: Int,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = onSortClick,
            modifier = Modifier
                .weight(1f)
                .height(ChezVousSize.buttonHeight),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(
                imageVector = Icons.Outlined.Sort,
                contentDescription = null,
                modifier = Modifier.size(ChezVousSize.iconMd)
            )

            Spacer(modifier = Modifier.width(ChezVousSpacing.xs))

            Text(
                text = stringResource(R.string.sort),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(ChezVousSpacing.sm))

        OutlinedButton(
            onClick = onFilterClick,
            modifier = Modifier
                .weight(1f)
                .height(ChezVousSize.buttonHeight),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = null,
                modifier = Modifier.size(ChezVousSize.iconMd)
            )

            Spacer(modifier = Modifier.width(ChezVousSpacing.xs))

            Text(
                text = if (filterCount > 0) {
                    stringResource(R.string.filter_count_format, filterCount)
                } else {
                    stringResource(R.string.filter)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
