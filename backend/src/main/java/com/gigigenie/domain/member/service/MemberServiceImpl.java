package com.gigigenie.domain.member.service;

import com.gigigenie.domain.member.dto.JoinRequestDTO;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.enums.MemberRole;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.props.JwtProps;
import com.gigigenie.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final JWTUtil jwtUtil;
    private final JwtProps jwtProps;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void join(JoinRequestDTO request) {
        memberRepository.findByEmail(request.getEmail())
                .ifPresent(member -> {
                    throw new IllegalArgumentException("이미 존재하는 회원입니다!");
                });

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
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
    public Map<String, Object> login(String id, String password) {
        Member member = memberRepository.findByEmail(id)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("password not found");
        }

        MemberDTO memberDTO = new MemberDTO(member.getMemberId(), member.getEmail(), member.getPassword(), member.getName(),
                member.getRole());

        log.info("memberService login memberDTO: {}", memberDTO);

        Map<String, Object> claims = memberDTO.getClaims();

        String accessToken = jwtUtil.generateToken(claims, jwtProps.getAccessTokenExpirationPeriod());
        String refreshToken = jwtUtil.generateToken(claims, jwtProps.getRefreshTokenExpirationPeriod());

        claims.put("accessToken", accessToken);
        claims.put("refreshToken", refreshToken);

        return claims;
    }

}
