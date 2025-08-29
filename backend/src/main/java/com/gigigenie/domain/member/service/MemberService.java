package com.gigigenie.domain.member.service;

import com.gigigenie.domain.member.dto.JoinRequestDTO;

import java.util.Map;

public interface MemberService {

    void join(JoinRequestDTO request);

    boolean isEmailDuplicate(String email);

    Map<String, Object> login(String id, String password);

}

