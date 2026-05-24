package com.tripjoy.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.feedback.FeedbackRequest;
import com.tripjoy.api.dto.request.feedback.FeedbackResponseRequest;
import com.tripjoy.api.dto.response.feedback.FeedbackResponse;
import com.tripjoy.api.dto.response.feedback.FeedbackResponseResponse;

public interface IFeedbackService {
    FeedbackResponse submitFeedback(FeedbackRequest request);

    Page<FeedbackResponse> getAllFeedback(Pageable pageable);

    FeedbackResponse getFeedbackById(UUID feedbackId);

    FeedbackResponseResponse respondToFeedback(UUID feedbackId, FeedbackResponseRequest request);
}
