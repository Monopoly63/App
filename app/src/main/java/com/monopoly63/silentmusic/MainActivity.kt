package com.monopoly63.silentmusic

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.monopoly63.silentmusic.core.database.entities.FolderRuleType
import com.monopoly63.silentmusic.core.design.*
import com.monopoly63.silentmusic.feature.folders.FolderFilterViewModel
import com.monopoly63.silentmusic.feature.nowplaying.NowPlayingViewModel
import com.monopoly63.silentmusic.feature.songs.AllSongsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SilentLuxuryTheme { SilentApp() } }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) { Songs("Songs", Icons.Rounded.LibraryMusic), Folders("Folders", Icons.Rounded.Folder), Favorites("Favorites", Icons.Rounded.Favorite) }

@Composable
private fun SilentApp(nowVm: NowPlayingViewModel = hiltViewModel()) {
    val playback by nowVm.state.collectAsState()
    var tab by remember { mutableStateOf(Tab.Songs) }
    RequestAudioPermission()
    AlbumArtBackdrop(playback.artworkUri, Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.Transparent, bottomBar = { GlassBottomBar(tab) { tab = it } }) { padding ->
            Crossfade(tab, modifier = Modifier.padding(padding), label = "tab") { selected ->
                when (selected) {
                    Tab.Songs -> SongsScreen()
                    Tab.Folders -> FoldersScreen()
                    Tab.Favorites -> FavoritesScreen()
                }
            }
            if (playback.title != null) MiniPlayer(playback.title ?: "", playback.artist ?: "Unknown Artist", playback.isPlaying, { nowVm.playPause() }, { nowVm.next() }, Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp, start = 16.dp, end = 16.dp))
        }
    }
}

@Composable
private fun RequestAudioPermission() {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) launcher.launch(permission)
    }
}

@Composable
private fun SongsScreen(vm: AllSongsViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsState()
    ScreenColumn("All Songs", "Curated local library") {
        if (state.songs.isEmpty()) EmptyHint("No songs found. Grant audio permission, then use Folders to whitelist music directories.")
        LazyColumn(contentPadding = PaddingValues(bottom = 180.dp)) {
            items(state.songs, key = { it.mediaStoreId }) { song ->
                SongRow(song.title, song.artist ?: "Unknown Artist", song.albumArtUri?.toString(), state.favoriteIds.contains(song.mediaStoreId), { vm.playSong(song) }, { vm.toggleFavorite(song) })
            }
        }
    }
}

@Composable
private fun FavoritesScreen(vm: AllSongsViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsState()
    val songs = state.songs.filter { state.favoriteIds.contains(it.mediaStoreId) }
    ScreenColumn("Favorites", "Your quiet luxury collection") {
        if (songs.isEmpty()) EmptyHint("Tap the heart on any song to add it here.")
        LazyColumn(contentPadding = PaddingValues(bottom = 180.dp)) {
            items(songs, key = { it.mediaStoreId }) { song -> SongRow(song.title, song.artist ?: "Unknown Artist", song.albumArtUri?.toString(), true, { vm.playSong(song) }, { vm.toggleFavorite(song) }) }
        }
    }
}

@Composable
private fun FoldersScreen(vm: FolderFilterViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsState()
    ScreenColumn("Folder Filter", "Whitelist music. Silence noise.") {
        if (state.folders.isEmpty()) EmptyHint("No folders detected yet. The scanner reads MediaStore relative paths.")
        LazyColumn(contentPadding = PaddingValues(bottom = 180.dp)) {
            items(state.folders, key = { it.path }) { folder ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp).glassSurface(RoundedCornerShape(22.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Folder, null, tint = LocalGlassColors.current.textSecondary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(folder.path, color = LocalGlassColors.current.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${folder.songCount} songs", color = LocalGlassColors.current.textSecondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = { if (folder.isIncluded) vm.clear(folder.path) else vm.setRule(folder.path, folder.name, FolderRuleType.INCLUDE) }) { Icon(Icons.Rounded.CheckCircle, null, tint = if (folder.isIncluded) Color(0xFFBDEBCB) else Color.White.copy(.45f)) }
                    IconButton(onClick = { if (folder.isExcluded) vm.clear(folder.path) else vm.setRule(folder.path, folder.name, FolderRuleType.EXCLUDE) }) { Icon(Icons.Rounded.Block, null, tint = if (folder.isExcluded) Color(0xFFFF8A8A) else Color.White.copy(.45f)) }
                }
            }
        }
    }
}

@Composable
private fun ScreenColumn(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(top = 20.dp)) {
        Text(title, Modifier.padding(horizontal = 24.dp), color = LocalGlassColors.current.textPrimary, fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, Modifier.padding(horizontal = 24.dp, vertical = 4.dp), color = LocalGlassColors.current.textSecondary)
        Spacer(Modifier.height(14.dp)); content()
    }
}

@Composable
private fun EmptyHint(text: String) { Box(Modifier.fillMaxWidth().padding(24.dp).glassSurface().padding(22.dp)) { Text(text, color = LocalGlassColors.current.textSecondary) } }

@Composable
private fun SongRow(title: String, artist: String, art: String?, favorite: Boolean, onClick: () -> Unit, onFavorite: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = art, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(.08f)))
        Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(title, color = LocalGlassColors.current.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium); Text(artist, color = LocalGlassColors.current.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) }
        IconButton(onClick = onFavorite) { Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (favorite) Color(0xFFFF6F91) else Color.White.copy(.55f)) }
    }
}

@Composable
private fun GlassBottomBar(selected: Tab, onSelected: (Tab) -> Unit) {
    Row(Modifier.navigationBarsPadding().padding(18.dp).fillMaxWidth().height(72.dp).glassSurface(RoundedCornerShape(36.dp)).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Tab.entries.forEach { tab -> Column(Modifier.weight(1f).clip(RoundedCornerShape(28.dp)).clickable { onSelected(tab) }.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(tab.icon, tab.label, tint = if (tab == selected) LocalGlassColors.current.textPrimary else LocalGlassColors.current.textSecondary); Text(tab.label, color = if (tab == selected) LocalGlassColors.current.textPrimary else LocalGlassColors.current.textSecondary, fontSize = 11.sp) } }
    }
}

@Composable
private fun MiniPlayer(title: String, artist: String, playing: Boolean, playPause: () -> Unit, next: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(72.dp).glassSurface(RoundedCornerShape(28.dp)).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, color = LocalGlassColors.current.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(artist, color = LocalGlassColors.current.textSecondary, fontSize = 12.sp, maxLines = 1) }
        GlassIcon(Icons.Rounded.SkipNext, next); Spacer(Modifier.width(8.dp)); GlassIcon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, playPause)
    }
}

@Composable
private fun GlassIcon(icon: ImageVector, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }; val pressed by src.collectIsPressedAsState()
    Box(Modifier.size(44.dp).scale(if (pressed) .92f else 1f).clip(CircleShape).background(Color.White.copy(.10f)).border(1.dp, Color.White.copy(.16f), CircleShape).clickable(src, null, onClick = onClick), contentAlignment = Alignment.Center) { Icon(icon, null, tint = LocalGlassColors.current.controlTint) }
}
