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
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "commentCount", source = "commentCount")
    @Mapping(target = "hashtags", source = "hashtags", qualifiedByName = "mapHashtagsToStrings")
    @Mapping(target = "isLiked", ignore = true)
    @Mapping(target = "isSaved", ignore = true)
    @Mapping(target = "latestComments", ignore = true)
    PostResponse toPostResponse(Post post, @Context com.tripjoy.api.dto.context.PostMappingContext context);

    @AfterMapping
    default void setContextFields(@MappingTarget PostResponse response, Post post, @Context com.tripjoy.api.dto.context.PostMappingContext context) {
        if (context != null) {
            response.setIsLiked(context.isLiked(post.getId()));
            response.setIsSaved(context.isSaved(post.getId()));
        } else {
            response.setIsLiked(false);
            response.setIsSaved(false);
        }
    }

    @Mapping(target = "author", source = "creator")
    @Mapping(target = "contentSnippet", expression = "java(post.getContent() != null ? (post.getContent().length() > 50 ? post.getContent().substring(0, 50) + \"...\" : post.getContent()) : \"\")")
    @Mapping(target = "thumbnailUrl", expression = "java(post.getMediaUrls() != null && !post.getMediaUrls().isEmpty() ? post.getMediaUrls().get(0) : null)")
    com.tripjoy.api.dto.response.simple.PostSimpleResponse toPostSimpleResponse(Post post);

    @Named("mapHashtagsToStrings")
    default Set<String> mapHashtagsToStrings(Set<Hashtag> hashtags) {
        if (hashtags == null) {
            return Collections.emptySet();
        }
        return hashtags.stream().map(Hashtag::getName).collect(Collectors.toSet());
    }
}
