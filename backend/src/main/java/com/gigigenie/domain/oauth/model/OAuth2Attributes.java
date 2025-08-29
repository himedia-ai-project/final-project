package com.gigigenie.domain.oauth.model;

import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Getter
public class OAuth2Attributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;
    private String providerId;

    @Builder
    public OAuth2Attributes(Map<String, Object> attributes, String nameAttributeKey,
                            String name, String email, String picture,
                            String provider, String providerId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static OAuth2Attributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        log.info("registrationId: {}", registrationId);

        if ("google".equals(registrationId)) {
            return ofGoogle(registrationId, userNameAttributeName, attributes);
        }

        throw new IllegalArgumentException("Unsupported provider: " + registrationId);
    }

    private static OAuth2Attributes ofGoogle(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider(registrationId)
                .providerId((String) attributes.get("sub"))
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .role(MemberRole.USER)
                .provider(provider)
                .providerId(providerId)
                .build();
    }
}