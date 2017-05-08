/**
 * 
 */
package com.hungama.myplay.activity.gigya;

/**
 * @author DavidSvilem
 *
 */
public class GoogleFriend implements Comparable<GoogleFriend> {

	public String email;
	public String firstName;
	// public String lastName;
	public String nickname;

	// public String provider;

	public GoogleFriend() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GoogleFriend another) {// xtpl
		if (another.firstName != null && firstName != null)
			return String.CASE_INSENSITIVE_ORDER.compare(firstName,
					another.firstName);
		else if (firstName == null && another.firstName != null)
			return -1;
		else if (another.firstName == null && firstName != null)
			return 1;
		else {
			if (another.email != null && email != null) {
				return String.CASE_INSENSITIVE_ORDER.compare(email,
						another.email);
			} else if (email == null)
				return -1;
			else if (another.email == null)
				return 1;
			return 0;
		}
	}
}
