package com.gigigenie.domain.member.service;

import com.gigigenie.domain.member.dto.JoinDTO;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.enums.MemberRole;
import com.gigigenie.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void join(JoinDTO joinDTO) {
        memberRepository.findByEmail(joinDTO.getEmail())
            .ifPresent(member -> {
                throw new IllegalArgumentException("이미 존재하는 회원입니다!");
            });

        Member member = Member.builder()
            .email(joinDTO.getEmail())
            .password(passwordEncoder.encode(joinDTO.getPassword()))
            .name(joinDTO.getName())
            .role(MemberRole.USER)
            .joinDate(LocalDateTime.now())
            .build();

        memberRepository.save(member);
    }

    @Override
    public boolean isEmailDuplicate(String email) {
        try {
            return memberRepository.findByEmail(email).isPresent();
        } catch (Exception e) {
            log.error("이메일 중복 체크 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MemberDTO login(String id, String password) {
        Member member = memberRepository.findByEmail(id)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new MemberDTO(member.getMemberId(), member.getEmail(), member.getPassword(),
            member.getName(),
            member.getRole());
    }
}