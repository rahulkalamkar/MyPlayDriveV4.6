package com.hungama.myplay.activity.data.dao.hungama;

import com.hungama.myplay.activity.util.Logger;

import java.io.Serializable;

/**
 * Enumeration definition types of {@link DownloadPlan} types.
 */
public enum PlanType implements Serializable {
	MOBILE, GOOGLE, TRIAL, REEDEM, REDEEM, INVALID, PROMO, CARD, WALLETS, IVR;

	public static final PlanType getPlanByName(String name) {
		try {
			if (name.equalsIgnoreCase(GOOGLE.toString())) {
				return GOOGLE;
			} else if (name.equalsIgnoreCase(REEDEM.toString())) {
				return REEDEM;
			} else if (name.equalsIgnoreCase(REDEEM.toString())) {
				return REEDEM;
			} else if (name.equalsIgnoreCase(MOBILE.toString())) {
				return MOBILE;
			} else if (name.equalsIgnoreCase(TRIAL.toString())) {
				return TRIAL;
			} else if (name.equalsIgnoreCase(PROMO.toString())) {
				return PROMO;
			} else if (name.equalsIgnoreCase(CARD.toString())) {
                return CARD;
            } else if (name.equalsIgnoreCase(WALLETS.toString())) {
                return WALLETS;
            } else if (name.equalsIgnoreCase(IVR.toString())) {
                return IVR;
            }
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return INVALID;
	}
}
