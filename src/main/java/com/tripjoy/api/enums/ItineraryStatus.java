package com.tripjoy.api.enums;

public enum ItineraryStatus {
    GENERATING, // Đang được AI tạo
    FAILED, // AI tạo thất bại
    DRAFT, // Bản nháp (AI tạo xong sẽ về trạng thái này, hoặc user tự tạo)
    CONFIRMED, // Đã chốt kế hoạch
    IN_PROGRESS, // Đang đi
    COMPLETED // Đã hoàn thành chuyến đi
}
