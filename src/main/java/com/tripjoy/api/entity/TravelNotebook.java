package com.tripjoy.api.entity;

import jakarta.persistence.*;

import lombok.*;

/**
 * Travel Notebook entity — lưu nội dung do AI sinh ra về điểm đến.
 * Khớp với Python model {@code TravelNotebook}:
 *   name, food, climate, culture, emergency_contacts
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelNotebook extends BaseEntity {

    private String name;

    /** Ẩm thực địa phương — do AI sinh ra từ Wikipedia + Gemini */
    @Column(columnDefinition = "TEXT")
    private String food;

    /** Khí hậu & lời khuyên trang phục — do AI sinh ra */
    @Column(columnDefinition = "TEXT")
    private String climate;

    /** Văn hóa, phong tục tập quán địa phương — do AI sinh ra */
    @Column(columnDefinition = "TEXT")
    private String culture;

    /** Số điện thoại khẩn cấp, bệnh viện, đại sứ quán — do AI sinh ra */
    @Column(name = "emergency_contacts", columnDefinition = "TEXT")
    private String emergencyContacts;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;
}
