/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hungama.myplay.activity;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.hungama.hungamamusic.lite.R;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.NotificationFactory;
import com.urbanairship.util.NotificationIDGenerator;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class RichPushNotificationFactory extends NotificationFactory {

	public RichPushNotificationFactory(Context context) {
		super(context);
	}

	@Override
	public Notification createNotification(PushMessage message,
			int notificationId) {
		// Build the notification
		Context context = UAirship.getApplicationContext();
		Resources res = context.getResources();
		/*
		 * List<RichPushMessage> unreadMessages = UAirship.shared()
		 * .getRichPushManager().getRichPushInbox().getUnreadMessages();
		 * Log.i("UnreadMessage",""+unreadMessages);
		 */

		/*
		 * int totalUnreadCount = unreadMessages.size(); String title =
		 * res.getQuantityString( R.plurals.inbox_notification_title,
		 * totalUnreadCount, totalUnreadCount);
		 */
//		NotificationCompat.Builder bulider=new NotificationCompat.Builder(getContext());
		 NotificationCompat.Builder bulider;
		 Bundle bundle = message.getPushBundle();
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN
				&& bundle.containsKey("big_picture")) {

			// String imageurl = bundle.getString("big_picture");

			if ( !bundle.getString("big_picture").toString().isEmpty()) {
				String imageurl = bundle.getString("big_picture");
				Bitmap remote_picture = null;

				try {
					remote_picture = BitmapFactory
							.decodeStream((InputStream) new URL(imageurl)
									.getContent());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					remote_picture=null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					remote_picture=null;
				}

				bulider = getDefaultNotificationBuilder(message, remote_picture,bundle,true);
			} else {
				bulider = getDefaultNotificationBuilder(message, null,bundle,true);
			}

		} else {

			Bitmap bigicon_picture = null;
			bigicon_picture = BitmapFactory.decodeResource(getContext()
					.getResources(), R.drawable.icon_launcher);

			if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

				bulider = getDefaultNotificationBuilder(message, null,bundle,false);
			} else {
				bulider = new NotificationCompat.Builder(getContext())
						.setSmallIcon(R.drawable.ic_notification)
						.setAutoCancel(true)
						.setLargeIcon(bigicon_picture)
						.setContentTitle("Hungama Music")
						.setSound(
								Uri.parse("android.resource://"
										+ getContext().getPackageName() + "/"
										+ R.raw.notify_))
						.setContentText(message.getAlert());
			}

		}

		bulider.extend(createNotificationActionsExtender(message,
				notificationId));

		return bulider.build();
	}

	private NotificationCompat.Builder getDefaultNotificationBuilder(PushMessage message, Bitmap remote_picture,Bundle bundle,boolean isfromimage){
		Bitmap bigicon_picture = null;
		bigicon_picture = BitmapFactory.decodeResource(getContext()
				.getResources(), R.drawable.icon_launcher);

		NotificationCompat.Builder bulider = new NotificationCompat.Builder(getContext())
				.setSmallIcon(R.drawable.ic_notification)
				.setAutoCancel(true)
				.setLargeIcon(bigicon_picture)
				.setPriority(Notification.PRIORITY_HIGH)
				.setContentTitle("Hungama Music")
				.setSound(
						Uri.parse("android.resource://"
								+ getContext().getPackageName() + "/"
								+ R.raw.notify_));

		if(remote_picture != null) {
			bulider.setContentText(message.getAlert());
			NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
			// notiStyle.setBigContentTitle(title);
			notiStyle.setSummaryText(message.getAlert());
			notiStyle.bigPicture(remote_picture);

			bulider.setStyle(notiStyle);
		} else {
			if(!bundle.containsKey("alt_text")){
				bulider.setContentText(message.getAlert());
				bulider.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText(message.getAlert()));
			}else{
				if(!bundle.getString("alt_text").toString().isEmpty() && isfromimage){
					bulider.setContentText(bundle.getString("alt_text"));
					bulider.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(bundle.getString("alt_text")));
				}else{
					bulider.setContentText(message.getAlert());
					bulider.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(message.getAlert()));
				}
			}

		}
		return bulider;
	}

	@Override
	public int getNextId(PushMessage pushMessage) {
		return NotificationIDGenerator.nextID();
	}
}
