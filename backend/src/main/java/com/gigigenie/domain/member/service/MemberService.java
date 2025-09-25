package com.gigigenie.domain.member.service;

import com.gigigenie.domain.member.dto.JoinDTO;
import com.gigigenie.domain.member.dto.MemberDTO;

public interface MemberService {

    void join(JoinDTO request);

    boolean isEmailDuplicate(String email);

    MemberDTO login(String id, String password);

}

