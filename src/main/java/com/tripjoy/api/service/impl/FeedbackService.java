package com.tripjoy.api.service.impl;

import org.springframework.stereotype.Service;

import com.tripjoy.api.service.IFeedbackService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackService implements IFeedbackService {}
