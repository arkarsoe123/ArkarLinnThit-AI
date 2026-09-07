package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.service.ToolDefinition
import com.example.service.ToolRegistry

@Composable
fun ToolCatalogScreen(
    enabledTools: Map<String, Boolean>,
    onToggle: (String, Boolean)->Unit,
    onOpen: (com.example.model.ActiveToolScreen)->Unit,
    onBack: ()->Unit,
    onOpenAiSkills: ()->Unit,
    modifier: Modifier=Modifier
) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Capabilities", style=MaterialTheme.typography.headlineSmall)
            Text("Device tools + AI skills ကို တစ်နေရာတည်းက စီမံနိုင်ပါတယ်။", color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Surface(
                shape=RoundedCornerShape(16.dp),
                color=MaterialTheme.colorScheme.primaryContainer,
                modifier=Modifier.fillMaxWidth().clickable(onClick = onOpenAiSkills)
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, null)
                    Spacer(Modifier.padding(5.dp))
                    Column(Modifier.weight(1f)) {
                        Text("99+ AI Skills Library", style=MaterialTheme.typography.titleMedium)
                        Text("စာရေး၊ ဘာသာပြန်၊ Coding၊ Business၊ Study၊ Planning နှင့် အခြား skill များ", style=MaterialTheme.typography.bodySmall)
                    }
                    Text("Open")
                }
            }
        }
        items(ToolRegistry.all) { tool -> ToolRow(tool, enabledTools[tool.id] != false, { onToggle(tool.id, it) }) { onOpen(tool.screen) } }
        item { Spacer(Modifier.height(8.dp)); Surface(shape=RoundedCornerShape(14.dp), color=MaterialTheme.colorScheme.surfaceVariant, modifier=Modifier.fillMaxWidth().clickable(onClick=onBack)) { Text("Settings သို့ပြန်", modifier=Modifier.padding(14.dp)) } }
    }
}

@Composable private fun ToolRow(tool: ToolDefinition, enabled: Boolean, onToggle:(Boolean)->Unit, onOpen:()->Unit) {
    Surface(shape=RoundedCornerShape(18.dp), color=MaterialTheme.colorScheme.surfaceVariant, modifier=Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha=.06f), RoundedCornerShape(18.dp)).clickable(onClick=onOpen)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Default.Build, null, tint=if(enabled) Color(0xFF6C8CFF) else Color.Gray)
            Spacer(Modifier.padding(4.dp))
            Column(Modifier.weight(1f)) { Text(tool.title); Text(tool.description, color=MaterialTheme.colorScheme.onSurfaceVariant, style=MaterialTheme.typography.bodySmall) }
            Switch(checked=enabled, onCheckedChange=onToggle)
        }
    }
}
