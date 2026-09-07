package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpreadsheetScreen(
    cells: Map<String, String>,
    onUpdateCell: (String, String) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = listOf("A", "B", "C", "D")
    val rows = (1..7).toList()

    var editingCellKey by remember { mutableStateOf<String?>(null) }
    var cellInputValue by remember { mutableStateOf("") }

    // Compute simple sum of numeric values in column D or all numbers
    val totalSum = cells.values.mapNotNull { it.toDoubleOrNull() }.sum()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "စာရင်းဇယား (Smart Spreadsheet)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "AI Formula & Dynamic Matrix Grid",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00F0FF))
                )
            }

            Button(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("ပင်မသို့", color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total calculation banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calc", tint = Color(0xFF00F0FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ဂဏန်း စုစုပေါင်း (Auto Sum):",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                    )
                }
                Text(
                    text = "%,.0f ကျပ်".format(totalSum),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Matrix Grid Table
        val hScroll = rememberScrollState()
        val vScroll = rememberScrollState()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .background(Color(0xFF0B1120), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .horizontalScroll(hScroll)
                    .verticalScroll(vScroll)
            ) {
                // Table Header (Row 0 with column letters)
                Row {
                    HeaderCell("#", width = 36.dp)
                    columns.forEach { col ->
                        HeaderCell(col, width = 96.dp)
                    }
                }

                // Data Rows
                rows.forEach { r ->
                    Row {
                        HeaderCell(r.toString(), width = 36.dp)
                        columns.forEach { col ->
                            val key = "$col$r"
                            val value = cells[key] ?: ""
                            DataCell(
                                value = value,
                                width = 96.dp,
                                onClick = {
                                    editingCellKey = key
                                    cellInputValue = value
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Table Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onClearAll,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ဇယား ရှင်းလင်းမည်", color = MaterialTheme.colorScheme.onSurface)
            }

            Text(
                text = "Cell ကိုနှိပ်၍ စာရင်းများ ပြင်ဆင်နိုင်သည်",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 11.sp)
            )
        }
    }

    // Cell Edit Dialog
    if (editingCellKey != null) {
        AlertDialog(
            onDismissRequest = { editingCellKey = null },
            title = { Text("Cell [$editingCellKey] ပြင်ဆင်ရန်") },
            text = {
                OutlinedTextField(
                    value = cellInputValue,
                    onValueChange = { cellInputValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingCellKey?.let { onUpdateCell(it, cellInputValue) }
                        editingCellKey = null
                    }
                ) {
                    Text("သတ်မှတ်မည်")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCellKey = null }) {
                    Text("မလုပ်တော့ပါ")
                }
            }
        )
    }
}

@Composable
fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Surface(
        color = Color(0xFF1E293B),
        modifier = Modifier
            .size(width, 36.dp)
            .border(0.5.dp, Color(0xFF334155))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00F0FF)
                )
            )
        }
    }
}

@Composable
fun DataCell(value: String, width: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .size(width, 36.dp)
            .border(0.5.dp, Color(0xFF1E293B))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}
