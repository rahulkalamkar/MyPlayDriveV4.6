package com.hungama.myplay.activity.services;

import android.app.IntentService;
import android.content.Intent;

import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.util.Logger;

public class ServerCommandsService extends IntentService {

	private static final String TAG = "ServerCommandsService";

	public static final String COMMAND = "command";

	// Commands type
	public static String INVENTORY_UPDATE = "10004";

	public ServerCommandsService() {
		super(TAG);
	}

	public ServerCommandsService(String name) {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.i(TAG, "Starts Server Commands Service.");
		String command = null;

		if (intent != null)
			command = intent.getStringExtra(COMMAND);

		if (command != null) {

			if (command.equalsIgnoreCase(INVENTORY_UPDATE)) {

				// InventoryLight Intent Service

				InventoryLightService.callService(
						getApplicationContext(),
						ApplicationConfigurations.getInstance(
								getApplicationContext()).isRealUser());
				// Intent inventoryLightService =
				// new Intent(getApplicationContext(),
				// InventoryLightService.class);
				// startService(inventoryLightService);
			}

		}
		if (intent != null)
			Logger.i(TAG, "Done Server Commands Service.");
		else
			Logger.i(TAG, "Done Server Commands Service.with intent null");
	}

}
