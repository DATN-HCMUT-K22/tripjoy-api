package com.tripjoy.api.service;

import java.util.UUID;

import com.tripjoy.api.dto.response.TravelNotebookResponse;

public interface ITravelNotebookService {

    /**
     * Gọi AI Service để sinh Travel Notebook cho một lịch trình.
     * Nếu notebook đã tồn tại thì cập nhật lại bằng nội dung AI mới.
     *
     * @param itineraryId UUID của lịch trình
     * @return TravelNotebookResponse chứa nội dung food/climate/culture từ AI
     */
    TravelNotebookResponse generateByItinerary(UUID itineraryId);

    /**
     * Lấy notebook hiện có theo itinerary ID (không gọi AI).
     */
    TravelNotebookResponse getByItineraryId(UUID itineraryId);

    /**
     * Lấy notebook theo ID trực tiếp.
     */
    TravelNotebookResponse getById(UUID notebookId);
}

