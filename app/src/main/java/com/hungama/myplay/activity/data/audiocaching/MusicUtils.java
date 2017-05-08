package com.hungama.myplay.activity.data.audiocaching;

import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Util class for managing connection to the music service, and general helper
 * methods.
 */
public class MusicUtils {

	// public static class ServiceToken {
	// ContextWrapper mWrappedContext;
	// ServiceToken(ContextWrapper context) {
	// mWrappedContext = context;
	// }
	// }
	//
	// public static NowPlayingService sService = null;
	// private static HashMap<ContextWrapper, ServiceBinder> sConnectionMap =
	// new HashMap<ContextWrapper, ServiceBinder>();
	//
	// private static class ServiceBinder implements ServiceConnection {
	// ServiceConnection mCallback;
	// ServiceBinder(ServiceConnection callback) {
	// mCallback = callback;
	// }
	//
	// public void onServiceConnected(ComponentName className,
	// android.os.IBinder service) {
	// NowPlayingServiceBinder binder = (NowPlayingServiceBinder) service;
	// sService = binder.getService();
	//
	// if (mCallback != null) {
	// mCallback.onServiceConnected(className, service);
	// }
	// }
	//
	// public void onServiceDisconnected(ComponentName className) {
	// if (mCallback != null) {
	// mCallback.onServiceDisconnected(className);
	// }
	// sService = null;
	// }
	// }
	//
	// public static ServiceToken bindToService(Activity context) {
	// return bindToService(context, null);
	// }
	//
	// public static ServiceToken bindToService(Activity context,
	// ServiceConnection callback) {
	// Activity realActivity = context.getParent();
	// if (realActivity == null) {
	// realActivity = context;
	// }
	// ContextWrapper cw = new ContextWrapper(realActivity);
	// cw.startService(new Intent(cw, NowPlayingService.class));
	// ServiceBinder sb = new ServiceBinder(callback);
	// if (cw.bindService((new Intent()).setClass(cw, NowPlayingService.class),
	// sb, 0)) {
	// sConnectionMap.put(cw, sb);
	// return new ServiceToken(cw);
	// }
	// Log.e("Music", "Failed to bind to service");
	// return null;
	// }
	//
	// public static void unbindServiceMap()
	// {
	// for (Entry<ContextWrapper, ServiceBinder> i : sConnectionMap.entrySet())
	// {
	// ServiceBinder kill = i.getValue();
	// i.getKey().unbindService(kill);
	//
	// }
	// sConnectionMap.clear();
	// }
	//
	// public static void unbindFromService(ServiceToken token) {
	// if (token == null) {
	// Log.e("MusicUtils", "Trying to unbind with null token");
	// return;
	// }
	// // if (!sConnectionMap.containsValue(token))
	// // {
	// // Log.e("MusicUtils", "Token Doesn't exist!");
	// // return;
	// // }
	// ContextWrapper cw = token.mWrappedContext;
	// ServiceBinder sb = sConnectionMap.remove(cw);
	// if (sb == null) {
	// Log.e("MusicUtils", "Trying to unbind for unknown Context");
	// return;
	// }
	// cw.unbindService(sb);
	// if (sConnectionMap.isEmpty()) {
	// // presumably there is nobody interested in the service at this point,
	// // so don't hang on to the ServiceConnection
	//
	// //Alex: not stopping the service here, it may continue with playback even
	// if GUI classes do unbind
	// // if (sService != null) {
	// // // Bug1788 :: Daniel :: Added to bypass AlbumTab stop Service when low
	// on resources and onDestory gets called.
	// // String appType = cw.getString(R.string.app_type);
	// // if(!appType.equalsIgnoreCase(GlobalApplicationData.ALBUM))
	// // {
	// // sService.stopSelf();
	// // }
	// // //end
	// // }
	//
	// sService = null;
	// }
	// }
	//
	// public static void setListScreenHandler(Handler mListScreenHandler) {
	// if(sService != null) {
	// sService.setListScreenHandler(mListScreenHandler);
	// }
	// }
	//
	// public static Track getCurrentPlayingTrack() {
	// if (sService != null) {
	// return sService.getCurrentPlayingTrack();
	// }
	// return null;
	// }
	//
	// public static Itemable getCurrentParentItem() {
	// if(sService != null) {
	// return sService.getCurrentParentItem();
	// }
	// return null;
	// }
	//
	// public static int getCurrentPlayingPos() {
	// if (sService != null) {
	// return sService.getPlayingListPosition();
	// }
	// return -1;
	// }
	//
	// public static boolean isServicePlaying() {
	// if (sService == null) {
	// return false;
	// }
	//
	// return sService.isPlaying();
	// }
	//
	// public static boolean isServicePausing() {
	// if (sService == null) {
	// return false;
	// }
	//
	// return sService.isPaused();
	// }
	//
	// public static boolean isServiceLoading() {
	// if (sService == null) {
	// return false;
	// }
	//
	// return sService.isLoading();
	// }
	//
	// /**
	// * @return whether song has completed playing and player state is
	// completed
	// */
	// public static boolean hasCompletedPlay() {
	// if (sService == null) {
	// return false;
	// }
	//
	// return sService.hasCompletedPlay();
	// }
	//
	// public static void StopServiceIfExist() {
	// if (sService != null && (sService.isPlaying() || sService.isLoading() ||
	// sService.isPaused())) {
	// sService.stop();
	// }
	// }

	// @Deprecated
	// public static void encrypt(byte[] buffer, int bytesRead, int _k){
	// for (int i = 0; i < bytesRead; i++) {
	// int temp = buffer[i];
	// temp ^= _k;
	// buffer[i] = (byte) temp;
	// int input = (_k & 128) ^ ((_k & 4) << 5) ^ ((_k & 2) << 6)
	// ^ ((_k & 1) << 7);
	// _k = ((_k >> 1) | input) & 255;
	//
	// // Encrypt every 4th byte after 100kb
	// if (i >= 102400)// 100kb
	// i += 100;
	// }
	// }

	public static int synchsafe(int in) {
		int out = 0, mask = 0x7F;

		while ((mask ^ 0x7FFFFFFF) > 0) {
			out = in & ~mask;
			out <<= 1;
			out |= in & mask;
			mask = ((mask + 1) << 8) - 1;
			in = out;
		}

		return out;
	}

	public static byte[] createId3(Track track) throws IOException {

		// TODO:: FLUSH BEFORE referencing.

		// added space because google Music was cutting off the first Letter.
		int id3Version = 3;
		String title = track.getTitle();
		String album = track.getAlbumName();
		String artist = track.getArtistName();
		int trackNumber = track.getTrackNumber();
		ByteArrayOutputStream file = new ByteArrayOutputStream();
		ByteArrayOutputStream allocater = new ByteArrayOutputStream();
		byte[] header = {};
		byte[] titleFrame = {};
		byte[] albumFrame = {};
		byte[] artistFrame = {};
		byte[] trackNumberFrame = {};
		byte[] privFrame = {};

		// Create Title frame
		allocater.write("TIT2".getBytes());
		byte[] len = ByteBuffer.allocate(4).putInt(title.length() + 1).array();
		allocater.write(len);
		allocater.write(0);
		allocater.write(0);
		allocater.write(0);
		allocater.write(title.getBytes());

		allocater.flush();
		titleFrame = allocater.toByteArray();

		allocater.reset();

		// Create Artist frame
		allocater.write("TPE1".getBytes());
		len = ByteBuffer.allocate(4).putInt(artist.length() + 1).array();
		allocater.write(len);
		allocater.write(0);
		allocater.write(0);
		allocater.write(0);
		allocater.write(artist.getBytes());

		artistFrame = allocater.toByteArray();

		allocater.flush();
		allocater.reset();

		// Create Album frame
		allocater.write("TALB".getBytes());
		len = ByteBuffer.allocate(4).putInt(album.length() + 1).array();
		allocater.write(len);
		allocater.write(0);
		allocater.write(0);
		allocater.write(0);
		allocater.write(album.getBytes());

		albumFrame = allocater.toByteArray();

		allocater.flush();
		allocater.reset();

		// Create Track Number frame
		allocater.write("TRCK".getBytes());
		len = ByteBuffer.allocate(4).putInt((" " + trackNumber).length() + 1)
				.array();
		allocater.write(len);
		allocater.write(0);
		allocater.write(0);
		allocater.write(0);
		allocater.write((" " + trackNumber).getBytes());

		trackNumberFrame = allocater.toByteArray();

		allocater.flush();
		allocater.reset();

		// Create Priv
		privFrame = track.getOwnerBlob();

		// Create Padding
		byte[] padding = new byte[40];

		// Create Header
		allocater.write("ID3".getBytes());
		allocater.write(id3Version);
		allocater.write(0);
		allocater.write(0);
		int id3Size = titleFrame.length + artistFrame.length
				+ albumFrame.length + privFrame.length
				+ trackNumberFrame.length + padding.length;

		len = ByteBuffer.allocate(4).putInt(synchsafe(id3Size)).array();
		allocater.write(len);

		header = allocater.toByteArray();

		allocater.flush();
		allocater.close();

		file.write(header);
		file.write(titleFrame);
		file.write(artistFrame);
		file.write(albumFrame);
		file.write(trackNumberFrame);
		file.write(privFrame);
		file.write(padding);

		byte[] fileArr = file.toByteArray();
		file.flush();
		file.close();

		return fileArr;

	}

	/**
	 * Parse Id3 tag and return the offset of the begining of the id3 frames
	 * 
	 * @param byteArray
	 *            data to parse
	 * @param TAG
	 *            log function
	 * @return offset
	 * @throws IOException
	 */
	public static int parse(ByteArrayOutputStream byteArray, String TAG)
			throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(
				byteArray.toByteArray());
		int offset = 0;

		// Read the first five bytes of the ID3 header - http://id3.org/id3v2-00
		byte[] headerbuf = new byte[10];
		is.read(headerbuf);
		offset += 10;
		// Parse it quickly
		if (headerbuf[0] != 'I' || headerbuf[1] != 'D' || headerbuf[2] != '3') {
			is.close();
			return -1;
		}

		// True if the tag is pre-V3 tag (shorter headers)
		final int TagVersion = headerbuf[3];

		// Check the version
		if (TagVersion < 0 || TagVersion > 4) {
			is.close();
			return -1;
		}

		// Get the ID3 tag size and flags; see 3.1
		int tagsize = (headerbuf[9] & 0xFF) | ((headerbuf[8] & 0xFF) << 7)
				| ((headerbuf[7] & 0xFF) << 14) | ((headerbuf[6] & 0xFF) << 21)
				+ 10;
		boolean uses_synch = (headerbuf[5] & 0x80) != 0 ? true : false;
		boolean has_extended_hdr = (headerbuf[5] & 0x40) != 0 ? true : false;

		// Read the extended header length and skip it
		if (has_extended_hdr) {

			int headersize = is.read() << 21 | is.read() << 14 | is.read() << 7
					| is.read();
			if (is.available() + offset < headersize) {
				is.close();
				return -1;
			}

			is.skip(headersize - 4);
			offset += headersize - 4;
		}

		if (offset > 0)
			return offset;

		// Read the whole tag
		byte[] buffer = new byte[tagsize];
		if (is.available() + offset < tagsize) {
			is.close();
			return -1;
		}
		is.read(buffer);
		is.close();

		// Prepare to parse the tag
		int length = buffer.length;

		// Recreate the tag if desynchronization is used inside; we need to
		// replace 0xFF 0x00 with 0xFF
		if (uses_synch) {
			int newpos = 0;
			byte[] newbuffer = new byte[tagsize];

			for (int i = 0; i < buffer.length; i++) {
				if (i < buffer.length - 1 && (buffer[i] & 0xFF) == 0xFF
						&& buffer[i + 1] == 0) {
					newbuffer[newpos++] = (byte) 0xFF;
					i++;
					continue;
				}

				newbuffer[newpos++] = buffer[i];
			}

			length = newpos;
			buffer = newbuffer;
		}

		// Set some params
		int pos = 0;
		final int ID3FrameSize = TagVersion < 3 ? 6 : 10;

		int lastFrame = 0;
		// Parse the tags
		while (true) {
			int rembytes = length - pos;

			// Do we have the frame header?
			if (rembytes < ID3FrameSize) {
				// pos += lastFrame;
				break;
			}
			// Is there a frame?
			if (buffer[pos] < 'A' || buffer[pos] > 'Z') {
				// pos += lastFrame;
				break;
			}
			// Frame name is 3 chars in pre-ID3v3 and 4 chars after
			String framename;
			int framesize;

			if (TagVersion < 3) {
				framename = new String(buffer, pos, 3);
				Logger.i(TAG, framename);
				framesize = ((buffer[pos + 5] & 0xFF) << 8)
						| ((buffer[pos + 4] & 0xFF) << 16)
						| ((buffer[pos + 3] & 0xFF) << 24);
			} else {
				framename = new String(buffer, pos, 4);
				Logger.i(TAG, framename);
				framesize = (buffer[pos + 7] & 0xFF)
						| ((buffer[pos + 6] & 0xFF) << 8)
						| ((buffer[pos + 5] & 0xFF) << 16)
						| ((buffer[pos + 4] & 0xFF) << 24);
			}

			if (pos + framesize > length) {
				pos += framesize + ID3FrameSize;
				break;
			}
			// if ( framename.equals( "TPE1" ) || framename.equals( "TPE2" ) ||
			// framename.equals( "TPE3" ) || framename.equals( "TPE" ) )
			// {
			// if ( m_artist == null )
			// m_artist = parseTextField( buffer, pos + ID3FrameSize, framesize
			// );
			// }
			//
			// if ( framename.equals( "TIT2" ) || framename.equals( "TIT" ) )
			// {
			// if ( m_title == null )
			// m_title = parseTextField( buffer, pos + ID3FrameSize, framesize
			// );
			// }
			lastFrame = framesize + ID3FrameSize;
			pos += framesize + ID3FrameSize;
			continue;
		}

		return pos + offset;
	}
}
