/**
 * 
 */
package com.hungama.myplay.activity.util;

/**
 * @author David
 *
 */
public class FlurryConstants {

	//
	// Enums Flurry
	//
	public enum FlurryEventName {

		SongSelectedForPlay("Song selected for play"), VideoSelected(
				"Video Selected"), FullPlayer("Full Player"), FullPlayerLyrics(
				"Full Player Lyrics"), FullPlayerTrivia("Full Player Trivia"), Download(
				"Download"), SimilarSongs("Similar Songs"), AlbumSongs(
				"Album Songs"), SimilarSongsResultClicked(
				"Similar Songs Result Clicked"), InfoTab("Info Tab"), TappedOnInfoLink(
				"Tapped On Info Link"), RelatedVideos("Related Videos"), TappedOnAnyRelatedVideo(
				"Tapped on any related video"), FavoriteButton(
				"Favorite Button"), DiscoveryMood("Discovery Mood"),
		// DiscoveryPreference("Discovery Preference"),
		DiscoveryResultClicked("Discovery - Result Clicked"), DiscoveryEra(
				"Discovery Era"), DiscoveryOfTheDay("Discovery of the day"), DiscoveryTempo(
				"Discovery Tempo"), PlaylistDetail("Playlist Detail"), SongDetail(
				"Song Detail"), AlbumDetail("Album Detail"), MusicSection(
				"Music Section"), VideoSection("Video Section"), VideoDetail(
				"Video Detail"), LiveRadio("Live Radio"), TopArtistRadio(
				"Top Artist Radio"),
		// Specials("Specials"),
		// PushNotificationPushed("Push Notification Pushed"),
		MusicSection3dots("Music Section 3 dots"), VideosSection3dots(
				"Videos Section 3 dots"), OfflineSongs3dots(
				"Offline Songs 3 dots"), ThreeDotsClicked("Three_Dots_Clicked"), OfflineSongsPlayAll(
				"Offline Songs Play All"), ContextMenuonMusicNew(
				"Context Menu on Music New"), ContectMenuonMusicPopular(
				"Contect Menu on Music Popular"), ContextMenuGenre(
				"Context Menu Genre"), TileClicked("Tile Clicked"), BrowseBy(
				"Browse By"), RadioTopButtons("Radio_Top Buttons"), AppOpen(
				"App Open"),
		TweetThis("Tweet This");

		private final String value;

		private FlurryEventName(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryKeys {

		Album("Album"), // Album
		Playlist("Playlist"), // Playlist
		Song("Song"), // Song
		Video("Video"), // Video
		Radio("Radio"), // Radio
		TitleOfTheSong("Title of the song"), TitleContentID("Title_ContentID"), SubSection(
				"SubSection"), Section("Section"), Source("Source"), SourceSection(
				"Source Section"),

		Title("Title"), Type("Type"), HashTag("HashTag"),

		WhichInfoTapped("Which info tapped"), KeywordSearch("Keyword search"),

		SongsAddedToQueue("Songs added to Queue"),
		// PlayAllAction("Play All Action"),

		TabSelected("Tab Selected"), LanguageSelected("Language Selected"),

		CurrentVideoTitle("Current video title when related selected"),

		ArtistName("Artist Name"),

		Rewards("Rewards"), Leaderboard("Leaderboard"),

		Duration("Duration"),
		// TimeStamp("Time Stamp"),

		OptionSelected("Option selected"),

		ButtonName("Button Name"), LiveRadio("Live Radio"), CelebRadio(
				"Celeb Radio"),

		// SongDetail("Song Detail"),
		// AlbumDetail("Album Detail"),
		// PlaylistDetail("Playlist Detail"),
		VideoDetail("Video Detail"),
		// MusicTile("Music Tile"),
		Fullplayer("Full player"),
		// VideoTile("Video Tile"),
		PlayerQueue("Player Queue"),
		// SimilarFullPlayer("Similar_Full Player"),
		AlbumFullPlayer("Album_Full Player"),
		// DiscoverResult("Discover Result"),
		OfflineMusic("Offline Music"), SearchResult("Search Result"), NameOfGenre(
				"name of genre");

		private final String value;

		private FlurryKeys(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurrySubSectionDescription {

		DiscoveryResults("Discovery Results"), VideoRelatedAudio(
				"Video's related audio"),
		// Playlist("Playlist"),
		MusicNew("Music New"), MusicPopular("Music Popular"), Video("Video"),
		// MusicRecomended("Music Recomended"),
		MyCollection("My Collection"), MyFavorite("My Favorite"),

		LiveStationsRadio("Live Stations Radio"), TopArtistsRadio(
				"Top Artists Radio"),

		FullPlayerSimilarSongs("Full Player's Similar Songs"),

		MyStreamEveryone("My Stream - everyone"), MyStreamFriends(
				"My Stream - friends"), MyStreamMe("My Stream - me"),

		AlbumDetail("Album detail"), PlaylistDetail("Playlist detail"), SongDetail(
				"Song detail"), VideoDetail("Video detail"),
		// RadioDetail("RadioDetail"),

		SearchResults("SearchResults"),

		// VideoNew("Video New"),
		// VideoPopular("Video Popular"),
		// VideoRecomended("Video Recomended"),
		VideoRelated("Video Related"),

		FullPlayer("Full Player"), MiniPlayer("Full Player");

		private final String value;

		private FlurrySubSectionDescription(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurrySourceDescription {

		TapOnPlayInContextualMenu("Tap on play in contextualmenu"), TapOnAddToQueueInContextualMenu(
				"Tap on add to queue in contextualmenu"), TapOnSongTile(
				"Tap on song tile"), TapOnPlayButtonTile(
				"Tap on play button tile"), TapOnPlaySongDetail(
				"Tap on play in song detail view"), TapOnPlayAlbumPlaylistDetail(
				"Tap on play in album/playlist detail view"), TapOnPlaySearchResult(
				"Tap on play in search results listing or any other listing view.");// xtpl
		// TapOnFavouriteButton("Tap on favourite button");

		private String value;

		private FlurrySourceDescription(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryFullPlayerParams {

		// Keys
		Type("Type"), ActionDone("Action done"), Title("title"), FullPlayerPlay(
				"Full Player Play"), FullPlayerMore("Full Player - More"),

		// Keys
		SongName("Song name"), OptionSelected("Option selected"),

		// Type values
		Music("Music"), LiveRadio("LiveRadio"), OnDemandRadio("OnDemandRadio"), DiscoveryMusic(
				"DiscoveryMusic"), Radio("Radio"), VideoPlayer("Video Player"),

		// Action done values
		LoadButtonClicked("Load button clicked"), Drag("Drag"), TextButtonClicked(
				"Text button clicked"), QueueButtonClicked(
				"Queue button clicked"), SleepMode("Sleep Mode"),
		// GymMode("Gym Mode"),
		Share("Share"), Similar("Similar"), Trivia("Trivia"), Lyrics("Lyrics"), Info(
				"Info"), Comment("Comment");

		private String value;

		private FlurryFullPlayerParams(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryDownloadPlansParams {

		// Event name
		BuyFromAppStore("Buy from app store"), BuyFromOperatorBilling(
				"Buy from operator billing"), GetFree("Get Free"),
		// AskedForMobileNumber("Asked for mobile number"),
		// MobileNumberEntered("Mobile number entered"),
		// SMSPasscodeEntered("SMS Passcode entered"),
		DownloadCompleteEvent("Download complete event"),

		// Keys
		Plan("Plan"), TitleOfTheSong("Title of the song");// ,
		// Status("Status"),

		// Plan values
		// Single("Single"),
		// ValuePack("Value pack"),

		// Status values
		// Valid("Valid"),
		// Invalid("Invalid");

		private String value;

		private FlurryDownloadPlansParams(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryDiscoveryParams {

		MoodSelected("Mood Selected"),
		// CategorySelected("Category Selected"),
		SongNamePlayed("Song Name Played"),
		// FromAndToValuesOfEra("From & to values of Era"),
		TempoSelected("Tempo Selected"), EraSelected("Era Selected"), DiscoveryOfTheDaySelected(
				"Discovery of the day selected");

		private String value;

		private FlurryDiscoveryParams(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurrySourceSection {

		PlayerQueue("Player Queue"),
		// Discovery("Discovery"),
		Favorites("Favorites"), Playlists("Playlists"), MyCollection(
				"My Collection"), Profile("Profile"), Search("Search"), NQSearch(
				"NQ Search"), Home("Home");

		private String value;

		private FlurrySourceSection(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryMediaDetailActions {

		// Key
		ActionTaken("Action Taken"),

		// Values
		PlayAll("Play All"), Share("Share"), Favorite("Favorite"), AddToPlaylist(
				"Add To Playlist"), Videos("Videos"),
		// Back("Back"),
		PlayAllTapped("Play all tapped"),
		// PlayNow("Play Now"),
		Addtoqueue("Add to queue"),
		// ViewDetails("View Details"),
		// SaveOffline("Save Offline"),

		// Status values
		yes("yes");// ,
		// no("no");
		private String value;

		private FlurryMediaDetailActions(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	// public enum FlurrySpecials{
	//
	// // Key
	// SpecialName("Special name"),
	// SourceOfEntry("Source of Entry"),
	//
	// // Values
	// GlobalNenu("Global Nenu"),
	// QuickMavigation("Quick Mavigation");
	//
	// private String value;
	//
	// private FlurrySpecials(final String val){
	// value = val;
	// }
	//
	// @Override
	// public String toString() {
	// return value;
	// }
	// }

	public enum FlurryPlaylists {

		// Events names
		AddToPlaylist("Add To Playlist"), PlaylistSaved("Playlist Saved"), CancelledAddToPlayList(
				"Cancelled Add To PlayList"),

		// Keys
		Source("Source"), PlaylistName("Playlist Name"),
		// PlaylistStatus("Playlist Status"),// New/Existing

		// Status Values
		// New("New"),
		// Existing("Existing"),

		// Source values
		Radio("Radio"), FullPlayer("Full Player");
		// PlayerQueue("Player Queue");

		private String value;

		private FlurryPlaylists(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryShare {

		// Events names
		ShareButton("Share Button"), ShareCompleted("Share Completed"),

		// Keys
		SourceSection("Source Section"), Title("Title"), Type("Type"), ShareVia(
				"Share Via"),

		// Source Section values
		Lyrics("Lyrics"), FullPlayer("Full Player"), Video("Video"), Trivia(
				"Trivia"),

		// Share via values
		Facebook("Facebook"), Twitter("Twitter"), Email("Email"), More("More"), SMS(
				"SMS");

		private String value;

		private FlurryShare(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryInvite {

		// Events names
		InviteFriends("Invite Friends"), InviteSent("Invite Sent"),

		// Keys
		Source("Source"), Mode("Mode"), CountOfFriends("Count Of Friends"),

		// Source keys
		GlobalMenu("Global Menu"), MyStreamFriendsWhenEmpty(
				"My Stream - friends when empty"), MyStreamInviteFriends(
				"My Stream - invite friends");

		private String value;

		private FlurryInvite(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryComments {

		// Events names
		Comment("Comment"), CommentPosted("Comment Posted"),

		// Keys
		UserTappedOnComment("User tapped on comment"), SourceSection(
				"Source Section"), Title("Title"), Type("Type"), IsSocialLoggedIn(
				"Is social network logged in"), SocialNetworksSelected(
				"Social Networks Selected"),

		// SourceSection keys
		Video("Video"), FullPlayer("Full Player");

		private String value;

		private FlurryComments(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurrySearch {

		// Events names
		SearchButtonTapped("Search button tapped"), SearchesUsingPopularKeywords(
				"Searches using popular keywords"), SearchesByTypingInBox(
				"Searches by typing in box"),
		// SearchesUsingAutoComplete("Searches using auto complete"),//xtpl
		Filter("Filter"), SearchResultTapped("SearchResultTapped"), Search(
				"Search"),

		// Keys
		SourceSection("SourceSection"),

		// SourceSection keys
		Video("Video"), FullPlayer("Full Player"), ActionBarSearch(
				"Action Bar Search"),

		SearchTerm("Search Term"), NumberOfResults("Number of results"), FilterValue(
				"Filter Value"),

		TitleOfResultTapped("Title of result tapped"), TypeOfResultTaped(
				"Title of result tapped");

		private String value;

		private FlurrySearch(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryNavigation {

		// Events names
		// PlusMenu("Plus Menu"),
		// GlobalMenu("Global Menu"),
		NavigationDrawer("Navigation Drawer"), SwipableTabs("Swipable Tabs"),

		// Keys
		// StartSection("Section"),
		// EndSection("End Section"),
		MenuOptionselected("Menu Option selected"), NameOfTheSection(
				"Name of the section"),

		// values
		// SongID("Song ID"),
		// Music("Music"),
		MusicNew("Music New"), MusicPopular("Music Popular"), Radio("Radio"), Video(
				"Videos"), Discover("Discover");

		private String value;

		private FlurryNavigation(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryUserStatus {

		// Events names
		Status("Status"), SeesLoginScreen("Sees Login Screen"), HungamaSignUp(
				"Hungama Sign up"), HungamaLogin("Hungama log ip"), SocialLogin(
				"Social Login"), SkipsLoginPage("Skips Login Page"),

		// Keys
		// StartSession("Start Session"),
		RegistrationStatus("Registration Status"), PaidStatus("Paid Status"), Source(
				"Source"), TypeOfLogin("TypeOfLogin"),

		// Values
		MyProfile("My Profile"), Download("Download"), Settings("Settings"), AppLaunch(
				"App Launch"), Upgrade("Upgrade"), UserEntersInfo(
				"User Enters Info"), GlobleMenu("Globle Menu"), RedeemCoupon(
				"Redeem Coupon"),

		// Facebbok("Facebbok"),
		// Twitter("Twitter"),
		// Google("Google"),
		NewRegistration("New Registration"), Login("Login");

		private String value;

		private FlurryUserStatus(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurrySubscription {

		// Events names
		SubscriptionMessgaeServed("Subscription Messgae Served"), TapsOnUpgrade(
				"Taps On Upgrade"), PlansScreen("Plans screen"), PlansSelected(
				"Plans selected"),
		// MobileVerification("MobileVerification"),
		PaymentSuccessful("Payment Successful"), PaymentFail("Payment Fail"), TapsOnHDAudioQuality(
				"Tapped on HD audio quality in Settings"),

		// Keys
		// Tapped("Section"),
		SourcePage("Source Page"),
		// SourceTrigger("Source Trigger"),
		PaymentMode("Payment Mode"), PlanName("Plan Name"), // xtpl
		MobileVerified("Mobile Verified"),

		// Values
		Video("Video Portrait"), Membership("Membership"), LoggedIn("Logged In"),
		LeftMenu("Left Menu");

		private String value;

		private FlurrySubscription(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryAllPlayer {

		// Events names
		SongPlayed("Song Played"), VideoPlayed("Video Played"), PlayerQueueViewed(
				"Player Queue Viewed"),

		// Keys
		// Source("Source"),
		NextFromMiniPlayer("Next From Mini Player"), NextFromFullPlayer(
				"Next From Full Player"), PrevFromFullPlayer(
				"Prev From Full Player"), NextPrevFromFullPlayerUsingSwipe(
				"Next/Prev From Full Player Using Swipe"), OnLoop("On Loop"), Shuffle(
				"Shuffle"), GymModeUsed("Gym Mode Used"), SleepModeUsed(
				"Sleep Mode Used"), TimeOfDay("Time Of Day"), Duration(
				"Duration"),

		// None("None"),
		ClearQueue("Clear Queue"), DeleteSong("Delete Song"), ChangeNowPlaying(
				"Change Now-Playing"), SongByTap("Song By Tap"),
		// ReorderSong("Reorder Song"),
		// LoadPlaylist("Load Playlist"),
		// LoadFavotrites("Load Favorites"),
		Back("Back");

		private String value;

		private FlurryAllPlayer(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryCaching {

		// Events names
		TappedonSaveOffline("Tapped on Save Offline"),
		// TappedonSaveAllOffline("Tapped on Save All Offline"),
		GoOffline("Go Offline"), GoOnline("Go Online"),

		// Keys
		UserStatus("User status"), Source("Source"), ContentType("Content type"), Title_contentID(
				"Title_contentID"),

		// Values
		// Header("header"),
		LeftMenuToggleButton("left menu toggle button"), NoInternetPrompt(
				"no internet prompt"), Prompt("prompt"), Settings("settings"), Free(
				"Free"), Trial("Trial"), Trial_expired("Trial_expired"), Paid(
				"Paid"), Song("Song"), Album("Album"), Playlist("Playlist"),
		// Music("Music"),
		AlbumDetails("Album details"), SongDetails("Song details"), FullPlayer(
				"Full player"), PlayerQueue("Player queue"), Video("Video"),
		// MiniPlayer("Mini-player"),
		SaveFavorites("Save favorites"), MyFavorites("My favorites"), MyPlaylist(
				"My playlist"), LongPressMenuSong("Long press menu � song"), LongPressMenuAlbum(
				"Long press menu � album"), LongPressMenuPlaylist(
				"Long press menu � playlist"), LongPressMenuVideo(
				"Long press menu � video"), LongPressMenuPlayerQueue(
				"Long press menu � player queue");

		private String value;

		private FlurryCaching(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryLanguage {
		// Events names
		DisplayLanguageSelected("Display Language Selected"),

		// Keys
		LanguageSelected("Language selected");

		private String value;

		private FlurryLanguage(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurrySongID {
		// Events names
		SongID("Song ID"),

		// Keys
		SongsSearched("Songs searched"), ClickedOn("Clicked on"),

		// values
		History("History"), SaveOffline("Save Offline"), Download("Download"), Play(
				"Play"), Fav("Fav"), Share("Share");

		private String value;

		private FlurrySongID(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum FlurryBrowseBy {
		// Events names
		BrowseByMusic("Browse By - Music"),
		// BrowseByVideo("Browse By - Video"),

		// Keys
		LanguageCategorySelected("Language/Category selected");

		private String value;

		private FlurryBrowseBy(final String val) {
			value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}
}
