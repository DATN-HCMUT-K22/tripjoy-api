package com.tripjoy.api.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tripjoy.api.dto.request.feedback.FeedbackRequest;
import com.tripjoy.api.dto.request.feedback.FeedbackResponseRequest;
import com.tripjoy.api.dto.response.feedback.FeedbackResponse;
import com.tripjoy.api.dto.response.feedback.FeedbackResponseResponse;
import com.tripjoy.api.entity.Feedback;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.UserMapper;
import com.tripjoy.api.repository.FeedbackRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IFeedbackService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackService implements IFeedbackService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_REPLIED = "REPLIED";
    private static final String TYPE_ADMIN_RESPONSE = "ADMIN_RESPONSE";

    FeedbackRepository feedbackRepository;
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request) {
        User sender = getCurrentUser();

        Feedback feedback = Feedback.builder()
                .type(normalize(request.getType()))
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .status(STATUS_OPEN)
                .sender(sender)
                .build();

        return toFeedbackResponse(feedbackRepository.save(feedback));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getAllFeedback(Pageable pageable) {
        return feedbackRepository.findAll(pageable).map(this::toFeedbackResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(UUID feedbackId) {
        return toFeedbackResponse(findFeedback(feedbackId));
    }

    @Override
    @Transactional
    public FeedbackResponseResponse respondToFeedback(UUID feedbackId, FeedbackResponseRequest request) {
        Feedback original = findFeedback(feedbackId);
        User admin = getCurrentUser();

        String newStatus = StringUtils.hasText(request.getStatus())
                ? normalize(request.getStatus())
                : STATUS_REPLIED;

        Feedback response = Feedback.builder()
                .type(TYPE_ADMIN_RESPONSE)
                .title("RE: " + original.getTitle())
                .content(request.getDescription().trim())
                .status(newStatus)
                .sender(admin)
                .receiver(original.getSender())
                .parentFeedback(original)
                .reportContent(original.getReportContent())
                .build();

        original.setStatus(newStatus);
        feedbackRepository.save(original);

        return toFeedbackResponseResponse(feedbackRepository.save(response), newStatus);
    }

    private Feedback findFeedback(UUID feedbackId) {
        return feedbackRepository.findById(feedbackId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User getCurrentUser() {
        return userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .type(feedback.getType())
                .status(feedback.getStatus())
                .userId(feedback.getSender() != null ? feedback.getSender().getId() : null)
                .receiverId(feedback.getReceiver() != null ? feedback.getReceiver().getId() : null)
                .parentFeedbackId(feedback.getParentFeedback() != null ? feedback.getParentFeedback().getId() : null)
                .reportContentId(feedback.getReportContent() != null ? feedback.getReportContent().getId() : null)
                .createdAt(feedback.getCreatedAt())
                .createdBy(feedback.getCreatedBy())
                .updatedAt(feedback.getUpdatedAt())
                .updatedBy(feedback.getUpdatedBy())
                .build();
    }

    private FeedbackResponseResponse toFeedbackResponseResponse(Feedback response, String status) {
        return FeedbackResponseResponse.builder()
                .id(response.getId())
                .feedbackId(response.getParentFeedback() != null
                        ? response.getParentFeedback().getId().toString()
                        : null)
                .respondedBy(userMapper.toUserSimpleResponse(response.getSender()))
                .responseFor(response.getReceiver() != null ? userMapper.toUserSimpleResponse(response.getReceiver()) : null)
                .createdAt(response.getCreatedAt())
                .description(response.getContent())
                .status(status)
                .build();
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }
}
