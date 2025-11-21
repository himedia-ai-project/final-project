package com.gigigenie.domain.member.dto;

import com.gigigenie.domain.member.enums.MemberRole;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
@ToString
public class MemberDTO extends User {

    private final Integer id;
    private final String email;
    private final String name;
    private final MemberRole role;

    public MemberDTO(Integer id, String email, String password, String name, MemberRole role) {
        super(email, password,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name())));
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", this.id);
        dataMap.put("email", this.email);
        dataMap.put("name", this.name);
        dataMap.put("role", this.role);
        return dataMap;
    }

}
