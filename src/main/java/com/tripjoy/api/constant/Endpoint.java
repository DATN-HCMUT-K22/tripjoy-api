package com.tripjoy.api.constant;

public class Endpoint {
    public static final String API_PREFIX = "/api/v1";

    public static final class Auth {
        public static final String BASE = API_PREFIX + "/auth";
        public static final String LOGIN = "/login";
        public static final String REGISTER = "/register";
        //        public static final String EMAIL_VERIFICATION_TOKEN = "/email-verification/{token}";
        public static final String INTROSPECT = "/introspect";
        public static final String REFRESH = "/refresh";
        public static final String RESET_PASSWORD = "/reset-password";
        //        public static final String RESET_PASSWORD_OTP = "/reset-password/{otp}";
        //        public static final String RESET_PASSWORD_EMAIL = "/reset-password/{email}";
        public static final String LOGOUT = "/logout";
    }

    public static final class User {
        public static final String BASE = API_PREFIX + "/users";
        public static final String ME = "/me";
        public static final String ID = "/{userId}";
        public static final String SEARCH = "/search";
        //        public static final String PASSWORD = "/password";
        //        public static final String RESEND_EMAIL_VERIFICATION = "/resend-email-verification";
    }

    public static final class Role {
        public static final String BASE = API_PREFIX + "/roles";
        public static final String ID = "/{roleId}";
    }

    public static final class Permission {
        public static final String BASE = API_PREFIX + "/permissions";
        public static final String ID = "/{permissionId}";
    }

    public static final class Location {
        public static final String BASE = API_PREFIX + "/locations";
        public static final String ID = "/{locationId}";
    }

    public static final class Group {
        public static final String BASE = API_PREFIX + "/groups";
        public static final String ID = "/{groupId}";
        public static final String SEARCH = "/search";

        // Quản lý thành viên trong group
        public static final String MEMBERS_BASE = ID + "/members";
        public static final String MEMBERS_ID = MEMBERS_BASE + "/{memberId}";
        public static final String MEMBERS_ME = MEMBERS_BASE + "/me";

        // Đề xuất địa điểm trong group
        public static final String LOCATION_SUGGESTIONS = ID + "/location-suggestions";
        public static final String LOCATION_SUGGESTIONS_ID = LOCATION_SUGGESTIONS + "/{suggestionId}";
    }

    //    public static final class Chat {
    //        public static final String BASE = API_PREFIX + "/chat"; // -> /api/v1/chat
    //
    //        // --- Endpoints cho Tin nhắn (Message) ---
    //        public static final String MESSAGES = "/messages";
    //        public static final String MESSAGE_ID = MESSAGES + "/{messageId}"; // -> /api/v1/chat/messages/{messageId}
    //        public static final String MESSAGE_LIKES = MESSAGE_ID + "/likes"; // -> .../{messageId}/likes
    //
    //        // --- Endpoints cho Chat 1-1 (Direct) ---
    //        public static final String DIRECT_BASE = "/direct/{userId}"; // -> /api/v1/chat/direct/{userId}
    //        public static final String DIRECT_MESSAGES = DIRECT_BASE + "/messages"; // -> .../direct/{userId}/messages
    //
    //        // --- Endpoints cho Chat Nhóm (Groups) ---
    //        public static final String GROUP_BASE = "/groups/{groupId}"; // -> /api/v1/chat/groups/{groupId}
    //        public static final String GROUP_MESSAGES = GROUP_BASE + "/messages"; // -> .../groups/{groupId}/messages
    //    }

    public static final class Conversation {
        public static final String BASE = API_PREFIX + "/conversations";

        // GET: Lấy danh sách Inbox
        // POST: Tạo cuộc hội thoại mới

        public static final String ID = "/{conversationId}"; // GET: Lấy chi tiết hội thoại

        // Quản lý tin nhắn trong hội thoại
        public static final String MESSAGES = ID + "/messages";

        // Quản lý thành viên (Thêm, Xóa, Rời nhóm, Mute)
        public static final String MEMBERS = ID + "/members";

        // Get all pinned messages
        public static final String PINNED_MESSAGES = ID + "/pinned-messages";

        // Search messages in conversation
        public static final String SEARCH_MESSAGES = ID + "/messages/search";
    }

    // Class này để xử lý hành động trên 1 tin nhắn cụ thể (Global ID)
    public static final class Message {
        public static final String BASE = API_PREFIX + "/messages";
        public static final String ID = "/{messageId}";

        public static final String LIKES = ID + "/likes";
        public static final String PIN = ID + "/pin";

        // Global search across all user's conversations
        public static final String SEARCH = "/search";
    }

    public static final class Itinerary {
        public static final String BASE = API_PREFIX + "/itineraries";
        public static final String ID = "/{itineraryId}";

        // Quản lý các điểm đến (trip item) trong 1 lịch trình
        public static final String ITEMS_BASE = ID + "/items";
        public static final String ITEMS_ID = ITEMS_BASE + "/{tripItemId}";

        // Quản lý chi phí (expense) trong 1 lịch trình
        public static final String EXPENSES_BASE = ID + "/expenses";
        public static final String EXPENSES_ID = EXPENSES_BASE + "/{expenseId}";

        // Thêm/bỏ yêu thích
        public static final String FAVORITES = ID + "/favorites";

        public static final String NOTEBOOKS = ID + "/notebooks";
    }

    public static final class TravelNotebook {
        public static final String BASE = API_PREFIX + "/notebooks";
        public static final String ID = "/{notebookId}";
    }

    public static final class Post {
        public static final String BASE = API_PREFIX + "/posts";
        public static final String SEARCH = "/search"; // Global feature-rich search
        public static final String ID = "/{postId}";
        public static final String LIKES = ID + "/likes";
        public static final String SAVES = ID + "/saves";
        public static final String MY_SAVES = "/my-saves";
        public static final String COMMENTS = ID + "/comments";
    }

    public static final class Comment {
        public static final String BASE = API_PREFIX + "/comments";
        public static final String ID = "/{commentId}";

        // Like/unlike comment
        public static final String LIKES = ID + "/likes";
        public static final String REPLIES = ID + "/replies";
    }

    public static final class Feedback {
        public static final String BASE = API_PREFIX + "/feedbacks";
        public static final String ID = "/{feedbackId}";
    }

    public static final class Report {
        public static final String BASE = API_PREFIX + "/reports";
        public static final String ID = "/{reportId}";
    }

    public static final class Notification {
        public static final String BASE = API_PREFIX + "/notifications";
        public static final String ID = "/{notificationId}";
        public static final String UNREAD_COUNT = "/unread-count";
        public static final String MARK_READ = ID + "/read";
        public static final String MARK_ALL_READ = "/mark-all-read";
        public static final String ARCHIVE = ID + "/archive";
    }

    public static final class Admin {
        public static final String BASE = API_PREFIX + "/admin";
    }

    public static final class Media {
        public static final String BASE = API_PREFIX + "/media";
        public static final String UPLOAD_IMAGE = "/upload/image";
        public static final String UPLOAD_VIDEO = "/upload/video";
        public static final String SIGN = "/sign";
        public static final String DELETE = "/delete";
    }
}
