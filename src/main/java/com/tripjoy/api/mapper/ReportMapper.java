package com.tripjoy.api.mapper;

import org.mapstruct.*;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.entity.HandleReportContent;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {UserMapper.class})
public interface ReportMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "reason", source = "report_type")
    @Mapping(target = "status", source = "reportContent.status")
    @Mapping(target = "reportedBy", source = "ba.id")
    @Mapping(target = "reportedEntityId", source = "reportContent.id")
    @Mapping(target = "reportedEntityType", source = "reportContent.contentType")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ReportResponse toReportResponse(HandleReportContent handleReport);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "reportContentId", source = "reportContent.id")
    @Mapping(target = "handledBy", source = "ba", qualifiedByName = "toUserSimpleResponse")
    @Mapping(target = "handledAt", source = "updatedAt")
    @Mapping(target = "status", source = "reportContent.status")
    @Mapping(target = "description", source = "description")
    HandleReportResponse toHandleReportResponse(HandleReportContent handleReport);

    @Named("toUserSimpleResponse")
    UserSimpleResponse toUserSimpleResponse(com.tripjoy.api.entity.User user);
}
