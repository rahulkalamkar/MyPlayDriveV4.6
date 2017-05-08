package com.hungama.myplay.activity.player;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class PlayerWidgetProvider extends AppWidgetProvider {

	private static final String LOG = "PlayerWidgetProvider";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		context.startService(new Intent(context,
				PlayerUpdateWidgetService.class));
	}
}