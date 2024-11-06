package com.pollub.cookie.security;

import com.pollub.cookie.model.User;
import lombok.Getter;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.user = user;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRola().name()));
    }

    @Override
    public String getPassword() {
        return user.getHaslo();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
    public Long getId() {
        return user.getId();
    }

}
