package com.tripjoy.api.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.*;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.PostResponse;
import com.tripjoy.api.dto.response.simple.ItinerarySimpleResponse;
import com.tripjoy.api.entity.Hashtag;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.Post;

@Mapper(config = BaseMapperConfig.class, uses = {UserMapper.class})
public interface PostMapper {

    ItinerarySimpleResponse toItinerarySimpleResponse(Itinerary itinerary);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "mediaUrls", source = "mediaUrls")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "sharedQuantity", source = "shareQuantity")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "itinerary", source = "itinerary")
    @Mapping(target = "createdByUser", source = "creator")
    @Mapping(target = "likeCount", expression = "java((long) (post.getLikeUsers() != null ? post.getLikeUsers().size() : 0))")
    @Mapping(target = "commentCount", expression = "java((long) (post.getComments() != null ? post.getComments().size() : 0))")
    @Mapping(target = "hashtags", source = "hashtags", qualifiedByName = "mapHashtagsToStrings")
    @Mapping(target = "isLiked", ignore = true) // Set manually in service
    @Mapping(target = "isSaved", ignore = true) // Set manually in service
    @Mapping(target = "latestComments", ignore = true) // Handled separately if needed
    PostResponse toPostResponse(Post post);

    @Named("mapHashtagsToStrings")
    default Set<String> mapHashtagsToStrings(Set<Hashtag> hashtags) {
        if (hashtags == null) {
            return Collections.emptySet();
        }
        return hashtags.stream().map(Hashtag::getName).collect(Collectors.toSet());
    }
}
