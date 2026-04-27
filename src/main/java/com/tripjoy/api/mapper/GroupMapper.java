package com.tripjoy.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.response.GroupMemberResponse;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.GroupMember;

@Mapper(config = BaseMapperConfig.class)
public interface GroupMapper {

    // 1. Request -> Entity (Dùng khi tạo mới)
    // Các field không có trong Request sẽ được set null hoặc default của Entity
    Group toGroup(GroupRequest request);

    // 2. Entity -> Response
    @Mapping(target = "itiCount", expression = "java(group.getItineraries() != null ? group.getItineraries().size() : 0)")
    GroupResponse toGroupResponse(Group group);

    // 3. Update Entity từ Request (Dùng cho hàm updateGroup)
    // @MappingTarget: Map đè dữ liệu từ request vào entity có sẵn
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "itineraries", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    void updateGroup(@MappingTarget Group group, GroupRequest request);

    // 4. Member Entity -> Member Response
    // Giả sử GroupMemberResponse có chứa UserResponse bên trong
    // MapStruct sẽ tự động map các field trùng tên (isLeader, id...)
    // Field 'user' trong GroupMember sẽ được map sang field 'user' trong Response
    @Mapping(source = "user", target = "user")
    GroupMemberResponse toGroupMemberResponse(GroupMember groupMember);
}
