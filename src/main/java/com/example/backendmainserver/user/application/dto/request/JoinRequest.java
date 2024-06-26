package com.example.backendmainserver.user.application.dto.request;

import lombok.Builder;

@Builder
public record JoinRequest(String loginId, String password, String nickname, String email, String phoneNumber) {

}
