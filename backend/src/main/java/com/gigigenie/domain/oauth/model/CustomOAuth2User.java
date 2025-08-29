package com.gigigenie.domain.oauth.model;

import com.gigigenie.domain.member.enums.MemberRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    private final String email;
    private final Integer memberId;
    private final String name;
    private final MemberRole role;

    public CustomOAuth2User(
            Map<String, Object> attributes,
            String nameAttributeKey,
            String email,
            Integer memberId,
            String name,
            MemberRole role) {
        super(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name())),
                attributes,
                nameAttributeKey
        );
        this.email = email;
        this.memberId = memberId;
        this.name = name;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}