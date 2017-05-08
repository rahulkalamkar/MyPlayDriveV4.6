/**
 * 
 */
package com.hungama.myplay.activity.gigya;

import java.util.List;

/**
 * @author DavidSvilem
 *
 */
public class FBFriend implements Comparable<FBFriend> {

	public String UID;
	public String nickname;
	// public String photoURL;
	public String thumbnailURL;

	// public boolean isSiteUser;
	// public boolean isSiteUID;

	List<Identity> identities;

	public FBFriend() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FBFriend another) {// xtpl
		if (another.nickname != null && nickname != null)
			return String.CASE_INSENSITIVE_ORDER.compare(nickname,
					another.nickname);
		else if (nickname == null && another.nickname != null)
			return -1;
		else if (another.nickname == null && nickname != null)
			return 1;
		else
			return 0;
	}
}
