package com.tripjoy.api.constant;

public class Endpoint {
    public static final String API_PREFIX = "/api/v1";


    public static final class Auth{
        public static final String BASE = API_PREFIX + "/auth";
        public static final String LOGIN = "/login";
        public static final String REGISTER = "/register";
//        public static final String EMAIL_VERIFICATION_TOKEN = "/email-verification/{token}";
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

        // Quản lý thành viên trong group
        public static final String MEMBERS_BASE = ID + "/members";
        public static final String MEMBERS_ID = MEMBERS_BASE + "/{memberId}";

        // Chat trong group
        public static final String MESSAGES_BASE = ID + "/messages";
    }

    public static final class Chat {
        public static final String BASE = API_PREFIX + "/chat";
        // Lấy tin nhắn trực tiếp với 1 user khác
        public static final String DIRECT_MESSAGES = "/direct/{userId}";
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
        public static final String FAVORITE = ID + "/favorite";
    }

    public static final class Post {
        public static final String BASE = API_PREFIX + "/posts";
        public static final String ID = "/{postId}";
        public static final String LIKE = ID + "/like";
        public static final String SAVE = ID + "/save";
        public static final String COMMENTS = ID + "/comments";
    }

    public static final class Comment {
        public static final String BASE = API_PREFIX + "/comments";
        public static final String ID = "/{commentId}";

        // Like/unlike comment
        public static final String LIKE = ID + "/like";
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
}
