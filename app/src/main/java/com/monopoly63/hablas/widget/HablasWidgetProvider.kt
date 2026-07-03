package com.monopoly63.hablas.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.monopoly63.hablas.MainActivity
import com.monopoly63.hablas.R
import com.monopoly63.hablas.playback.PlayerState

class HablasWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, buildViews(context, null)) }
    }

    companion object {
        fun update(context: Context, state: PlayerState?) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, HablasWidgetProvider::class.java))
            ids.forEach { manager.updateAppWidget(it, buildViews(context, state)) }
        }

        private fun buildViews(context: Context, state: PlayerState?): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_hablas)
            views.setTextViewText(R.id.widget_title, state?.title ?: "Hablas")
            views.setTextViewText(R.id.widget_artist, state?.artist ?: "Native glass music player")
            views.setImageViewResource(R.id.widget_play_pause, if (state?.isPlaying == true) R.drawable.ic_widget_pause else R.drawable.ic_widget_play)
            views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 10, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            views.setOnClickPendingIntent(R.id.widget_previous, action(context, ACTION_WIDGET_PREVIOUS, 11))
            views.setOnClickPendingIntent(R.id.widget_play_pause, action(context, ACTION_WIDGET_PLAY_PAUSE, 12))
            views.setOnClickPendingIntent(R.id.widget_next, action(context, ACTION_WIDGET_NEXT, 13))
            return views
        }

        private fun action(context: Context, action: String, request: Int): PendingIntent = PendingIntent.getBroadcast(
            context,
            request,
            Intent(context, WidgetControlReceiver::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
