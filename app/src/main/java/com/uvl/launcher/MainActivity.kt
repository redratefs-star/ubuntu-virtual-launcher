package com.uvl.launcher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val UbuntuOrange = Color(0xFFE95420)
private val UbuntuDark = Color(0xFF2C001E)
private val UbuntuPanel = Color(0xFF3D1832)
private val UbuntuCream = Color(0xFFFFF7F2)

class MainActivity : ComponentActivity() {
    private val termuxPackage = "com.termux"
    private val termuxRunPermission = "com.termux.permission.RUN_COMMAND"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = UbuntuOrange,
                    secondary = Color(0xFF77216F),
                    background = UbuntuCream,
                    surface = Color.White
                )
            ) {
                LauncherScreen(
                    onInstall = { runTermux("install") },
                    onStart = { runTermux("start") },
                    onStop = { runTermux("stop") },
                    onTerminal = { openTermux() },
                    onGithub = {
                        openUrl("https://github.com/login")
                    },
                    onRepository = {
                        openUrl("https://github.com/new")
                    },
                    onVnc = {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnc://127.0.0.1:5901")))
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(this, "Pasang aplikasi VNC Viewer seperti AVNC, lalu buka vnc://127.0.0.1:5901", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }

    private fun openTermux() {
        val launch = packageManager.getLaunchIntentForPackage(termuxPackage)
        if (launch != null) startActivity(launch)
        else Toast.makeText(this, "Termux belum terpasang.", Toast.LENGTH_LONG).show()
    }

    private fun runTermux(action: String) {
        val script = when (action) {
            "install" -> "\$HOME/uvl/ubuntu/install.sh"
            "start" -> "\$HOME/uvl/ubuntu/start.sh"
            "stop" -> "\$HOME/uvl/ubuntu/stop.sh"
            else -> ""
        }
        val command = when (action) {
            "terminal" -> "/data/data/com.termux/files/usr/bin/bash"
            else -> "/data/data/com.termux/files/usr/bin/bash"
        }
        try {
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setClassName(termuxPackage, "com.termux.app.RunCommandService")
                putExtra("com.termux.RUN_COMMAND_PATH", command)
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS",
                    if (action == "terminal") arrayOf("-l") else arrayOf("-lc", script))
                putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
            }
            if (checkSelfPermission(termuxRunPermission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izinkan aplikasi eksternal menjalankan perintah di Termux, lalu coba lagi.", Toast.LENGTH_LONG).show()
                return
            }
            startService(intent)
            Toast.makeText(this, "Perintah dikirim ke Termux: $action", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Termux belum siap. Instal Termux dan aktifkan izin RUN_COMMAND. Detail: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

@Composable
fun LauncherScreen(
    onInstall: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTerminal: () -> Unit,
    onGithub: () -> Unit,
    onRepository: () -> Unit,
    onVnc: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = UbuntuCream) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Laptop, contentDescription = null, tint = UbuntuOrange, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Ubuntu", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = UbuntuDark)
                    Text("VIRTUAL LAUNCHER", fontSize = 11.sp, color = Color.Gray, letterSpacing = 2.sp)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showHelp = !showHelp }) {
                    Icon(Icons.Default.HelpOutline, contentDescription = "Bantuan", tint = UbuntuDark)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().background(UbuntuDark, RoundedCornerShape(22.dp)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Welcome to Ubuntu", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Linux desktop environment on your Android device.", color = Color(0xFFFFD8C8), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("Ubuntu ARM64")
                    StatusChip("XFCE Desktop")
                }
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("START COMPUTER", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onVnc,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Icon(Icons.Default.DesktopWindows, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Open Desktop Viewer") }
            }

            Text("Computer controls", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UbuntuDark)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ControlCard("Install Ubuntu", "First-time setup", Icons.Default.Download, Modifier.weight(1f), onInstall)
                ControlCard("Stop Linux", "Stop desktop session", Icons.Default.StopCircle, Modifier.weight(1f), onStop)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ControlCard("Terminal", "Open Termux shell", Icons.Default.Terminal, Modifier.weight(1f), onTerminal)
                ControlCard("GitHub", "Optional sign-in", Icons.Default.Code, Modifier.weight(1f), onGithub)
            }

            OutlinedButton(onClick = onRepository, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create / manage GitHub repository")
            }

            if (showHelp) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("First setup", fontWeight = FontWeight.Bold, color = UbuntuDark)
                        Text("1. Install Termux from its official F-Droid/GitHub source.\n2. Enable 'Allow external apps' / RUN_COMMAND permission in Termux settings.\n3. Clone this project's repository into ~/uvl.\n4. Tap Install Ubuntu once, then Start Computer.\n5. Connect an AVNC client to 127.0.0.1:5901.", color = Color.DarkGray, fontSize = 13.sp)
                        Text("GitHub login is optional. The GitHub button currently opens GitHub in your browser; OAuth token storage is intentionally not included in this starter build.", color = UbuntuOrange, fontSize = 12.sp)
                    }
                }
            }

            Text("Requires Termux + PRoot + VNC viewer. This app is a launcher, not a full hardware emulator.", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(color = UbuntuPanel, shape = RoundedCornerShape(50)) {
        Text(text, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun ControlCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(modifier = modifier, onClick = onClick, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, contentDescription = null, tint = UbuntuOrange, modifier = Modifier.size(27.dp))
            Text(title, fontWeight = FontWeight.Bold, color = UbuntuDark)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
