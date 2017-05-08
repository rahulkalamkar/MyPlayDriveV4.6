package com.gigya.socialize.android.event;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSPluginFragment;

/**
 * A listener interface for receiving Gigya plugin events.
 */
public interface GSPluginListener {
    /**
     * Invoked after a plugin fragment has finished loading the plugin.
     *
     * @param pluginFragment The plugin fragment that has finished loading.
     * @param event          A GSObject containing details of the event, with fields as described in the relevant plugin's documentation.
     */
    public void onLoad(GSPluginFragment pluginFragment, GSObject event);

    /**
     * Invoked after encountering an error, either generated by the plugin or while loading.
     *
     * @param pluginFragment The plugin fragment that has encountered the error.
     * @param error          An error object specifying the specific error.
     */
    public void onError(GSPluginFragment pluginFragment, GSObject error);

    /**
     * Invoked when the plugin fires a <a target="_blank" href="http://developers.gigya.com/display/GD/Events">custom event</a>.
     * (For example - commentsUI's <a target="_blank" href="http://developers.gigya.com/display/GD/comments.showCommentsUI%20JS#comments.showCommentsUIJS-onCommentSubmittedEventData">commentSubmitted</a>).
     *
     * @param pluginFragment The plugin fragment that has fired the event.
     * @param event          The event object.
     */
    public void onEvent(GSPluginFragment pluginFragment, GSObject event);
}
