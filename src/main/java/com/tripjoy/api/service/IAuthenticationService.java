package com.tripjoy.api.service;

import com.nimbusds.jose.JOSEException;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.auth.AuthenticationRequest;
import com.tripjoy.api.dto.request.auth.IntrospectRequest;
import com.tripjoy.api.dto.request.auth.LogoutRequest;
import com.tripjoy.api.dto.request.auth.RefreshRequest;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.auth.AuthenticationResponse;
import com.tripjoy.api.dto.response.auth.IntrospectResponse;

import java.text.ParseException;

public interface IAuthenticationService {
    UserResponse register(UserCreationRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    IntrospectResponse introspectToken(IntrospectRequest request);

    void logout(LogoutRequest request) throws JOSEException, ParseException;

    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
}
