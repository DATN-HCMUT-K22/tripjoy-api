package com.tripjoy.api.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;
import com.tripjoy.api.entity.ChatMessage;
import com.tripjoy.api.entity.Comment;
import com.tripjoy.api.entity.Feedback;
import com.tripjoy.api.entity.HandleReportContent;
import com.tripjoy.api.entity.ModerationAction;
import com.tripjoy.api.entity.Post;
import com.tripjoy.api.entity.ReportContent;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ReportContentType;
import com.tripjoy.api.enums.ReportStatus;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.UserMapper;
import com.tripjoy.api.repository.ChatMessageRepository;
import com.tripjoy.api.repository.CommentRepository;
import com.tripjoy.api.repository.ConversationMemberRepository;
import com.tripjoy.api.repository.FeedbackRepository;
import com.tripjoy.api.repository.HandleReportContentRepository;
import com.tripjoy.api.repository.ModerationActionRepository;
import com.tripjoy.api.repository.PostRepository;
import com.tripjoy.api.repository.ReportContentRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IReportService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService implements IReportService {

    private static final String FEEDBACK_TYPE_REPORT = "REPORT_FEEDBACK";
    private static final String FEEDBACK_STATUS_SENT = "SENT";

    ReportContentRepository reportContentRepository;
    HandleReportContentRepository handleReportContentRepository;
    ModerationActionRepository moderationActionRepository;
    FeedbackRepository feedbackRepository;
    UserRepository userRepository;
    PostRepository postRepository;
    CommentRepository commentRepository;
    ChatMessageRepository chatMessageRepository;
    ConversationMemberRepository conversationMemberRepository;
    UserMapper userMapper;

    @Override
    @Transactional
    public ReportResponse submitReport(ReportRequest request) {
        User reporter = getCurrentUser();
        ReportContentType contentType = parseContentType(request.getContentType());
        ReportTargetSnapshot snapshot = resolveTarget(contentType, request.getContentId(), reporter.getId());

        ReportContent report = ReportContent.builder()
                .contentType(contentType.name())
                .targetId(request.getContentId())
                .reportType(normalize(request.getReportType()))
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null)
                .text(snapshot.text())
                .mediaUrl(snapshot.mediaUrl())
                .status(ReportStatus.PENDING.name())
                .reporter(reporter)
                .reportedUser(snapshot.owner())
                .build();

        return toReportResponse(reportContentRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportContentRepository.findAll(pageable).map(this::toReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(UUID reportId) {
        return toReportResponse(findReport(reportId));
    }

    @Override
    @Transactional
    public HandleReportResponse handleReport(UUID reportId, HandleReportRequest request) {
        ReportContent report = findReport(reportId);
        User admin = getCurrentUser();
        ReportStatus status = parseHandledStatus(request.getStatus());

        ModerationAction moderationAction = null;
        if (request.getModerationAction() != null) {
            moderationAction = createModerationAction(request.getModerationAction(), report, admin);
        }

        report.setStatus(status.name());
        reportContentRepository.save(report);

        HandleReportContent handleRecord = HandleReportContent.builder()
                .reportType(report.getReportType())
                .status(status.name())
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null)
                .reportContent(report)
                .ba(admin)
                .moderationAction(moderationAction)
                .build();
        handleRecord = handleReportContentRepository.save(handleRecord);

        if (StringUtils.hasText(request.getFeedbackContent())) {
            createReportFeedback(report, admin, request.getFeedbackContent().trim());
        }

        return toHandleReportResponse(handleRecord);
    }

    private ModerationAction createModerationAction(
            ModerationActionRequest request, ReportContent report, User admin) {
        if (!request.getUserId().equals(report.getReportedUser().getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Moderation action user must be the reported content owner");
        }

        User target = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ModerationAction action = ModerationAction.builder()
                .actionType(normalize(request.getActionType()))
                .note(request.getNote())
                .user(target)
                .ba(admin)
                .reportContent(report)
                .build();

        return moderationActionRepository.save(action);
    }

    private void createReportFeedback(ReportContent report, User admin, String content) {
        Feedback feedback = Feedback.builder()
                .type(FEEDBACK_TYPE_REPORT)
                .title("Report processed")
                .content(content)
                .status(FEEDBACK_STATUS_SENT)
                .sender(admin)
                .receiver(report.getReporter())
                .reportContent(report)
                .build();
        feedbackRepository.save(feedback);
    }

    private ReportTargetSnapshot resolveTarget(ReportContentType contentType, UUID targetId, UUID reporterId) {
        return switch (contentType) {
            case POST -> resolvePost(targetId);
            case COMMENT -> resolveComment(targetId);
            case MESSAGE -> resolveMessage(targetId, reporterId);
        };
    }

    private ReportTargetSnapshot resolvePost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (post.getSoftDeleteInfo() != null && post.getSoftDeleteInfo().isDeleted()) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
        String mediaUrl = post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()
                ? post.getMediaUrls().getFirst()
                : null;
        return new ReportTargetSnapshot(post.getContent(), mediaUrl, post.getCreator());
    }

    private ReportTargetSnapshot resolveComment(UUID commentId) {
        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return new ReportTargetSnapshot(comment.getContent(), null, comment.getUser());
    }

    private ReportTargetSnapshot resolveMessage(UUID messageId, UUID reporterId) {
        ChatMessage message = chatMessageRepository
                .findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        boolean canAccessMessage = conversationMemberRepository.existsByConversationIdAndUserId(
                message.getConversation().getId(), reporterId);
        if (!canAccessMessage) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }
        return new ReportTargetSnapshot(message.getMessageContent(), message.getMediaUrl(), message.getSender());
    }

    private ReportContent findReport(UUID reportId) {
        return reportContentRepository
                .findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User getCurrentUser() {
        return userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private ReportContentType parseContentType(String value) {
        try {
            return ReportContentType.valueOf(normalize(value));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "content_type must be one of POST, COMMENT, MESSAGE");
        }
    }

    private ReportStatus parseHandledStatus(String value) {
        try {
            ReportStatus status = ReportStatus.valueOf(normalize(value));
            if (status == ReportStatus.PENDING) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Handled report status cannot be PENDING");
            }
            return status;
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "status must be PROCESSED or DISMISSED");
        }
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }

    private ReportResponse toReportResponse(ReportContent report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reason(report.getReportType())
                .status(report.getStatus())
                .description(report.getDescription())
                .reportedBy(report.getReporter() != null ? report.getReporter().getId() : null)
                .reporter(report.getReporter() != null ? userMapper.toUserSimpleResponse(report.getReporter()) : null)
                .reportedUser(
                        report.getReportedUser() != null ? userMapper.toUserSimpleResponse(report.getReportedUser()) : null)
                .reportedEntityId(report.getTargetId())
                .reportedEntityType(report.getContentType())
                .reportedContentText(report.getText())
                .reportedMediaUrl(report.getMediaUrl())
                .createdAt(report.getCreatedAt())
                .createdBy(report.getCreatedBy())
                .updatedAt(report.getUpdatedAt())
                .updatedBy(report.getUpdatedBy())
                .build();
    }

    private HandleReportResponse toHandleReportResponse(HandleReportContent handleRecord) {
        ModerationAction action = handleRecord.getModerationAction();
        return HandleReportResponse.builder()
                .id(handleRecord.getId())
                .reportContentId(handleRecord.getReportContent().getId().toString())
                .handledBy(userMapper.toUserSimpleResponse(handleRecord.getBa()))
                .handledAt(handleRecord.getCreatedAt())
                .status(handleRecord.getStatus())
                .description(handleRecord.getDescription())
                .moderationAction(action != null ? toModerationActionResponse(action) : null)
                .build();
    }

    private ModerationActionResponse toModerationActionResponse(ModerationAction action) {
        return ModerationActionResponse.builder()
                .id(action.getId())
                .moderatedUser(userMapper.toUserSimpleResponse(action.getUser()))
                .admin(userMapper.toUserSimpleResponse(action.getBa()))
                .actionType(action.getActionType())
                .createdAt(action.getCreatedAt())
                .note(action.getNote())
                .build();
    }

    private record ReportTargetSnapshot(String text, String mediaUrl, User owner) {}
}
