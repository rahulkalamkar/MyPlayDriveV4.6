/**
 * 
 */
package com.hungama.myplay.activity.util;

/**
 * @author stas
 *
 */
public enum Needles {
	a(12), b(15), c(16), d(2), e(3), f(5), g(6), h(30), i(32), j(31), k(33), l(
			9), m(29), n(28), o(17), p(25), q(18), r(19), s(24), t(35), u(20), v(
			21), w(22), x(23), y(36), aa(26), ab(27), ac(10), ad(1), ae(4), // ;
	af(42), ag(40), ah(41), ai(11), aj(43), ak(45);
	private final int value;

	private Needles(int value) {
		this.value = value;
	}

	public static boolean contains(int test) {

		for (Needles c : Needles.values()) {
			if (c.value == test) {
				return true;
			}
		}

		return false;
	}
}
