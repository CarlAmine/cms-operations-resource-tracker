package org.cmstracker.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @Builder @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private UserDTO user;
}
