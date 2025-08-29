package com.gigigenie.domain.oauth.service;

import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.oauth.model.CustomOAuth2User;
import com.gigigenie.domain.oauth.model.OAuth2Attributes;
import com.gigigenie.props.JwtProps;
import com.gigigenie.util.CookieUtil;
import com.gigigenie.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;
    private final JwtProps jwtProps;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 로그인 - registrationId: {}", registrationId);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        log.info("OAuth2 attributes: {}", attributes);

        Member member = saveOrUpdate(attributes);
        log.info("OAuth2 사용자 저장/업데이트 완료: id={}, email={}", member.getMemberId(), member.getEmail());

        return new CustomOAuth2User(
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                member.getEmail(),
                member.getMemberId(),
                member.getName(),
                member.getRole()
        );
    }

    private Member saveOrUpdate(OAuth2Attributes attributes) {
        log.info("OAuth2 사용자 데이터 저장/업데이트 시작: email={}", attributes.getEmail());

        Member member = memberRepository.findByEmail(attributes.getEmail())
                .map(entity -> {
                    log.info("기존 사용자 정보 업데이트: email={}, name={}", entity.getEmail(), attributes.getName());
                    entity.updateName(attributes.getName());
                    entity.updateOAuth2Info(attributes.getProvider(), attributes.getProviderId());
                    return entity;
                })
                .orElseGet(() -> {
                    log.info("새 사용자 생성: email={}, name={}", attributes.getEmail(), attributes.getName());
                    return attributes.toEntity();
                });

        Member savedMember = memberRepository.save(member);
        log.info("사용자 저장 완료: id={}, email={}", savedMember.getMemberId(), savedMember.getEmail());

        return savedMember;
    }

    public Map<String, Object> generateTokens(CustomOAuth2User oAuth2User, HttpServletResponse response) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", oAuth2User.getMemberId());
        claims.put("email", oAuth2User.getEmail());
        claims.put("name", oAuth2User.getName());
        claims.put("role", oAuth2User.getRole().name());

        log.info("토큰 생성 시작: id={}, email={}", oAuth2User.getMemberId(), oAuth2User.getEmail());

        String accessToken = jwtUtil.generateToken(claims, jwtProps.getAccessTokenExpirationPeriod());
        String refreshToken = jwtUtil.generateToken(claims, jwtProps.getRefreshTokenExpirationPeriod());

        log.info("토큰 생성 완료: accessToken 생성됨");

        CookieUtil.setTokenCookie(response, "accessToken", accessToken, jwtProps.getAccessTokenExpirationPeriod());
        CookieUtil.setTokenCookie(response, "refreshToken", refreshToken, jwtProps.getRefreshTokenExpirationPeriod());

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("id", oAuth2User.getMemberId().toString());
        tokenInfo.put("name", oAuth2User.getName());
        tokenInfo.put("role", oAuth2User.getRole().name());
        tokenInfo.put("accessToken", accessToken);
        tokenInfo.put("refreshToken", refreshToken);

        return tokenInfo;
    }
}
