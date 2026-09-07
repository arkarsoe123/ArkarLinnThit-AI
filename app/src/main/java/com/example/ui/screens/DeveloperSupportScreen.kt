package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val K_PAY = "09691529743"

@Composable
fun DeveloperSupportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun copyKPay() {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("KPay", K_PAY))
        Toast.makeText(context, "KPay နံပါတ် copy လုပ်ပြီးပါပြီ", Toast.LENGTH_SHORT).show()
    }

    fun openKPay() {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage("com.dagonmobile.kpay")
        if (intent != null) {
            context.startActivity(intent)
            Toast.makeText(context, "KPay ဖွင့်ပေးလိုက်ပါပြီ။ လွှဲမည့်အကောင့်ကို ကိုယ်တိုင်စစ်ဆေးပြီး အတည်ပြုပါ။", Toast.LENGTH_LONG).show()
        } else {
            copyKPay()
            Toast.makeText(context, "KPay app မတွေ့ပါ။ နံပါတ်ကို copy လုပ်ထားပါတယ်။", Toast.LENGTH_LONG).show()
        }
    }

    fun shareSupport() {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Arkar Lin Thit AI ကို ပံ့ပိုးရန် KPay: $K_PAY")
        }
        context.startActivity(Intent.createChooser(send, "Support information မျှဝေရန်"))
    }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text("Developer Mr.A", fontSize = 25.sp)
                Text("Arkar Lin Thit AI · v1.6.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.Person, null)
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Developer", style = MaterialTheme.typography.titleLarge)
                Text("Mr.A", fontSize = 22.sp)
                Text("Arkar Lin Thit AI ကို AI-first, chat-first assistant အဖြစ် တည်ဆောက်နေသော developer page ဖြစ်ပါတယ်။")
                Text("v1.6.0 တွင် 99+ AI skills, device tools, memory, voice, model/provider settings နှင့် developer support ကို တစ်နေရာတည်းမှာ စုစည်းထားပါတယ်။", fontSize = 13.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Support / KPay", style = MaterialTheme.typography.titleLarge)
                }
                Text("ပံ့ပိုးကူညီချင်သူများအတွက် KPay")
                Text(K_PAY, fontSize = 25.sp, style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = ::copyKPay, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.padding(3.dp)); Text("Copy")
                    }
                    Button(onClick = ::openKPay, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.OpenInNew, null); Spacer(Modifier.padding(3.dp)); Text("Open KPay")
                    }
                }
                OutlinedButton(onClick = ::shareSupport, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Share, null); Spacer(Modifier.padding(3.dp)); Text("Support info မျှဝေရန်")
                }
                Text("ငွေလွှဲမှုကို app က အလိုအလျောက် အတည်ပြုမပေးပါ။ KPay ထဲတွင် လက်ခံသူ/ပမာဏကို ကိုယ်တိုင်စစ်ဆေးပြီးမှ Confirm လုပ်ပါ။", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
