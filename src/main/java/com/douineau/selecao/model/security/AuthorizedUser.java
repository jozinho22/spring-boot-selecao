package com.douineau.selecao.model.security;

import com.douineau.selecao.model.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class AuthorizedUser extends AbstractEntity implements UserDetails  {

    @Column(unique = true)
    @Email(message = "This should be a valid mail")
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Collection<Role> roles;

    public AuthorizedUser(String email, String password, Collection<Role> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
    public AuthorizedUser() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        for(Role role : roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        return grantedAuthorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
