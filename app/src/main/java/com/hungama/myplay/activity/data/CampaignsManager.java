package com.hungama.myplay.activity.data;

import android.content.Context;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CacheManager.Callback;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.CampaignUtils;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.campaigns.WeightedBucket;
import com.hungama.myplay.activity.data.dao.campaigns.intervaltree.IntervalTree;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CampaignCreateOperation;
import com.hungama.myplay.activity.services.CampaignsPreferchingService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

/**
 * Utility class for constructing and accesing compaigns.
 */
public class CampaignsManager implements CommunicationOperationListener {

	private static final String TAG = "CampaignsManager";

	public static final String NODES = "nodes";
	// public static final String CAMPAIGNS = "campaigns";

	private static final int ONLY_TIME_FORMAT = 6;

	private DataManager mDataManager;
	private Context mContext;
	private static CampaignsManager sIntance;
	private List<Node> nodes;
	private List<Campaign> campaigns;
	private OnGetCampaignsListener mOnGetCampaigns;

	private String returnType = Utils.TEXT_EMPTY;

	private static Map<Float, Placement> weightsMap;
	private ApplicationConfigurations mApplicationConfigurations;

	private long lastLoadingTime = 0;

	/**
	 * Bucket keys consist of String Placement priority + String Placement Type
	 */
	public HashMap<String, WeightedBucket> buckets = new HashMap<String, WeightedBucket>();

	public HashMap<String, IntervalTree<Placement>> intervalTree = new HashMap<String, IntervalTree<Placement>>();

	public static final synchronized CampaignsManager getInstance(
			Context applicationContext) {
		if (sIntance == null) {
			sIntance = new CampaignsManager(applicationContext);
		}
		return sIntance;
	}

	private CampaignsManager(Context context) {
		mContext = context;
		nodes = new ArrayList<Node>();
		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		init();
	}

	private void reloadCampaigns() {
		Logger.s(" ::::::::::::::-- reloadCampaigns");
		nodes = new ArrayList<Node>();
		buckets = new HashMap<String, WeightedBucket>();
		intervalTree = new HashMap<String, IntervalTree<Placement>>();
		init();
	}

	private void init() {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		loadCampaigns();
		lastLoadingTime = System.currentTimeMillis();
		// }
		// }).start();
	}

	private void loadCampaigns() {
		campaigns = mDataManager.getStoredCampaign();
		if (campaigns != null && !campaigns.isEmpty()) {
			List<Placement> campaignPlacements;
			HashMap<String, IntervalTree<Placement>> placementMap = new HashMap<String, IntervalTree<Placement>>();
			// Loop the Campaigns
			for (Campaign c : campaigns) {
				List<Placement> placements = c.getPlacements();

				if (!Utils.isListEmpty(placements)) {
					try {
						campaignPlacements = new ArrayList<Placement>(
								c.getPlacements());
						// Loop the Placements
						for (Placement placement : campaignPlacements) {
							try {
								placement.setCampaignID(c.getID());
								String key = String.valueOf(placement
										.getPriority())
										+ String.valueOf(placement
												.getPlacementType());
								IntervalTree<Placement> placementArr = placementMap
										.get(key);
								if (placementArr == null) {
									placementArr = new IntervalTree<Placement>();
									placementMap.put(key, placementArr);
								}
								placementArr.addInterval(
										placement.getEffectiveFromInLong(),
										placement.getEffectiveTillInLong(),
										placement);
							} catch (Exception e) {
							}

						}
					} catch (Exception e) {
					}
				}
			}
			intervalTree = placementMap;
			calculateWeightedBucket();
		}
	}

	// public static Map<Float, Placement> getWeightsMap() {
	// return weightsMap;
	// }

	public static void setWeightsMap(Map<Float, Placement> weightsMap) {
		CampaignsManager.weightsMap = weightsMap;
	}

	// ======================================================
	// GetCampaignsListener
	// ======================================================

	public interface OnGetCampaignsListener {

		public void onGetCampaignsNodes(List<Node> campaignsNodes);

		public void onGetCampaignsList(List<Campaign> campaignsList);
	}

	public void setOnGetCampaignsListener(OnGetCampaignsListener listener) {
		mOnGetCampaigns = listener;
	}

	// ======================================================
	// Helper Methods
	// ======================================================

	/**
	 * Populates the campaign nodes with their campaign's ID.
	 */
	public static void setCampaignIdForNode(Node node, String campaignId) {
		node.setCampaignID(campaignId);
		List<Node> nodes = node.getChildNodes();
		if (nodes != null && !nodes.isEmpty()) {
			for (Node n : nodes) {
				setCampaignIdForNode(n, campaignId);
			}
		} else {
			return;
		}
	}

	// public static Node findCampaignRootNodeByID(List<Campaign> campaigns,
	// String id) {
	// Node node = null;
	// if (campaigns != null && !campaigns.isEmpty()) {
	// for (Campaign campaign : campaigns) {
	// if (campaign.getID().equalsIgnoreCase(id)) {
	// node = campaign.getNode();
	// break;
	// }
	// }
	// }
	// return node;
	// }

	public static List<Placement> getAllPlacementsOfType(
			List<Campaign> campaigns, String type) {
		if (TextUtils.isEmpty(type.toString()))
			return null;

		if (Utils.isListEmpty(campaigns))
			return null;

		// Build a list of Placement
		// Note: i had to loop each Placement and set it's Campaign's id,
		// cause there is Campaign's id attribute in Placement object.
		List<Placement> campaignPlacements;
		List<Placement> resultPlacements = new ArrayList<Placement>();

		if (campaigns != null && !campaigns.isEmpty()) {
			// Loop the Campaigns
			for (Campaign c : campaigns) {
				List<Placement> placements = c.getPlacements();

				if (!Utils.isListEmpty(placements)) {
					try {
						campaignPlacements = new ArrayList<Placement>(
								c.getPlacements());
						// Loop the Placements
						for (Placement placement : campaignPlacements) {
							try {
								placement.setCampaignID(c.getID());
								if (placement.getPlacementType()
										.equalsIgnoreCase(type)) {
									resultPlacements.add(placement);
								}
							} catch (Exception e) {
							}

						}
					} catch (Exception e) {
					}
				}
			}

			// if(type.equals(ForYouActivity.PLACEMENT_TYPE_SPLASH) &&
			// sIntance!=null){
			// sIntance.init();
			// }
		}

		return resultPlacements;
	}

	public Placement getPlacementOfType(PlacementType type) {
		// System.out.println(" :::::::::::::::::::::: " + type);
		if (((mApplicationConfigurations.isRealUser() || Logger.allowPlanForSilentUser) &&
                mApplicationConfigurations.isUserHasSubscriptionPlan() && !mApplicationConfigurations
				.isUserHasTrialSubscriptionPlan()) ||
				((mApplicationConfigurations.isRealUser() || Logger.allowPlanForSilentUser) && mApplicationConfigurations.isUserHasTrialSubscriptionPlan() &&
						!mApplicationConfigurations.isShowAds())
				|| mApplicationConfigurations.getSaveOfflineMode()) {
			return null;
		}

		long updatedCampaignTime = CampaignsPreferchingService
				.getPrefTimeCampaign(mContext);
		Logger.s(" ::::::::::::::::::- - getPlacementOfType " + updatedCampaignTime + " :: " + lastLoadingTime);
		if (lastLoadingTime < updatedCampaignTime) {
			reloadCampaigns();
		}

		Placement placementToReturn = getPlacementByType(type.toString()
				.toLowerCase());
		// Placement placementToReturn = new Placement();
		if (placementToReturn == null) {
			if (Utils.isListEmpty(campaigns)) {
				campaigns = mDataManager.getStoredCampaign();
			}
			List<Placement> placements = getAllPlacementsOfType(campaigns,
					type.toString());

			if (Utils.isListEmpty(placements)) {
				return null;
			}
			Logger.s(" ::::::::::::::-- placements " + placements.size());
			// else{
			// for(Placement placement : placements){
			// System.out.println(placement.getTrackingID() +
			// " ::::::::::::::::;;; " + placement.getWeight());
			// // System.out.println("Placement :::::::::: " + new
			// Gson().toJson(placement));
			// }
			// }
			boolean useExcess = false;
			// placementToReturn = addPlacements(useExcess, placements);
			placementToReturn = getRandomPlacementFromPlacements(placements);

			if (placementToReturn == null) {
				useExcess = true;
				placementToReturn = addPlacements(useExcess, placements);
				if (placements == null) {
					return null;
				}
			}
		}
		if (type != PlacementType.AUDIO_AD && type != PlacementType.VIDEO_AD
				&& HomeActivity.metrics != null) {
			String imgUrl = Utils.getDisplayProfile(HomeActivity.metrics,
					placementToReturn);
			if (TextUtils.isEmpty(imgUrl)) {
				return null;
			}
		}
		// System.out.println(placementToReturn.getTrackingID() +
		// " 111::::::::::::::::;;; " + placementToReturn.getWeight());
		return placementToReturn;
	}

	public static Placement addPlacements(boolean useExcess,
			List<Placement> placements) {
		try {
			List<Placement> placementsToReturn = new ArrayList<Placement>();
			List<Float> weightsList = new ArrayList<Float>();
			weightsMap = new HashMap<Float, Placement>();
			float lastWeight = 0;

			for (Placement placement : placements) {
				int i = 0; // counter to fill the weightsArray
				if ((useExcess && placement.isExcess())
						|| (!useExcess && !placement.isExcess())) {
					if (isDateValid(placement)) {
						placementsToReturn.add(placement);

						if (weightsMap.isEmpty()) {
							weightsMap.put((float) 0, placement);
							weightsList.add((float) 0);
						} else {
							weightsMap.put(lastWeight, placement);
							weightsList.add((float) lastWeight);
							;
						}

						lastWeight = lastWeight + placement.getWeight();
					}
				}
			}
			float random = (float) (Math.random() * lastWeight);
			float selectedKey = 0;

			for (int j = 0; j <= weightsList.size() - 1; j++) {
				float currentWeightKey = weightsList.get(j);
				float nextWeight = weightsMap.get(weightsList.get(j))
						.getWeight();
				if (random >= currentWeightKey
						&& random < (currentWeightKey + nextWeight)) {
					selectedKey = currentWeightKey;
				}
			}

			return weightsMap.get(selectedKey);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return null;
	}

	public Placement getRandomPlacementFromPlacements(List<Placement> placements) {
		try {
			Collections.sort(placements);

			float sumofPlacements = 0f;
			for (int i = 0; i < placements.size(); i++) {
				if (placements.get(i) != null && isDateValid(placements.get(i)))
					sumofPlacements += placements.get(i).getWeight();
			}

			// Math.Random() will return float between 0.0 & 1.0
			float choice = (float) (Math.random() * sumofPlacements);
			float cum_total = 0f;
			float new_total = 0f;
			for (Placement p : placements) {
				if (isDateValid(p)) {
					new_total = cum_total + p.getWeight();
					Logger.s(cum_total + ":" + choice + ":" + new_total);
					if (cum_total < choice && (choice < new_total)) {
						return p;
					}
					cum_total = new_total;
				}
			}
		} catch (Exception e) {
			Logger.s(">>" + e);
			Logger.s(">>" + e.getMessage());
		}
		return null;
	}

	// public void getCampignsList()
	// {
	// mContext.startService(new
	// Intent(mContext,CampaignsPreferchingService.class));
	//
	// }

	private void buildNodesListFromCampaigns(List<Campaign> list) {
		nodes.clear();
		if (list != null && !list.isEmpty()) {
			for (Campaign c : list) {
				nodes.add(c.getNode());
			}
		}

	}

	public void clearCampaigns() {
		if (mDataManager == null) {
			mDataManager = DataManager.getInstance(mContext);
		}
		mDataManager.storeCampaignList(new ArrayList<String>());
		mDataManager.storeCampaign(new ArrayList<Campaign>(), new Callback() {

			@Override
			public void onResult(Boolean gotResponse) {
				// TODO Auto-generated method stub

			}
		});
		CampaignsPreferchingService.resetPrefTimeCampaign(mContext);
	}

	public void clearInstance() {
		sIntance = null;
		// System.gc();
	}

	public void returnCampaigns(List<Campaign> campaigns) {
		if (returnType != Utils.TEXT_EMPTY
				&& returnType.equalsIgnoreCase(NODES)) {
			buildNodesListFromCampaigns(campaigns);

			if (mOnGetCampaigns != null) {
				mOnGetCampaigns.onGetCampaignsNodes(nodes);
				Logger.i(TAG, "From Cache");
			}
		} else {
			if (mOnGetCampaigns != null) {
				mOnGetCampaigns.onGetCampaignsList(campaigns);
				Logger.i(TAG, "From Cache");
			}
		}
	}

	public static boolean isDateValid(Placement placement) {
		if (placement.getEffectiveFrom() == null
				&& placement.getEffectiveTill() == null) {
			return true;
		} else {
			Date from = null;
			Date till = null;
			Date today = null;
			String dateFormatUTC = "";
			if (placement.getEffectiveFrom() != null) {
				if (placement.getEffectiveFrom().length() > ONLY_TIME_FORMAT) {
					dateFormatUTC = "yyyy-MM-dd'T'HH:mm'Z'";
				} else {
					dateFormatUTC = "HH:mm'Z'";
				}
				from = Utils.convertTimeStampToDateCM(dateFormatUTC,
						placement.getEffectiveFrom());
				if (from == null) {
					return false;
				}
				Logger.i(TAG, "From: " + from);
			}

			if (placement.getEffectiveTill() != null) {
				if (placement.getEffectiveTill().length() > ONLY_TIME_FORMAT) {
					dateFormatUTC = "yyyy-MM-dd'T'HH:mm'Z'";
				} else {
					dateFormatUTC = "HH:mm'Z'";
				}
				till = Utils.convertTimeStampToDateCM(dateFormatUTC,
						placement.getEffectiveTill());
				if (till == null) {
					return false;
				}
				Logger.i(TAG, "Till: " + till);
			}

			SimpleDateFormat sdf = new SimpleDateFormat(dateFormatUTC);
			sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
			today = Utils.convertTimeStampToDateCM(dateFormatUTC,
					sdf.format(new Date()));

			if (today.after(from) && today.before(till)) {
				return true;
			}
			return false;
		}
	}

	// ======================================================
	// Communication callbacks.
	// ======================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.communication.CommunicationOperationListener
	 * #onStart(int)
	 */
	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ):

			break;

		case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ):

			break;

		default:
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.communication.CommunicationOperationListener
	 * #onSuccess(int, java.util.Map)
	 */
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ):

				// List<String> campaignList = (List<String>)
				// responseObjects.get(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST);
				//
				// mDataManager.getCampigns(this, campaignList);

				break;

			case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ):

				campaigns = (List<Campaign>) responseObjects
						.get(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN);

				returnCampaigns(campaigns);

				break;

			default:
				break;
			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.communication.CommunicationOperationListener
	 * #onFailure(int,
	 * com.hungama.myplay.activity.communication.CommunicationManager.ErrorType,
	 * java.lang.String)
	 */
	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (mOnGetCampaigns != null) {
			mOnGetCampaigns.onGetCampaignsNodes(null);
		}

	}

	// getPlacements

	private void calculateWeightedBucket() {
		HashMap<String, WeightedBucket> newBucket = new HashMap<String, WeightedBucket>();
		// Date now = new Date();
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis() / CampaignUtils.TIME_UTC_DIVISOR;
		for (Entry<String, IntervalTree<Placement>> i : intervalTree.entrySet()) {
			List<Placement> placements = i.getValue().get(now);
			// System.out.println("calculateWeightedBucket::::::::::::::::::::::: 4 "
			// +
			// placements.size());
			if (CampaignUtils.D)
				Logger.i(TAG, i.getValue().toString());
			if (!placements.isEmpty()) {
				// System.out.println("calculateWeightedBucket::::::::::::::::::::::: 5 "
				// + i.getKey());
				newBucket.put(
						i.getKey(),
						new WeightedBucket((Placement[]) placements
								.toArray(new Placement[placements.size()])));
			}
		}
		// System.out.println("calculateWeightedBucket::::::::::::::::::::::: 6 "
		// + newBucket.size());
		buckets = newBucket;
		// serializeBucket();
	}

	// private static final String TREE_SERIALIZED_FILE_NAME =
	// "intervalTree.tree";
	//
	// private void serializeTree() {
	// synchronized (intervalTree) {
	// init = true;
	// FileOutputStream fos;
	// ObjectOutputStream os;
	// try {
	// fos = mContext.openFileOutput(TREE_SERIALIZED_FILE_NAME,
	// Context.MODE_PRIVATE);
	// os = new ObjectOutputStream(fos);
	// os.writeObject(intervalTree);
	// os.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// @SuppressWarnings("unchecked")
	// private boolean loadTree() {
	// synchronized (intervalTree) {
	// boolean loaded = false;
	// FileInputStream fis;
	// ObjectInputStream is;
	// try {
	// fis = mContext.openFileInput(TREE_SERIALIZED_FILE_NAME);
	// is = new ObjectInputStream(fis);
	// HashMap<String, IntervalTree<Placement>> temp = (HashMap<String,
	// IntervalTree<Placement>>) is
	// .readObject();
	// if (temp != null && !temp.isEmpty()) {
	// intervalTree = temp;
	// loaded = true;
	// }
	// is.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (StreamCorruptedException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// return loaded;
	// }
	//
	// }
	//
	// private static final String BUCKET_SERIALIZED_FILE_NAME =
	// "weightedbucket.bucket";
	//
	// private void serializeBucket() {
	// HashMap<String, WeightedBucket> serialized = new HashMap<String,
	// WeightedBucket>();
	// for (String i : SERIALIZED_PLACEMENTS_KEYS) {
	// for (int j : Placement.PRIORITY_LIST) {
	// String key = String.valueOf(j) + i;
	// WeightedBucket bucket = buckets.get(key);
	// serialized.put(key, bucket);
	// }
	// }
	//
	// synchronized (buckets) {
	// init = true;
	// FileOutputStream fos;
	// ObjectOutputStream os;
	// try {
	// fos = mContext.openFileOutput(BUCKET_SERIALIZED_FILE_NAME,
	// Context.MODE_PRIVATE);
	// os = new ObjectOutputStream(fos);
	// os.writeObject(serialized);
	// os.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// @SuppressWarnings("unchecked")
	// private boolean loadBucket() {
	// synchronized (intervalTree) {
	// boolean loaded = false;
	// FileInputStream fis;
	// ObjectInputStream is;
	// try {
	// fis = mContext.openFileInput(BUCKET_SERIALIZED_FILE_NAME);
	// is = new ObjectInputStream(fis);
	// HashMap<String, WeightedBucket> temp = (HashMap<String, WeightedBucket>)
	// is
	// .readObject();
	// if (temp != null && !temp.isEmpty()) {
	// buckets = temp;
	// loaded = true;
	// }
	// is.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (StreamCorruptedException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// }
	// return loaded;
	// }
	// }

	public Placement getPlacementByType(String type) {
		// if (!init)
		// return getDefaultPlacementByType(type);
		Placement placement = null;
		for (int i : Placement.PRIORITY_LIST) {
			// System.out.println("::::::::::::::::::::::: 1 " +
			// String.valueOf(i)
			// + " ::: " + String.valueOf(type) + " ::: " + buckets.size());
			WeightedBucket bucket = buckets.get(String.valueOf(i)
					+ String.valueOf(type));
			if (bucket == null)
				continue;
			placement = bucket.getPlacement();
			if (placement != null && (isDateValid(placement)))
				break;
			else
				placement = null;
		}

		// if (placement == null)
		// placement = getDefaultPlacementByType(type);

		return placement;
	}
}
