package com.tripjoy.api.service.impl;

import org.springframework.stereotype.Service;

import com.tripjoy.api.service.IPostService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {}
