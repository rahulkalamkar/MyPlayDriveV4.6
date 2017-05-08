package com.hungama.myplay.activity.player;

import com.google.gson.Gson;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all tracks in a synchronized queue-like playing sequence.
 */
public class PlayingQueue {

	public static final int POSITION_NOT_AVAILABLE = -1;

	private List<Track> mQueue;
	private int mCurrentPosition;

	private static final int MIN_INDEX = 0;
	PlayerService service;

	public PlayingQueue(List<Track> tracks, int currentPosition,
			PlayerService service) {
		this.service = service;
		if (tracks != null && tracks.size() > 0) {

			mQueue = new ArrayList<Track>(tracks);
			mCurrentPosition = currentPosition;

		} else {

			mQueue = new ArrayList<Track>();
			mCurrentPosition = 0;
		}
	}

	public synchronized int size() {
		return mQueue.size();
	}

	public synchronized int getCurrentPosition() {
		return mCurrentPosition;
	}

	public synchronized Track getCurrentTrack() {
		try {
			if ((mQueue.size() - 1) >= mCurrentPosition) {
				return mQueue.get(mCurrentPosition);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return null;
	}

	public synchronized Track getNextTrack() {
		if ((mQueue.size() - 1) >= mCurrentPosition + 1) {
			return mQueue.get(mCurrentPosition + 1);
		}
		return null;
	}

	public synchronized Track getNextTrack(int pos) {
		if ((mQueue.size() - 1) >= pos) {
			return mQueue.get(pos);
		}
		return null;
	}

	public synchronized Track getTrackFromPos(int pos) {
		if ((mQueue.size() - 1) >= pos) {
			return mQueue.get(pos);
		}
		return null;
	}

	public synchronized Track getPreviousTrack() {
		if (MIN_INDEX <= mCurrentPosition - 1) {
			return mQueue.get(mCurrentPosition - 1);
		}
		return null;
	}

	public synchronized boolean hasNext() {
		if ((mQueue.size() - 1) >= mCurrentPosition + 1) {
			return true;
		}
		return false;
	}

	public synchronized boolean hasPrevious() {
		if (MIN_INDEX <= mCurrentPosition - 1) {
			return true;
		}
		return false;
	}

	public synchronized Track next() {
		if ((mQueue.size() - 1) >= mCurrentPosition + 1) {
			mCurrentPosition++;
			return mQueue.get(mCurrentPosition);

		} else {
			return null;
		}
	}

	public synchronized Track previous() {
		if (MIN_INDEX <= mCurrentPosition - 1) {
			mCurrentPosition--;
			return mQueue.get(mCurrentPosition);
		} else {
			return null;
		}
	}

	public synchronized void addNext(List<Track> tracks) {
		// if the current position is the last, just add it to the end of the
		// queue.
		try {
			if (mCurrentPosition == mQueue.size() - 1 || (mQueue.size() == 0)) {
				mQueue.addAll(tracks);
			} else {
				mQueue.addAll(mCurrentPosition + 1, tracks);
			}
			savePlayerQueue();
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":108", e.toString());
		}
	}

	public synchronized void addToCurrent(List<Track> tracks) {
		// if the current position is the last, just add it to the end of the
		// queue.
		try {
			// if (mCurrentPosition == mQueue.size() - 1 || (mQueue.size() ==
			// 0)) {
			// mQueue.addAll(tracks);
			// } else {
			mQueue.addAll(mCurrentPosition, tracks);
			// }
			savePlayerQueue();
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":108", e.toString());
		}
	}

	public synchronized void addToQueue(List<Track> tracks) {
		mQueue.addAll(tracks);
		savePlayerQueue();
	}

	public synchronized Track goTo(int position) {
		// checks if the position in the Playinglist scope.
		if (MIN_INDEX <= position && ((mQueue.size() - 1) >= position)
				&& position != mCurrentPosition) {
			// sets the new position and retrieves the value of the new one.
			mCurrentPosition = position;
			return mQueue.get(position);
		}
		return null;
	}

	public synchronized Track goToNew(int position) {
		// checks if the position in the Playinglist scope.
		if (MIN_INDEX <= position && ((mQueue.size() - 1) >= position)) {
			// sets the new position and retrieves the value of the new one.
			mCurrentPosition = position;
			return mQueue.get(position);
		}
		return null;
	}

	public synchronized Track removeFrom(int position) {
		if (MIN_INDEX <= position && ((mQueue.size() - 1) >= position)) {
			// if (position <= mCurrentPosition && position >= 0) {
			// mCurrentPosition--;
			// }
			if (mCurrentPosition == -1 && mQueue.size() > 1) {
				mCurrentPosition = 0;
			} else if (position == mCurrentPosition && position == 0
					&& mQueue.size() == 1) {
				mCurrentPosition = -1;
			} else if (position == mCurrentPosition && position >= 0
					&& (mQueue.size() - 1) > position) {
				// mCurrentPosition--;
			} else if (position == mCurrentPosition && position >= 0
					&& (mQueue.size() - 1) == position) {
				mCurrentPosition--;
			} else if (position <= mCurrentPosition && position >= 0) {
				mCurrentPosition--;
			}
			Track removedTrack = mQueue.remove(position);
			savePlayerQueue();
			return removedTrack;
		}
		return null;
	}

	public void setCurrentPos(int currentPos){
		this.mCurrentPosition = currentPos;
	}
	
	/**
	 * Retrieves a new deep copy of the list.
	 * 
	 * @return
	 */
	public synchronized List<Track> getCopy() {
		List<Track> tracks = new ArrayList<Track>();
		if(mQueue!=null)
			for (Track track : mQueue) {
				try{
					tracks.add(track.newCopy());
				}catch (Exception e){}
			}
		return tracks;
	}

	// ======================================================
	// For cached tracks when there is no connectivity.
	// ======================================================

	// public synchronized boolean hasNextCached() {
	// int index = mCurrentPosition + 1;
	// int maxIndex = mQueue.size() - 1;
	// if (maxIndex >= index) {
	// Track track = null;
	// for(; index <= maxIndex; index++) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	//
	// public synchronized boolean hasPreviousCached() {
	// int index = mCurrentPosition - 1;
	// if (MIN_INDEX <= index) {
	// Track track = null;
	// for(; index >= MIN_INDEX; index--) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	//
	// public synchronized Track nextCachedTrack() {
	// int index = mCurrentPosition + 1;
	// int maxIndex = mQueue.size() - 1;
	// if (maxIndex >= index) {
	// Track track = null;
	// for(; index <= maxIndex; index++) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// mCurrentPosition = index;
	// return track;
	// }
	// }
	// }
	// return null;
	// }
	//
	// public synchronized Track previousCachedTrack() {
	// int index = mCurrentPosition - 1;
	// if (MIN_INDEX <= index) {
	// Track track = null;
	// for(; index >= MIN_INDEX; index--) {
	// track = mQueue.get(index);
	// if (track.isCached()) {
	// mCurrentPosition = index;
	// return track;
	// }
	// }
	// }
	// return null;
	// }

	/**
	 * Creates new shuffled playing queue from the given one, when the current
	 * playing Track will be the first an all the rest will be played after it
	 * but in a scrambled sequence.
	 */
	public static PlayingQueue createShuffledQueue(PlayingQueue playingQueue,
			PlayerService service) {

		PlayingQueue scrambledPlayingQueue = null;

		// checks for end point scenarios.
		List<Track> tracks = playingQueue.getCopy();

		// if (Utils.isListEmpty(tracks)) {
		// throw new IllegalArgumentException("Given Plaing queue is empty!");
		// }

		try {

			if (!Utils.isListEmpty(tracks)) {

				if (tracks.size() == 1) {
					// just return the queue to the nudnik.
					return playingQueue;
				}

				/*
				 * Does the following: 1. gets the current track and it's
				 * position (in case it appears couple of times in the queue).
				 * 2. aggregates all the tracks that are not the same one. 3.
				 * shuffles all the tracks. 4. adds the current one to be the
				 * first.
				 */
				Track currentTrack = playingQueue.getCurrentTrack();
				int currentTrackPosition = playingQueue.getCurrentPosition();

				List<Track> tracksToScramble = new ArrayList<Track>();

				int size = tracks.size();
				Track track = null;

				for (int i = 0; i < size; i++) {

					track = tracks.get(i);
					if (track.getId() != currentTrack.getId()
							&& i != currentTrackPosition) {
						tracksToScramble.add(track.newCopy());
					}
				}

				// Everyday I'm shuffling.
				Collections.shuffle(tracksToScramble);

				// adds the current track to be the first.
				tracksToScramble.add(0, currentTrack.newCopy());

				scrambledPlayingQueue = new PlayingQueue(tracksToScramble,
						MIN_INDEX, service);

			} else {

				scrambledPlayingQueue = new PlayingQueue(tracks, MIN_INDEX,
						service);
			}

		} catch (IllegalArgumentException e) {

			new IllegalArgumentException("Given Plaing queue is empty!");
		}

		return scrambledPlayingQueue;
	}

	// xtpl
	public synchronized void setCurrentTrack(long trackId) {
		for (int i = 0; i < mQueue.size(); i++) {
			Track track = mQueue.get(i);
			if (track.getId() == trackId) {
				mCurrentPosition = i;
				break;
			}
		}
	}

	public void updateTrack(Track track) {
		if (mQueue != null) {
			for (int i = 0; i < mQueue.size(); i++) {
				// System.out.println(mQueue.get(i).getId() +
				// " ::::::: updateTrack ::::::: " + track.getId());
				if (mQueue.get(i).getId() == track.getId()) {
					// System.out.println("Track found :::::::::::::: " + i);
					mQueue.set(i, track);
					break;
				}
			}
			savePlayerQueue();
		}
	}

	public Track trackDragAndDrop(int from, int to) {
		if (mQueue != null) {
			try {
				Track currentTrack = getCurrentTrack();

				// if ((from < mCurrentPosition && to < mCurrentPosition)
				// || (from > mCurrentPosition && to > mCurrentPosition)) {
				Track track = mQueue.get(from);
				mQueue.remove(from);
				mQueue.add(to, track);

				// } else
				// if (from == mCurrentPosition) {
				// final Track track = mQueue.get(from);
				// mQueue.remove(from);
				// mQueue.add(to, track);
				// mCurrentPosition = to;
				// } else if (to == mCurrentPosition && from < to) {
				// final Track track = mQueue.get(from);
				// mQueue.remove(from);
				// mQueue.add(to, track);
				// mCurrentPosition--;
				// } else if (to == mCurrentPosition && from > to) {
				// final Track track = mQueue.get(from);
				// mQueue.remove(from);
				// mQueue.add(to, track);
				// mCurrentPosition++;
				// }
				updateCurrentPlayingPosition(currentTrack);
				savePlayerQueue();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
		return null;
	}

	// xtpl

	private void updateCurrentPlayingPosition(Track currentTrack) {
		// Track currentTrack = getCurrentTrack();
		if (currentTrack != null && mQueue != null && mQueue.size() > 0) {
			mCurrentPosition = mQueue.indexOf(currentTrack);
		}
	}

	public static Object sync = new Object();

	public void savePlayerQueue() {
		if (service == null) {
			service = PlayerService.service;
		}
		if (service == null)
			return;
		//service.updateCastingQueue();
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (sync) {

					if (service.getPlayMode() != null
							&& service.getPlayMode() == PlayMode.MUSIC) {
						ArrayList<Track> temp = new ArrayList<Track>(mQueue);
						try {
							// Set<String> setTracks = new HashSet<String>();
							// ArrayList<String> queueToSave = new
							// ArrayList<String>();
							JSONArray jsonTracks = new JSONArray();
							for (Track track : temp) {
								// System.out.println(" ::::::: " + new
								// Gson().toJson(track));
								// queueToSave.add(new Gson().toJson(track));
								jsonTracks.put(new Gson().toJson(track));
							}
							// setTracks.addAll(queueToSave);
							// System.out.println("Total tracks in queue :::: "
							// +
							// jsonTracks.length());

							service.mApplicationConfigurations
									.setPlayerQueue(jsonTracks.toString());
						} catch (Error e) {
							System.gc();
							System.runFinalization();
							System.gc();
						} catch (Exception e) {
							System.gc();
							System.runFinalization();
							System.gc();
						}
					}
				}
			};
		});

	}

    public synchronized Track getTrack(int position) {
        if (MIN_INDEX <= position && ((mQueue.size() - 1) >= position)) {
            return mQueue.get(position);
        }
        return null;
    }

	public void clearQueue(){
		if(mQueue!=null){
			mQueue.clear();
		}
	}
}
