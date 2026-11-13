package com.peyya.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peyya.app.data.PlaceSearchCatalog
import com.peyya.app.ui.theme.PeyyaBrand

@Composable
fun PlaceSearchField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val results = remember(value, focused) {
        if (focused || value.isNotBlank()) PlaceSearchCatalog.search(value) else emptyList()
    }
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                focused = true
            },
            label = { Text(label) },
            placeholder = { Text("동·역·건물명으로 검색") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = PeyyaBrand) },
        )
        if (focused && results.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
            ) {
                Column(Modifier.fillMaxWidth()) {
                    results.forEach { item ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(item.label)
                                    focused = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(item.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                item.detail,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        if (value.isNotBlank() && !focused) {
            Spacer(Modifier.height(4.dp))
            Text(
                "선택됨 · 다시 검색하려면 입력창을 수정하세요",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
