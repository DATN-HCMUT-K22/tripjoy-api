package com.tripjoy.api.enums;

public enum NotificationType {
    // POST INTERACTIONS
    POST_LIKED, // Someone liked your post
    POST_COMMENTED, // Someone commented on your post
    POST_SHARED, // Someone shared your post
    POST_SAVED, // Someone saved your post

    // COMMENT INTERACTIONS
    COMMENT_LIKED, // Someone liked your comment
    COMMENT_REPLIED, // Someone replied to your comment
    COMMENT_MENTIONED, // You were mentioned in a comment

    // GROUP ACTIVITIES
    GROUP_INVITE, // You were invited to a group
    GROUP_MEMBER_JOINED, // Someone joined your group
    GROUP_MEMBER_LEFT, // Someone left your group
    GROUP_ROLE_CHANGED, // Your role in a group changed
    GROUP_UPDATED, // Group information was updated
    GROUP_LEADERSHIP_TRANSFERRED, // Leadership was transferred to a new member
    GROUP_ITINERARY_CREATED, // New itinerary created in your group

    // CHAT INTERACTIONS
    CHAT_MESSAGE, // New chat message (for mentions or important DMs)
    CHAT_MESSAGE_LIKED, // Someone liked your message
    CHAT_MENTIONED, // You were mentioned in a chat

    // ITINERARY INTERACTIONS
    ITINERARY_SHARED, // Itinerary was shared with you
    ITINERARY_LIKED, // Someone liked your itinerary
    ITINERARY_UPDATED, // Itinerary you're following was updated

    // SYSTEM NOTIFICATIONS
    SYSTEM_ANNOUNCEMENT, // System-wide announcement
    ACCOUNT_VERIFICATION, // Account verification status
    CREDITS_AWARDED // Credits awarded to user
}
