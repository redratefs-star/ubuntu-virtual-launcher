package com.uvl.launcher

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val Background = Color(0xFF080C17)
private val Panel = Color(0xFF151D30)
private val Purple = Color(0xFF927BFF)
private val Cyan = Color(0xFF55D8E8)
private val Muted = Color(0xFF9BAAC4)

class MainActivity : ComponentActivity() {

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }

                Toast.makeText(
                    this,
                    "File berhasil dipilih",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val mediaPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { openMedia(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

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
                    onLaunchGame = { packageName, name ->
                        launchApp(packageName, name)
                    },
                    onPickFile = {
                        filePicker.launch(arrayOf("*/*"))
                    },
                    onPickMedia = {
                        mediaPicker.launch(
                            arrayOf(
                                "video/*",
                                "audio/*",
                                "image/*"
                            )
                        )
                    },
                    onSettings = {
                        startActivity(
                            Intent(Settings.ACTION_SETTINGS)
                        )
                    },
                    getRamInfo = {
                        getRamInfo()
                    },
                    getStorageInfo = {
                        getStorageInfo()
                    }
                )
            }
        }
    }

    private fun launchApp(packageName: String, name: String) {
        try {
            val intent =
                packageManager.getLaunchIntentForPackage(packageName)

            if (intent != null) {
                startActivity(intent)
            } else {
                showToast("$name belum terpasang.")
            }
        } catch (e: Exception) {
            showToast("Tidak bisa membuka $name.")
        }
    }

    private fun openMedia(uri: Uri) {
        try {
            val mimeType =
                contentResolver.getType(uri) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)
        } catch (e: Exception) {
            showToast("Tidak ada aplikasi yang cocok untuk membuka media ini.")
        }
    }

    private fun getRamInfo(): Pair<String, String> {
        return try {
            val manager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val memoryInfo = ActivityManager.MemoryInfo()
            manager.getMemoryInfo(memoryInfo)

            val total =
                memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

            val available =
                memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)

            val used = (total - available).coerceAtLeast(0.0)

            Pair(
                String.format(Locale.US, "%.1f GB", used),
                String.format(Locale.US, "%.1f GB", total)
            )
        } catch (e: Exception) {
            Pair("--", "--")
        }
    }

    private fun getStorageInfo(): Pair<String, String> {
        return try {
            val stat =
                StatFs(Environment.getDataDirectory().path)

            val total =
                stat.totalBytes / (1024.0 * 1024.0 * 1024.0)

            val free =
                stat.availableBytes / (1024.0 * 1024.0 * 1024.0)

            Pair(
                String.format(Locale.US, "%.1f GB", total - free),
                String.format(Locale.US, "%.1f GB", total)
            )
        } catch (e: Exception) {
            Pair("--", "--")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun VirtusApp(
    onLaunchGame: (String, String) -> Unit,
    onPickFile: () -> Unit,
    onPickMedia: () -> Unit,
    onSettings: () -> Unit,
    getRamInfo: () -> Pair<String, String>,
    getStorageInfo: () -> Pair<String, String>
) {
    var currentPage by remember {
        mutableIntStateOf(0)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column {
            VirtusHeader()

            Box(
                modifier = Modifier.weight(1f)
            ) {
                when (currentPage) {
                    0 -> HomePage(
                        onNavigate = { currentPage = it },
                        getRamInfo = getRamInfo,
                        getStorageInfo = getStorageInfo
                    )

                    1 -> DesktopPage(
                        onSettings = onSettings
                    )

                    2 -> GameCenterPage(
                        onLaunchGame = onLaunchGame
                    )

                    3 -> FileManagerPage(
                        onPickFile = onPickFile
                    )

                    4 -> SystemMonitorPage(
                        getRamInfo = getRamInfo,
                        getStorageInfo = getStorageInfo
                    )

                    5 -> MediaCenterPage(
                        onPickMedia = onPickMedia
                    )

                    6 -> CloudManagerPage()
                }
            }

            VirtusNavigationBar(
                selectedPage = currentPage,
                onSelect = { currentPage = it }
            )
        }
    }
}

@Composable
fun VirtusHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Purple, RoundedCornerShape(14.dp)),
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Text(
                "ANDROID DESKTOP ECOSYSTEM",
                color = Muted,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }

        Surface(
            color = Color(0xFF17392F),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                "STAGE 1",
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
fun VirtusNavigationBar(
    selectedPage: Int,
    onSelect: (Int) -> Unit
) {
    val items = listOf(
        "Home" to Icons.Default.Home,
        "Desktop" to Icons.Default.DesktopWindows,
        "Games" to Icons.Default.SportsEsports,
        "Files" to Icons.Default.Folder,
        "Monitor" to Icons.Default.Memory,
        "Media" to Icons.Default.PlayCircle,
        "Cloud" to Icons.Default.Cloud
    )

    NavigationBar(
        containerColor = Panel
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedPage == index,
                onClick = {
                    onSelect(index)
                },
                icon = {
                    Icon(
                        item.second,
                        contentDescription = item.first
                    )
                },
                label = {
                    Text(
                        item.first,
                        fontSize = 9.sp
                    )
                }
            )
        }
    }
}

@Composable
fun PageContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
        content = content
    )
}

@Composable
fun HomePage(
    onNavigate: (Int) -> Unit,
    getRamInfo: () -> Pair<String, String>,
    getStorageInfo: () -> Pair<String, String>
) {
    val ram = getRamInfo()
    val storage = getStorageInfo()

    PageContainer {
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
                    RoundedCornerShape(22.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    "WELCOME TO YOUR DIGITAL SPACE",
                    color = Cyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Virtus OS",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    "Satu ruang untuk aplikasi, file, media, dan pengelolaan perangkat.",
                    color = Color(0xFFD0D9EA),
                    fontSize = 13.sp
                )
            }
        }

        Text(
            "QUICK ACCESS",
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureCard(
                "Desktop",
                "Workspace",
                Icons.Default.DesktopWindows,
                Modifier.weight(1f)
            ) {
                onNavigate(1)
            }

            FeatureCard(
                "Game Center",
                "Game Android",
                Icons.Default.SportsEsports,
                Modifier.weight(1f)
            ) {
                onNavigate(2)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureCard(
                "File Manager",
                "Kelola file",
                Icons.Default.Folder,
                Modifier.weight(1f)
            ) {
                onNavigate(3)
            }

            FeatureCard(
                "System Monitor",
                "RAM & storage",
                Icons.Default.Memory,
                Modifier.weight(1f)
            ) {
                onNavigate(4)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureCard(
                "Media Center",
                "Audio & video",
                Icons.Default.PlayCircle,
                Modifier.weight(1f)
            ) {
                onNavigate(5)
            }

            FeatureCard(
                "Cloud Manager",
                "Cloud service",
                Icons.Default.Cloud,
                Modifier.weight(1f)
            ) {
                onNavigate(6)
            }
        }

        InfoCard(
            "DEVICE STATUS",
            "RAM terpakai: ${ram.first} dari ${ram.second}\n" +
                "Penyimpanan terpakai: ${storage.first} dari ${storage.second}"
        )

        InfoCard(
            "VIRTUS OS",
            "Ini adalah dashboard Android-native. RAM dan penyimpanan tetap mengikuti perangkat. Cloud PC dan Ubuntu mandiri belum aktif."
        )
    }
}

@Composable
fun DesktopPage(
    onSettings: () -> Unit
) {
    PageContainer {
        Text(
            "Virtus Desktop",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Workspace bergaya desktop",
            color = Muted
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF25204D),
                            Color(0xFF0D3445)
                        )
                    ),
                    RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.DesktopWindows,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(50.dp)
                )

                Text(
                    "VIRTUS WORKSPACE",
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                )

                Text(
                    "Desktop shell preview",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            Text(
                "VIRTUS OS",
                modifier = Modifier.align(Alignment.BottomStart),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        ActionCard(
            "Android Settings",
            "Buka pengaturan perangkat",
            Icons.Default.Settings,
            onSettings
        )

        InfoCard(
            "NEXT UPDATE",
            "Dock aplikasi, wallpaper bergerak, jendela aplikasi internal, dan multi-window akan dikembangkan pada tahap berikutnya."
        )
    }
}

@Composable
fun GameCenterPage(
    onLaunchGame: (String, String) -> Unit
) {
    PageContainer {
        Text(
            "UVL Game Center",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Buka game Android yang sudah terpasang.",
            color = Muted
        )

        ActionCard(
            "Roblox",
            "Jalankan Roblox Android",
            Icons.Default.SportsEsports
        ) {
            onLaunchGame(
                "com.roblox.client",
                "Roblox"
            )
        }

        ActionCard(
            "Mobile Legends",
            "Jalankan Mobile Legends",
            Icons.Default.Shield
        ) {
            onLaunchGame(
                "com.mobile.legends",
                "Mobile Legends"
            )
        }

        ActionCard(
            "Free Fire",
            "Jalankan Free Fire",
            Icons.Default.Whatshot
        ) {
            onLaunchGame(
                "com.dts.freefireth",
                "Free Fire"
            )
        }

        InfoCard(
            "CLOUD GAMING",
            "Game PC belum berjalan secara lokal di HP. Streaming game PC membutuhkan PC atau server yang aktif."
        )
    }
}

@Composable
fun FileManagerPage(
    onPickFile: () -> Unit
) {
    PageContainer {
        Text(
            "Virtus File Manager",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Pilih file dari penyimpanan Android.",
            color = Muted
        )

        ActionCard(
            "Browse files",
            "Buka pemilih dokumen dan file Android",
            Icons.Default.FolderOpen,
            onPickFile
        )

        InfoCard(
            "FILE ACCESS",
            "Pemilih file Android hanya memberikan akses ke file yang kamu pilih. Aplikasi ini belum memiliki akses bebas ke seluruh folder privat Android."
        )
    }
}

@Composable
fun SystemMonitorPage(
    getRamInfo: () -> Pair<String, String>,
    getStorageInfo: () -> Pair<String, String>
) {
    var refresh by remember {
        mutableIntStateOf(0)
    }

    val ram = getRamInfo()
    val storage = getStorageInfo()

    PageContainer {
        Text(
            "System Monitor",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Informasi perangkat dari Android.",
            color = Muted
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                "RAM USED",
                ram.first,
                Modifier.weight(1f)
            )

            MetricCard(
                "RAM TOTAL",
                ram.second,
                Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                "STORAGE USED",
                storage.first,
                Modifier.weight(1f)
            )

            MetricCard(
                "STORAGE TOTAL",
                storage.second,
                Modifier.weight(1f)
            )
        }

        ActionCard(
            "Refresh monitor",
            "Perbarui informasi perangkat",
            Icons.Default.Refresh
        ) {
            refresh++
        }

        Text(
            "Refresh ke-$refresh",
            color = Muted,
            fontSize = 10.sp
        )

        InfoCard(
            "CATATAN",
            "Aplikasi Android biasa tidak dapat menambah RAM fisik, mengubah GPU, atau menjamin peningkatan FPS perangkat."
        )
    }
}

@Composable
fun MediaCenterPage(
    onPickMedia: () -> Unit
) {
    PageContainer {
        Text(
            "Virtus Media Center",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Pilih media dari penyimpanan perangkat.",
            color = Muted
        )

        ActionCard(
            "Open media",
            "Pilih video, audio, atau gambar",
            Icons.Default.PlayCircle,
            onPickMedia
        )

        InfoCard(
            "MEDIA PLAYBACK",
            "File media akan dibuka menggunakan aplikasi Android yang mendukung format tersebut."
        )

        InfoCard(
            "YOUTUBE",
            "Untuk YouTube, gunakan aplikasi YouTube atau browser yang terpasang. Pemutar YouTube internal belum dibuat."
        )
    }
}

@Composable
fun CloudManagerPage() {
    PageContainer {
        Text(
            "UVL Cloud Manager",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Target konfigurasi cloud Virtus OS.",
            color = Muted
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                "RAM TARGET",
                "10 GB",
                Modifier.weight(1f)
            )

            MetricCard(
                "STORAGE TARGET",
                "100 GB",
                Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                "CPU TARGET",
                "4 vCPU",
                Modifier.weight(1f)
            )

            MetricCard(
                "GRAPHICS",
                "Cloud GPU",
                Modifier.weight(1f)
            )
        }

        InfoCard(
            "SERVER STATUS",
            "Belum terhubung. Belum ada server cloud yang dikonfigurasi."
        )

        InfoCard(
            "CARA KERJA",
            "RAM 10 GB, storage 100 GB, dan GPU cloud hanya tersedia jika ada server/provider yang menyediakannya. Angka di atas adalah target, bukan kapasitas aktif."
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(27.dp)
            )

            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                subtitle,
                color = Muted,
                fontSize = 10.sp
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(29.dp)
            )

            Spacer(Modifier.width(13.dp))

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
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Muted
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Panel
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                title,
                color = Muted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                value,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
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
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
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
                lineHeight = 18.sp
            )
        }
    }
}
