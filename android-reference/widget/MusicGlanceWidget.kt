package com.lumen.player.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.lumen.player.MainActivity
import com.lumen.player.playback.PlaybackService

/**
 * Rounded to the system radius, frosted, tinted with the same desaturated
 * accent as the in-app Now Playing screen. Tapping the artwork/title opens
 * the app (deep link to Now Playing); Play/Pause/Skip act directly on the
 * MediaController — the app process does not need to be foregrounded.
 */
class MusicGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentWidgetState() // custom GlanceStateDefinition, see below
            GlassWidgetContent(
                title = state.title,
                artist = state.artist,
                artworkUri = state.artworkUri,
                isPlaying = state.isPlaying,
                accent = Color(state.accentArgb),
            )
        }
    }
}

@Composable
private fun GlassWidgetContent(
    title: String,
    artist: String,
    artworkUri: String?,
    isPlaying: Boolean,
    accent: Color,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color.Black.copy(alpha = 0.001f))) // required for RemoteViews translucency host
            .cornerRadius(28.dp) // follows system widget corner radius on API 31+
            .padding(12.dp)
            .clickable(actionRunCallback<OpenAppAction>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (artworkUri != null) {
            Image(
                provider = ImageProvider(android.net.Uri.parse(artworkUri)),
                contentDescription = null,
                modifier = GlanceModifier.size(52.dp).cornerRadius(16.dp),
            )
        } else {
            Box(
                modifier = GlanceModifier.size(52.dp).cornerRadius(16.dp)
                    .background(ColorProvider(accent.copy(alpha = 0.4f))),
                contentAlignment = Alignment.Center,
            ) {}
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = title,
                maxLines = 1,
                style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Medium),
            )
            Text(
                text = artist,
                maxLines = 1,
                style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.6f))),
            )
        }

        Row {
            RoundIconButton(
                symbol = if (isPlaying) "⏸" else "▶",
                action = actionRunCallback<TogglePlayAction>(),
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            RoundIconButton(symbol = "⏭", action = actionRunCallback<SkipNextAction>())
        }
    }
}

@Composable
private fun RoundIconButton(symbol: String, action: androidx.glance.action.Action) {
    Box(
        modifier = GlanceModifier
            .size(36.dp)
            .cornerRadius(18.dp)
            .background(ColorProvider(Color.White.copy(alpha = 0.14f)))
            .clickable(action),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = symbol, style = TextStyle(color = ColorProvider(Color.White)))
    }
}

/* ---------------------------------------------------------------------- */
/*  Actions — talk directly to the Media3 session, never open an Activity  */
/* ---------------------------------------------------------------------- */

private fun controllerFuture(context: Context) =
    MediaController.Builder(context, SessionToken(context, android.content.ComponentName(context, PlaybackService::class.java)))
        .buildAsync()

class TogglePlayAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val future = controllerFuture(context)
        future.addListener({
            val controller = future.get()
            if (controller.isPlaying) controller.pause() else controller.play()
        }, MoreExecutors.directExecutor())
    }
}

class SkipNextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val future = controllerFuture(context)
        future.addListener({ future.get().seekToNext() }, MoreExecutors.directExecutor())
    }
}

class OpenAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("open_now_playing", true)
        }
        context.startActivity(intent)
    }
}

class MusicGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MusicGlanceWidget()
}
