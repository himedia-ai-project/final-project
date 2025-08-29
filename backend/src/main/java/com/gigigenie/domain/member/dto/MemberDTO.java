package com.gigigenie.domain.member.dto;

import com.gigigenie.domain.member.enums.MemberRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class MemberDTO extends User {
    private Integer id;
    private String email;
    private String password;
    private String name;
    private MemberRole role;

    public MemberDTO(Integer id, String email, String password, String name, MemberRole role) {
        super(email, password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name())));
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", this.id);
        dataMap.put("email", this.email);
        dataMap.put("password", this.password);
        dataMap.put("name", this.name);
        dataMap.put("role", this.role);

        return dataMap;
    }

}
