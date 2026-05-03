package com.iptv.player.ui.components
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptv.player.ui.theme.*

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query, onValueChange = onQueryChange, modifier = modifier.fillMaxWidth(),
        placeholder = { Text("搜索频道...", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Filled.Search, "搜索", tint = TextSecondary) },
        trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Filled.Clear, "清除", tint = TextSecondary) } },
        singleLine = true, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue.copy(alpha = 0.5f), unfocusedBorderColor = DarkSurfaceVariant,
            focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface,
            cursorColor = PrimaryBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
        )
    )
}
