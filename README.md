# tripjoy-api

```
tripjoy-api
├─ .mvn
│  └─ wrapper
│     └─ maven-wrapper.properties
├─ docker
│  ├─ postgis
│  │  └─ init-postgis.sh
│  └─ redis
│     └─ redis.conf
├─ docker-compose.yml
├─ Dockerfile
├─ docs
│  ├─ LOCATION_MAP_API_GUIDE.md
│  ├─ NOTIFICATION_SYSTEM_GUIDE.md
│  ├─ SOCKET_IO_README_VI.md
│  └─ TECHNICAL_REPORT_SOCKET_IO.md
├─ mvnw
├─ mvnw.cmd
├─ pom.xml
├─ README.md
├─ scripts
│  ├─ docker-setup.sh
│  └─ swaggerhub-upload.sh
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ tripjoy
│  │  │        └─ api
│  │  │           ├─ configuration
│  │  │           │  ├─ AsyncConfig.java
│  │  │           │  ├─ mapper
│  │  │           │  │  └─ BaseMapperConfig.java
│  │  │           │  ├─ redis
│  │  │           │  │  ├─ RedisCacheConfig.java
│  │  │           │  │  └─ RedissonConfig.java
│  │  │           │  ├─ security
│  │  │           │  │  ├─ ApplicationInitConfig.java
│  │  │           │  │  ├─ CustomJwtDecoder.java
│  │  │           │  │  ├─ JwtAuthenticationEntryPoint.java
│  │  │           │  │  ├─ JwtUtils.java
│  │  │           │  │  ├─ SecurityConfig.java
│  │  │           │  │  └─ UserDetailsCustom.java
│  │  │           │  ├─ socketio
│  │  │           │  │  ├─ SocketExceptionHandler.java
│  │  │           │  │  ├─ SocketIOConfig.java
│  │  │           │  │  ├─ SocketIOListenerInitializer.java
│  │  │           │  │  ├─ SocketIOServerLifecycle.java
│  │  │           │  │  └─ SocketRateLimiter.java
│  │  │           │  └─ swagger
│  │  │           │     ├─ OpenApiProperties.java
│  │  │           │     └─ OpenAPISwaggerConfig.java
│  │  │           ├─ constant
│  │  │           │  ├─ Endpoint.java
│  │  │           │  ├─ MessageKey.java
│  │  │           │  └─ PredefinedRole.java
│  │  │           ├─ controller
│  │  │           │  ├─ AdminController.java
│  │  │           │  ├─ AuthenticationController.java
│  │  │           │  ├─ ChatMessageController.java
│  │  │           │  ├─ CommentController.java
│  │  │           │  ├─ ConversationController.java
│  │  │           │  ├─ FeedbackController.java
│  │  │           │  ├─ GroupController.java
│  │  │           │  ├─ ItineraryController.java
│  │  │           │  ├─ LocationController.java
│  │  │           │  ├─ NotificationController.java
│  │  │           │  ├─ PermissionController.java
│  │  │           │  ├─ PostController.java
│  │  │           │  ├─ ReportController.java
│  │  │           │  ├─ RoleController.java
│  │  │           │  ├─ SuggestLocationController.java
│  │  │           │  ├─ TravelNotebookController.java
│  │  │           │  └─ UserController.java
│  │  │           ├─ converter
│  │  │           │  └─ StringListConverter.java
│  │  │           ├─ dto
│  │  │           │  ├─ event
│  │  │           │  │  ├─ GroupCreatedEvent.java
│  │  │           │  │  ├─ MemberJoinedGroupEvent.java
│  │  │           │  │  ├─ MemberRemovedFromGroupEvent.java
│  │  │           │  │  ├─ MessageSentEvent.java
│  │  │           │  │  └─ NotificationEvent.java
│  │  │           │  ├─ request
│  │  │           │  │  ├─ auth
│  │  │           │  │  │  ├─ AuthenticationRequest.java
│  │  │           │  │  │  ├─ IntrospectRequest.java
│  │  │           │  │  │  ├─ LogoutRequest.java
│  │  │           │  │  │  └─ RefreshRequest.java
│  │  │           │  │  ├─ chat
│  │  │           │  │  │  ├─ ChatMessageRequest.java
│  │  │           │  │  │  ├─ ConversationUpdateRequest.java
│  │  │           │  │  │  └─ DirectConversationCreationRequest.java
│  │  │           │  │  ├─ CommentRequest.java
│  │  │           │  │  ├─ ExpenseRequest.java
│  │  │           │  │  ├─ feedback
│  │  │           │  │  │  ├─ FeedbackRequest.java
│  │  │           │  │  │  └─ FeedbackResponseRequest.java
│  │  │           │  │  ├─ GroupRequest.java
│  │  │           │  │  ├─ ItineraryRequest.java
│  │  │           │  │  ├─ LocationCreateRequest.java
│  │  │           │  │  ├─ member
│  │  │           │  │  │  ├─ AddMemberRequest.java
│  │  │           │  │  │  ├─ TransferLeadershipRequest.java
│  │  │           │  │  │  └─ UpdateMemberRoleRequest.java
│  │  │           │  │  ├─ PermissionRequest.java
│  │  │           │  │  ├─ PostRequest.java
│  │  │           │  │  ├─ report
│  │  │           │  │  │  ├─ HandleReportRequest.java
│  │  │           │  │  │  ├─ ModerationActionRequest.java
│  │  │           │  │  │  └─ ReportRequest.java
│  │  │           │  │  ├─ RoleRequest.java
│  │  │           │  │  ├─ SocketMessageRequest.java
│  │  │           │  │  ├─ SuggestLocationRequest.java
│  │  │           │  │  ├─ TravelNotebookRequest.java
│  │  │           │  │  ├─ TripItemRequest.java
│  │  │           │  │  ├─ UserCreationRequest.java
│  │  │           │  │  └─ UserUpdateRequest.java
│  │  │           │  └─ response
│  │  │           │     ├─ ApiResponse.java
│  │  │           │     ├─ auth
│  │  │           │     │  ├─ AuthenticationResponse.java
│  │  │           │     │  └─ IntrospectResponse.java
│  │  │           │     ├─ BaseResponse.java
│  │  │           │     ├─ ChatMessageResponse.java
│  │  │           │     ├─ CommentResponse.java
│  │  │           │     ├─ ConversationResponse.java
│  │  │           │     ├─ ExpenseResponse.java
│  │  │           │     ├─ feedback
│  │  │           │     │  ├─ FeedbackResponse.java
│  │  │           │     │  └─ FeedbackResponseResponse.java
│  │  │           │     ├─ GroupMemberResponse.java
│  │  │           │     ├─ GroupResponse.java
│  │  │           │     ├─ ItineraryResponse.java
│  │  │           │     ├─ location
│  │  │           │     │  ├─ AddressComponentsDto.java
│  │  │           │     │  └─ LocationResponse.java
│  │  │           │     ├─ NotificationResponse.java
│  │  │           │     ├─ PermissionResponse.java
│  │  │           │     ├─ PostResponse.java
│  │  │           │     ├─ report
│  │  │           │     │  ├─ HandleReportResponse.java
│  │  │           │     │  ├─ ModerationActionResponse.java
│  │  │           │     │  └─ ReportResponse.java
│  │  │           │     ├─ RoleResponse.java
│  │  │           │     ├─ simple
│  │  │           │     │  ├─ ChatMessageSimpleResponse.java
│  │  │           │     │  ├─ GroupSimpleResponse.java
│  │  │           │     │  ├─ ItinerarySimpleResponse.java
│  │  │           │     │  └─ UserSimpleResponse.java
│  │  │           │     ├─ SuggestLocationResponse.java
│  │  │           │     ├─ TravelNotebookResponse.java
│  │  │           │     ├─ TripItemResponse.java
│  │  │           │     └─ UserResponse.java
│  │  │           ├─ entity
│  │  │           │  ├─ ActivityLog.java
│  │  │           │  ├─ BaseEntity.java
│  │  │           │  ├─ ChatMessage.java
│  │  │           │  ├─ Comment.java
│  │  │           │  ├─ Conversation.java
│  │  │           │  ├─ ConversationMember.java
│  │  │           │  ├─ embeddable
│  │  │           │  │  ├─ AddressComponents.java
│  │  │           │  │  └─ SoftDeleteInfo.java
│  │  │           │  ├─ Expense.java
│  │  │           │  ├─ Feedback.java
│  │  │           │  ├─ Group.java
│  │  │           │  ├─ GroupMember.java
│  │  │           │  ├─ HandleReportContent.java
│  │  │           │  ├─ InvalidatedToken.java
│  │  │           │  ├─ Itinerary.java
│  │  │           │  ├─ ItineraryTheme.java
│  │  │           │  ├─ Location.java
│  │  │           │  ├─ ModerationAction.java
│  │  │           │  ├─ Notification.java
│  │  │           │  ├─ Permission.java
│  │  │           │  ├─ Post.java
│  │  │           │  ├─ PostHashtag.java
│  │  │           │  ├─ ReportContent.java
│  │  │           │  ├─ Role.java
│  │  │           │  ├─ SuggestLocation.java
│  │  │           │  ├─ TravelNotebook.java
│  │  │           │  ├─ TripItem.java
│  │  │           │  └─ User.java
│  │  │           ├─ enums
│  │  │           │  ├─ ActivityAction.java
│  │  │           │  ├─ ConversationType.java
│  │  │           │  ├─ GroupRole.java
│  │  │           │  ├─ MapProvider.java
│  │  │           │  ├─ NotificationType.java
│  │  │           │  └─ OperationalStatus.java
│  │  │           ├─ exception
│  │  │           │  ├─ AppException.java
│  │  │           │  ├─ ErrorCode.java
│  │  │           │  └─ GlobalExceptionHandler.java
│  │  │           ├─ listener
│  │  │           │  ├─ GroupEventListener.java
│  │  │           │  ├─ MessageEventListener.java
│  │  │           │  └─ NotificationEventListener.java
│  │  │           ├─ mapper
│  │  │           │  ├─ ChatMessageMapper.java
│  │  │           │  ├─ ConversationMapper.java
│  │  │           │  ├─ GroupMapper.java
│  │  │           │  ├─ LocationMapper.java
│  │  │           │  ├─ NotificationMapper.java
│  │  │           │  ├─ PermissionMapper.java
│  │  │           │  ├─ RoleMapper.java
│  │  │           │  ├─ SuggestLocationMapper.java
│  │  │           │  └─ UserMapper.java
│  │  │           ├─ repository
│  │  │           │  ├─ ActivityLogRepository.java
│  │  │           │  ├─ ChatMessageRepository.java
│  │  │           │  ├─ ConversationMemberRepository.java
│  │  │           │  ├─ ConversationRepository.java
│  │  │           │  ├─ GroupMemberRepository.java
│  │  │           │  ├─ GroupRepository.java
│  │  │           │  ├─ InvalidatedTokenRepository.java
│  │  │           │  ├─ ItineraryRepository.java
│  │  │           │  ├─ LocationRepository.java
│  │  │           │  ├─ NotificationRepository.java
│  │  │           │  ├─ PermissionRepository.java
│  │  │           │  ├─ RoleRepository.java
│  │  │           │  ├─ SuggestLocationRepository.java
│  │  │           │  └─ UserRepository.java
│  │  │           ├─ service
│  │  │           │  ├─ IAdminService.java
│  │  │           │  ├─ IAuthenticationService.java
│  │  │           │  ├─ IChatMessageService.java
│  │  │           │  ├─ ICommentService.java
│  │  │           │  ├─ IConversationService.java
│  │  │           │  ├─ IFeedbackService.java
│  │  │           │  ├─ IGroupService.java
│  │  │           │  ├─ IItineraryService.java
│  │  │           │  ├─ ILocationService.java
│  │  │           │  ├─ impl
│  │  │           │  │  ├─ AdminService.java
│  │  │           │  │  ├─ AuthenticationService.java
│  │  │           │  │  ├─ ChatMessageService.java
│  │  │           │  │  ├─ CommentService.java
│  │  │           │  │  ├─ ConversationService.java
│  │  │           │  │  ├─ FeedbackService.java
│  │  │           │  │  ├─ GroupService.java
│  │  │           │  │  ├─ ItineraryService.java
│  │  │           │  │  ├─ LocationService.java
│  │  │           │  │  ├─ NotificationService.java
│  │  │           │  │  ├─ PermissionService.java
│  │  │           │  │  ├─ PostService.java
│  │  │           │  │  ├─ ReportService.java
│  │  │           │  │  ├─ RoleService.java
│  │  │           │  │  ├─ SocketService.java
│  │  │           │  │  ├─ SuggestLocationService.java
│  │  │           │  │  ├─ TravelNotebookService.java
│  │  │           │  │  └─ UserService.java
│  │  │           │  ├─ INotificationService.java
│  │  │           │  ├─ IPermissionService.java
│  │  │           │  ├─ IPostService.java
│  │  │           │  ├─ IReportService.java
│  │  │           │  ├─ IRoleService.java
│  │  │           │  ├─ ISocketService.java
│  │  │           │  ├─ ISuggestLocationService.java
│  │  │           │  ├─ ITravelNotebookService.java
│  │  │           │  └─ IUserService.java
│  │  │           ├─ TripjoyApiApplication.java
│  │  │           ├─ utils
│  │  │           │  ├─ PageableUtils.java
│  │  │           │  └─ SecurityUtils.java
│  │  │           └─ validator
│  │  │              ├─ PasswordConstraint.java
│  │  │              └─ PasswordValidator.java
│  │  └─ resources
│  │     ├─ application-dev.yaml
│  │     ├─ application-prod.yaml
│  │     └─ application.yaml
│  └─ test
│     └─ java
│        └─ com
│           └─ tripjoy
│              └─ api
│                 ├─ controller
│                 │  └─ UserControllerTest.java
│                 └─ TripjoyApiApplicationTests.java
├─ swagger
│  ├─ swagger.json
│  └─ swagger.yaml
└─ swagger.json

```