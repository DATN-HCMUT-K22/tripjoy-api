package com.tripjoy.api.controller;

import com.nimbusds.jose.JOSEException;
import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.AuthenticationRequest;
import com.tripjoy.api.dto.request.IntrospectRequest;
import com.tripjoy.api.dto.request.LogoutRequest;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.AuthenticationResponse;
import com.tripjoy.api.dto.response.IntrospectResponse;
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
@Tag(name = "Authentication", description = "Endpoints for user authentication")
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

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspectToken(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspectToken(request);
        return ApiResponse.<IntrospectResponse>builder()
                .data(result)
                .build();
    }

    @Operation(summary = "Log out from the system")
    @PostMapping(Endpoint.Auth.LOGOUT)
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException{
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    //// --------
    @Operation(summary = "Register a new user account")
    @PostMapping(Endpoint.Auth.REGISTER)
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
//                .data(authService.register(request))
                .build();
    }
    //// --------
}
