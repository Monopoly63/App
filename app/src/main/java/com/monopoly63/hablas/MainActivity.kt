package com.monopoly63.hablas

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import coil.compose.AsyncImage
import com.monopoly63.hablas.core.AudioTrack

class MainActivity : ComponentActivity() {
    private val viewModel: HablasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.rgb(5, 6, 10)
        setContent { HablasTheme { HablasApp(viewModel) } }
    }
}

private enum class Tab(val title: String, val icon: ImageVector) {
    Songs("Songs", Icons.Rounded.LibraryMusic),
    Folders("Folders", Icons.Rounded.Folder),
    Favorites("Favorites", Icons.Rounded.Favorite)
}

@Composable
private fun HablasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF05060A),
            surface = Color.White.copy(alpha = 0.08f),
            primary = Color(0xFFF2EEE6),
            onBackground = Color(0xFFF6F1E8),
            onSurface = Color(0xFFF6F1E8)
        ),
        content = content
    )
}

@Composable
private fun HablasApp(vm: HablasViewModel) {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (granted) vm.scanLibrary()
    }
    LaunchedEffect(Unit) {
        if (hasPermission) vm.scanLibrary() else permissionLauncher.launch(permission)
    }

    val tracks by vm.tracks.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val excluded by vm.excludedFolders.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val player by vm.playerState.collectAsState()
    val visible = remember(tracks, excluded) { tracks.filter { it.folderPath !in excluded } }
    var tab by remember { mutableStateOf(Tab.Songs) }

    GlassBackdrop(art = player.artworkUri) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            TopHeader(
                title = "Hablas",
                subtitle = when (tab) {
                    Tab.Songs -> "Native local music, organized by your device folders"
                    Tab.Folders -> "Each folder is separate — hide the noise instantly"
                    Tab.Favorites -> "Your selected premium library"
                },
                isScanning = isScanning,
                onScan = { if (hasPermission) vm.scanLibrary() else permissionLauncher.launch(permission) }
            )
            Box(Modifier.weight(1f)) {
                Crossfade(tab, animationSpec = tween(160), label = "tab") { selected ->
                    when (selected) {
                        Tab.Songs -> SongsScreen(visible, favorites, onPlay = vm::play, onFav = vm::toggleFavorite)
                        Tab.Folders -> FoldersScreen(tracks, excluded, onToggle = vm::toggleFolder)
                        Tab.Favorites -> SongsScreen(visible.filter { it.id in favorites }, favorites, onPlay = vm::play, onFav = vm::toggleFavorite, empty = "No favorites yet.")
                    }
                }
            }
            if (player.title != null) {
                MiniPlayer(
                    title = player.title.orEmpty(),
                    artist = player.artist ?: "Unknown Artist",
                    playing = player.isPlaying,
                    onPrev = vm::previous,
                    onPlay = vm::playPause,
                    onNext = vm::next
                )
            }
            BottomBar(tab, onChange = { tab = it })
        }
    }
}

@Composable
private fun GlassBackdrop(art: android.net.Uri?, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF05060A))) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(Color(0xFF3B4268).copy(alpha = .65f), Color(0xFF11131D), Color(0xFF05060A)),
                    radius = 1100f
                )
            )
        )
        if (art != null) {
            AsyncImage(
                model = art,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().scale(1.06f).blur(38.dp).alpha(.38f).graphicsLayer { clip = false }
            )
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .28f)))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White.copy(.055f), Color.Transparent, Color.Black.copy(.46f)))))
        content()
    }
}

@Composable
private fun TopHeader(title: String, subtitle: String, isScanning: Boolean, onScan: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.GraphicEq, null, tint = Color(0xFFF6F1E8), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = Color(0xFFF6F1E8), fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
            }
            Text(subtitle, color = Color.White.copy(.52f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        GlassCircle(icon = Icons.Rounded.Refresh, contentDescription = "Scan", onClick = onScan, spinning = isScanning)
    }
}

@Composable
private fun SongsScreen(
    tracks: List<AudioTrack>,
    favorites: Set<Long>,
    onPlay: (AudioTrack) -> Unit,
    onFav: (Long) -> Unit,
    empty: String = "No music found. Tap scan after granting audio permission."
) {
    if (tracks.isEmpty()) {
        EmptyState(empty)
        return
    }
    LazyColumn(contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 18.dp), modifier = Modifier.fillMaxSize()) {
        val grouped = tracks.groupBy { it.folderPath }
        grouped.forEach { (folder, itemsInFolder) ->
            item(key = "folder-$folder") {
                Text(folder, color = Color.White.copy(.46f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp, top = 14.dp, bottom = 6.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            items(itemsInFolder, key = { it.id }) { track ->
                SongRow(track, isFavorite = track.id in favorites, onPlay = { onPlay(track) }, onFav = { onFav(track.id) })
            }
        }
    }
}

@Composable
private fun SongRow(track: AudioTrack, isFavorite: Boolean, onPlay: () -> Unit, onFav: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(22.dp)).clickable(onClick = onPlay).padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(.08f)).border(1.dp, Color.White.copy(.10f), RoundedCornerShape(15.dp))
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color(0xFFF6F1E8), fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, color = Color.White.copy(.48f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onFav) {
            Icon(if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (isFavorite) Color(0xFFFF7E9D) else Color.White.copy(.42f))
        }
    }
}

@Composable
private fun FoldersScreen(tracks: List<AudioTrack>, excluded: Set<String>, onToggle: (String) -> Unit) {
    val folders = remember(tracks) { tracks.groupBy { it.folderPath }.toSortedMap(String.CASE_INSENSITIVE_ORDER) }
    if (folders.isEmpty()) { EmptyState("No folders detected yet. Tap scan."); return }
    LazyColumn(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp, bottom = 22.dp), modifier = Modifier.fillMaxSize()) {
        items(folders.entries.toList(), key = { it.key }) { entry ->
            val off = entry.key in excluded
            Row(
                Modifier.fillMaxWidth().padding(vertical = 5.dp).glass(RoundedCornerShape(24.dp), alpha = if (off) .045f else .085f).clickable { onToggle(entry.key) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Folder, null, tint = if (off) Color.White.copy(.30f) else Color(0xFFEDE8DC), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.key, color = Color.White.copy(if (off) .38f else .92f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${entry.value.size} tracks • ${if (off) "hidden" else "included"}", color = Color.White.copy(.42f), fontSize = 11.sp)
                }
                Text(if (off) "OFF" else "ON", color = if (off) Color.White.copy(.35f) else Color(0xFFBDEBCB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MiniPlayer(title: String, artist: String, playing: Boolean, onPrev: () -> Unit, onPlay: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth().height(72.dp).glass(RoundedCornerShape(30.dp), .10f).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            AnimatedContent(title, label = "mini-title") { Text(it, color = Color(0xFFF6F1E8), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium) }
            Text(artist, color = Color.White.copy(.48f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        GlassCircle(Icons.Rounded.SkipPrevious, "Previous", onPrev, size = 42)
        Spacer(Modifier.width(8.dp))
        GlassCircle(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play", onPlay, size = 48)
        Spacer(Modifier.width(8.dp))
        GlassCircle(Icons.Rounded.SkipNext, "Next", onNext, size = 42)
    }
}

@Composable
private fun BottomBar(selected: Tab, onChange: (Tab) -> Unit) {
    Row(Modifier.navigationBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth().height(68.dp).glass(RoundedCornerShape(34.dp), .10f).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Tab.entries.forEach { tab ->
            val active = tab == selected
            Column(Modifier.weight(1f).clip(RoundedCornerShape(26.dp)).clickable { onChange(tab) }.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(tab.icon, tab.title, tint = Color.White.copy(if (active) .95f else .42f), modifier = Modifier.size(22.dp))
                Text(tab.title, color = Color.White.copy(if (active) .86f else .42f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().glass(RoundedCornerShape(32.dp), .08f).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.GraphicEq, null, tint = Color.White.copy(.62f), modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(12.dp))
            Text(text, color = Color.White.copy(.60f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun GlassCircle(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, size: Int = 46, spinning: Boolean = false) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .92f else 1f, tween(80), label = "press")
    Box(
        Modifier.size(size.dp).scale(scale).clip(CircleShape).background(Color.White.copy(.10f)).border(1.dp, Color.White.copy(.16f), CircleShape).clickable(source, null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = Color(0xFFF6F1E8), modifier = Modifier.size((size * .48f).dp).graphicsLayer { if (spinning) rotationZ = 0f })
    }
}

private fun Modifier.glass(shape: RoundedCornerShape, alpha: Float): Modifier = this
    .clip(shape)
    .background(Brush.linearGradient(listOf(Color.White.copy(alpha), Color.White.copy(alpha * .35f))))
    .border(1.dp, Color.White.copy(.14f), shape)
