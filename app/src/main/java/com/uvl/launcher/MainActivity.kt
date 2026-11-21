
package com.uvl.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF080C17)
private val Panel = Color(0xFF151D30)
private val PanelLight = Color(0xFF202C43)
private val Purple = Color(0xFF927BFF)
private val Cyan = Color(0xFF55D8E8)
private val Muted = Color(0xFF9BAAC4)

class MainActivity : ComponentActivity() {

    private val termuxPackage = "com.termux"
    private val runPermission = "com.termux.permission.RUN_COMMAND"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Purple,
                    secondary = Cyan,
                    background = Background,
                    surface = Panel
                )
            ) {
                VirtusApp(
                    onLinuxInstall = {
                        runLinux("install")
                    },
                    onLinuxStart = {
                        runLinux("start")
                    },
                    onLinuxStop = {
                        runLinux("stop")
                    },
                    onTerminal = {
                        openTermux()
                    },
                    onVnc = {
                        openVnc()
                    },
                    onRoblox = {
                        launchGame(
                            "com.roblox.client",
                            "Roblox"
                        )
                    },
                    onML = {
                        launchGame(
                            "com.mobile.legends",
                            "Mobile Legends"
                        )
                    },
                    onFF = {
                        launchGame(
                            "com.dts.freefireth",
                            "Free Fire"
                        )
                    }
                )
            }
        }
    }

    private fun runLinux(action: String) {

        val script = when (action) {
            "install" -> "\$HOME/uvl/ubuntu/install.sh"
            "start" -> "\$HOME/uvl/ubuntu/start.sh"
            "stop" -> "\$HOME/uvl/ubuntu/stop.sh"
            else -> return
        }

        if (
            checkSelfPermission(runPermission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            toast(
                "Aktifkan izin RUN_COMMAND di Termux."
            )
            return
        }

        try {
            val intent = Intent(
                "com.termux.RUN_COMMAND"
            ).apply {
                setClassName(
                    termuxPackage,
                    "com.termux.app.RunCommandService"
                )

                putExtra(
                    "com.termux.RUN_COMMAND_PATH",
                    "/data/data/com.termux/files/usr/bin/bash"
                )

                putExtra(
                    "com.termux.RUN_COMMAND_ARGUMENTS",
                    arrayOf("-lc", script)
                )

                putExtra(
                    "com.termux.RUN_COMMAND_WORKDIR",
                    "/data/data/com.termux/files/home"
                )

                putExtra(
                    "com.termux.RUN_COMMAND_BACKGROUND",
                    false
                )
            }

            startService(intent)

            toast("Perintah Linux dikirim: $action")

        } catch (e: Exception) {
            toast(
                "Termux belum siap: ${e.message}"
            )
        }
    }

    private fun openTermux() {
        val intent =
            packageManager.getLaunchIntentForPackage(
                termuxPackage
            )

        if (intent != null) {
            startActivity(intent)
        } else {
            toast("Termux belum terpasang.")
        }
    }

    private fun openVnc() {
        val intent = packageManager
            .getLaunchIntentForPackage("com.gaurav.avnc")

        if (intent != null) {
            startActivity(intent)
        } else {
            toast(
                "Buka AVNC dan hubungkan ke 127.0.0.1 port 5901."
            )
        }
    }

    private fun launchGame(
        packageName: String,
        gameName: String
    ) {
        val intent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )

        if (intent != null) {
            startActivity(intent)
        } else {
            toast(
                "$gameName belum terpasang atau paketnya berbeda."
            )
        }
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun VirtusApp(
    onLinuxInstall: () -> Unit,
    onLinuxStart: () -> Unit,
    onLinuxStop: () -> Unit,
    onTerminal: () -> Unit,
    onVnc: () -> Unit,
    onRoblox: () -> Unit,
    onML: () -> Unit,
    onFF: () -> Unit
) {

    var page by remember {
        mutableIntStateOf(0)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column {

            TopHeader()

            Box(
                modifier = Modifier.weight(1f)
            ) {
                when (page) {
                    0 -> HomePage(
                        onCloud = { page = 1 },
                        onGaming = { page = 2 },
                        onLinux = { page = 3 },
                        onVnc = onVnc
                    )

                    1 -> CloudPage()

                    2 -> GamingPage(
                        onRoblox,
                        onML,
                        onFF
                    )

                    3 -> LinuxPage(
                        onLinuxInstall,
                        onLinuxStart,
                        onLinuxStop,
                        onTerminal,
                        onVnc
                    )
                }
            }

            NavigationBar(
                containerColor = Panel
            ) {
                val items = listOf(
                    "Home",
                    "Cloud",
                    "Gaming",
                    "Linux"
                )

                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.Cloud,
                    Icons.Default.Gamepad,
                    Icons.Default.Terminal
                )

                items.forEachIndexed { index, label ->

                    NavigationBarItem(
                        selected = page == index,
                        onClick = {
                            page = index
                        },
                        icon = {
                            Icon(
                                icons[index],
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(label)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 17.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(45.dp)
                .background(
                    Purple,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Terminal,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "VIRTUS OS",
                fontSize = 21.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Text(
                "ULTIMATE ECOSYSTEM",
                color = Muted,
                fontSize = 9.sp,
                letterSpacing = 1.3.sp
            )
        }

        Surface(
            color = Color(0xFF17392F),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                "V2 BETA",
                color = Color(0xFF80E7B6),
                fontSize = 10.sp,
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                )
            )
        }
    }
}

@Composable
fun PageColumn(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        content = content
    )
}

@Composable
fun HomePage(
    onCloud: () -> Unit,
    onGaming: () -> Unit,
    onLinux: () -> Unit,
    onVnc: () -> Unit
) {
    PageColumn {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF35285F),
                            Color(0xFF132E49)
                        )
                    ),
                    RoundedCornerShape(23.dp)
                )
                .padding(21.dp)
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    "YOUR DEVICE. YOUR CLOUD.",
                    color = Cyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Welcome to Virtus.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Gaming, Linux, cloud computing, dan AI dalam satu ekosistem.",
                    color = Color(0xFFD0D9EA),
                    fontSize = 13.sp
                )
            }
        }

        Text(
            "QUICK ACCESS",
            color = Muted,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            FeatureCard(
                "Cloud PC",
                "Windows + Linux",
                Icons.Default.Cloud,
                Modifier.weight(1f),
                onCloud
            )

            FeatureCard(
                "Game Center",
                "Android gaming",
                Icons.Default.Gamepad,
                Modifier.weight(1f),
                onGaming
            )
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            FeatureCard(
                "Linux",
                "Ubuntu XFCE",
                Icons.Default.Terminal,
                Modifier.weight(1f),
                onLinux
            )

            FeatureCard(
                "Desktop",
                "VNC Viewer",
                Icons.Default.DesktopWindows,
                Modifier.weight(1f),
                onVnc
            )
        }

        InfoCard(
            "VIRTUS CORE",
            "Dashboard utama untuk mengelola modul Virtus OS."
        )

        InfoCard(
            "LOCAL + CLOUD",
            "Linux lokal berjalan di HP. Cloud PC memerlukan server eksternal yang benar-benar disediakan."
        )
    }
}

@Composable
fun CloudPage() {

    var expanded by remember {
        mutableStateOf(false)
    }

    PageColumn {

        Text(
            "UVL Cloud Manager",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Virtual computer management",
            color = Muted,
            fontSize = 12.sp
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                "RAM TARGET",
                "10 GB",
                Icons.Default.Memory,
                Modifier.weight(1f)
            )

            MetricCard(
                "STORAGE TARGET",
                "100 GB",
                Icons.Default.Storage,
                Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                "CPU TARGET",
                "4 vCPU",
                Icons.Default.Memory,
                Modifier.weight(1f)
            )

            MetricCard(
                "GRAPHICS",
                "Cloud GPU",
                Icons.Default.Computer,
                Modifier.weight(1f)
            )
        }

        InfoCard(
            "SERVER STATUS",
            "Belum terhubung. Belum ada cloud server yang dikonfigurasi."
        )

        Button(
            onClick = {
                expanded = !expanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                if (expanded)
                    "Hide Configuration"
                else
                    "Show Configuration"
            )
        }

        if (expanded) {

            InfoCard(
                "WINDOWS VIRTUAL MACHINE",
                "Target alokasi: RAM 6 GB dan storage 60 GB. Membutuhkan host virtualisasi, Windows yang berlisensi, dan dukungan GPU."
            )

            InfoCard(
                "LINUX VIRTUAL MACHINE",
                "Target alokasi: RAM 4 GB dan storage 40 GB. Ubuntu Linux untuk coding, server, dan pekerjaan lainnya."
            )

            InfoCard(
                "GPU CLOUD",
                "Memerlukan instance GPU atau GPU passthrough/vGPU dari provider cloud."
            )

            InfoCard(
                "CLOUD GAMING",
                "Game PC berjalan di server. HP menerima video dan audio melalui streaming."
            )
        }

        InfoCard(
            "IMPORTANT",
            "Angka RAM dan storage di sini adalah target konfigurasi, bukan kapasitas yang sudah tersedia."
        )
    }
}

@Composable
fun GamingPage(
    onRoblox: () -> Unit,
    onML: () -> Unit,
    onFF: () -> Unit
) {
    PageColumn {

        Text(
            "UVL Game Center",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Launch your favorite games",
            color = Muted
        )

        GameCard(
            "Roblox",
            "Open Android game",
            Icons.Default.SportsEsports,
            onRoblox
        )

        GameCard(
            "Mobile Legends",
            "Open Android game",
            Icons.Default.Shield,
            onML
        )

        GameCard(
            "Free Fire",
            "Open Android game",
            Icons.Default.Whatshot,
            onFF
        )

        InfoCard(
            "UNIVERSAL GAME CONTROL",
            "Target fitur: virtual gamepad, keyboard mapping, mouse mapping, custom layout, dan profil kontrol per game. Belum aktif dalam versi ini."
        )

        InfoCard(
            "UVL CLOUD GAMING",
            "Streaming game PC dari server GPU ke HP. Membutuhkan server GPU dan aplikasi streaming."
        )
    }
}

@Composable
fun LinuxPage(
    onInstall: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTerminal: () -> Unit,
    onVnc: () -> Unit
) {
    PageColumn {

        Text(
            "Local Linux",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Ubuntu ARM64 + XFCE",
            color = Muted
        )

        InfoCard(
            "LOCAL COMPUTER",
            "Linux berjalan melalui Termux dan PRoot. RAM dan storage mengikuti kemampuan Samsung A14."
        )

        ActionCard(
            "Install Ubuntu",
            "First time installation",
            Icons.Default.Download,
            onInstall
        )

        ActionCard(
            "Start Linux",
            "Start desktop environment",
            Icons.Default.PlayArrow,
            onStart
        )

        ActionCard(
            "Stop Linux",
            "Stop VNC session",
            Icons.Default.Stop,
            onStop
        )

        ActionCard(
            "Open Terminal",
            "Open Termux",
            Icons.Default.Terminal,
            onTerminal
        )

        Button(
            onClick = onVnc,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            )
        ) {
            Icon(
                Icons.Default.DesktopWindows,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("OPEN LINUX DESKTOP")
        }

        InfoCard(
            "VNC CONNECTION",
            "Address: 127.0.0.1\nPort: 5901\nPastikan Ubuntu sudah berjalan."
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable {
            onClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = Panel
        ),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(28.dp)
            )

            Text(
                title,
                fontWeight = FontWeight.Bold
            )

            Text(
                subtitle,
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Panel
        ),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Cyan
            )

            Text(
                title,
                color = Muted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GameCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Panel
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(30.dp)
            )

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    subtitle,
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Icon(
                Icons.Default.PlayCircle,
                contentDescription = null,
                tint = Cyan
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Panel
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(13.dp))

            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    subtitle,
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    body: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Panel
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            Text(
                title,
                color = Cyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                body,
                color = Color(0xFFD4DDED),
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
        }
    }
}
