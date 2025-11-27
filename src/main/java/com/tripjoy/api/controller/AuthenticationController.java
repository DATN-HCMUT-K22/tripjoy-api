package com.tripjoy.api.controller;

import com.nimbusds.jose.JOSEException;
import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.auth.AuthenticationRequest;
import com.tripjoy.api.dto.request.auth.IntrospectRequest;
import com.tripjoy.api.dto.request.auth.LogoutRequest;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.auth.RefreshRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.auth.AuthenticationResponse;
import com.tripjoy.api.dto.response.auth.IntrospectResponse;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping(Endpoint.Auth.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "Endpoints for users authentication")
public class AuthenticationController {
    AuthenticationService authenticationService;

    @Operation(summary = "Log in to the system")
    @PostMapping(Endpoint.Auth.LOGIN)
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }

    @Operation(summary = "Introspect authentication token")
    @PostMapping(Endpoint.Auth.INTROSPECT)
    public ApiResponse<IntrospectResponse> introspectToken(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspectToken(request);
        return ApiResponse.<IntrospectResponse>builder()
                .data(result)
                .build();
    }

    @Operation(summary = "Refresh authentication token")
    @PostMapping(Endpoint.Auth.REFRESH)
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }

    @Operation(summary = "Log out from the system")
    @PostMapping(Endpoint.Auth.LOGOUT)
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    //// --------
    @Operation(summary = "Register a new users account")
    @PostMapping(Endpoint.Auth.REGISTER)
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
//                .data(authService.register(request))
                .build();
    }
    //// --------
}
