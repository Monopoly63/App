package com.monopoly63.silentmusic.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monopoly63.silentmusic.R
import com.monopoly63.silentmusic.core.playback.ACTION_NEXT
import com.monopoly63.silentmusic.core.playback.ACTION_PLAY_PAUSE
import com.monopoly63.silentmusic.core.playback.PlaybackCommandReceiver

class MusicGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent { WidgetContent() }
}

@Composable
private fun WidgetContent() {
    Box(GlanceModifier.fillMaxSize().cornerRadius(28.dp).background(ColorProvider(Color(0xAA15171D))).padding(16.dp)) {
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(GlanceModifier.size(72.dp).cornerRadius(20.dp).background(ColorProvider(Color(0x33FFFFFF)))) {}
            Spacer(GlanceModifier.width(14.dp))
            Column(GlanceModifier.defaultWeight()) {
                Text("Silent Music", maxLines = 1, style = TextStyle(color = ColorProvider(Color(0xFFF6F3ED)), fontSize = 15.sp, fontWeight = FontWeight.Medium))
                Spacer(GlanceModifier.height(4.dp))
                Text("Glass local player", maxLines = 1, style = TextStyle(color = ColorProvider(Color(0xFFA9A7A2)), fontSize = 12.sp))
            }
            Image(ImageProvider(R.drawable.ic_play_arrow), "Play/Pause", GlanceModifier.size(38.dp).clickable(actionRunCallback<WidgetPlayPauseAction>()))
            Spacer(GlanceModifier.width(10.dp))
            Image(ImageProvider(R.drawable.ic_skip_next), "Next", GlanceModifier.size(38.dp).clickable(actionRunCallback<WidgetNextAction>()))
        }
    }
}

class MusicWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = MusicGlanceWidget() }

class WidgetPlayPauseAction : ActionCallback { override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) { context.sendBroadcast(Intent(context, PlaybackCommandReceiver::class.java).setAction(ACTION_PLAY_PAUSE)) } }
class WidgetNextAction : ActionCallback { override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) { context.sendBroadcast(Intent(context, PlaybackCommandReceiver::class.java).setAction(ACTION_NEXT)) } }
