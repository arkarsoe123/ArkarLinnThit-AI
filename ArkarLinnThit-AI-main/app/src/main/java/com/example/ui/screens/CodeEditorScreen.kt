package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeFile

@Composable
fun CodeEditorScreen(
    files: List<CodeFile>,
    activeFileIndex: Int,
    terminalLogs: String,
    isTerminalOpen: Boolean,
    recentFileNames: List<String>,
    onSelectFile: (Int) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveFile: () -> Unit,
    onNewFile: (String, String) -> Unit,
    onRunCode: () -> Unit,
    onToggleTerminal: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showRecentMenu by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("script.kt") }

    val activeFile = files.getOrNull(activeFileIndex) ?: CodeFile(name = "Empty.txt", language = "text", content = "// No file")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // TOP TOOLBAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // New File
            ToolbarButton(
                icon = Icons.Default.Add,
                label = "New",
                onClick = { showNewFileDialog = true },
                tag = "code_toolbar_new"
            )

            // Save File
            ToolbarButton(
                icon = Icons.Default.Save,
                label = "Save",
                onClick = onSaveFile,
                tag = "code_toolbar_save"
            )

            // Recent Files
            Box {
                ToolbarButton(
                    icon = Icons.Default.History,
                    label = "Recent",
                    onClick = { showRecentMenu = true },
                    tag = "code_toolbar_recent"
                )
                DropdownMenu(
                    expanded = showRecentMenu,
                    onDismissRequest = { showRecentMenu = false }
                ) {
                    recentFileNames.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                val idx = files.indexOfFirst { it.name == name }
                                if (idx >= 0) onSelectFile(idx)
                                showRecentMenu = false
                            }
                        )
                    }
                }
            }

            // Toggle Terminal
            ToolbarButton(
                icon = Icons.Default.Terminal,
                label = if (isTerminalOpen) "Hide Term" else "Terminal",
                onClick = onToggleTerminal,
                tag = "code_toolbar_terminal"
            )

            // RUN CODE ACTION
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981),
                modifier = Modifier
                    .clickable { onRunCode() }
                    .testTag("code_toolbar_run")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Run Code",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Back button
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("ပင်မသို့", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // FILE TABS
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(files) { idx, file ->
                val isSelected = idx == activeFileIndex
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155),
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                        .clickable { onSelectFile(idx) }
                        .testTag("code_tab_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = file.name + if (file.isModified) " *" else "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color(0xFF00F0FF) else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }

        // CODE TEXT EDITOR AREA
        Card(
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
            modifier = Modifier
                .weight(if (isTerminalOpen) 0.6f else 1f)
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Line Numbers Gutter
                val lineCount = activeFile.content.lines().size.coerceAtLeast(1)
                Column(
                    modifier = Modifier
                        .background(Color(0xFF161B22))
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    for (i in 1..lineCount.coerceAtMost(30)) {
                        Text(
                            text = "%2d".format(i),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF484F58),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Editor TextArea
                OutlinedTextField(
                    value = activeFile.content,
                    onValueChange = onContentChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE6EDF3),
                        fontSize = 13.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFFE6EDF3),
                        unfocusedTextColor = Color(0xFFE6EDF3)
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_textarea")
                )
            }
        }

        // TERMINAL OUTPUT DRAWER
        AnimatedVisibility(
            visible = isTerminalOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF040D1A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 8.dp)
                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("code_terminal_window")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Arkar AI Sandbox Terminal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF00F0FF),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        IconButton(
                            onClick = onToggleTerminal,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Terminal",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val scroll = rememberScrollState()
                    Text(
                        text = terminalLogs,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scroll)
                    )
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("ဖိုင်အသစ် ဖန်တီးရန်") },
            text = {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    label = { Text("ဖိုင်အမည် (ဥပမာ: script.kt, app.py)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ext = newFileNameInput.substringAfterLast(".", "text")
                        val lang = when (ext) {
                            "kt" -> "kotlin"
                            "py" -> "python"
                            "html" -> "html"
                            "js" -> "javascript"
                            else -> "text"
                        }
                        onNewFile(newFileNameInput, lang)
                        showNewFileDialog = false
                    }
                ) {
                    Text("ဖန်တီးမည်")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("မလုပ်တော့ပါ")
                }
            }
        )
    }
}

@Composable
fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color(0xFF00F0FF), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
