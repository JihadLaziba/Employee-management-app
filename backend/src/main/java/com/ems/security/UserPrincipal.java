package com.ems.security;

import com.ems.enums.Permission;
import com.ems.enums.Role;
import com.ems.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean enabled;
    private final Set<Permission> permissions;
    private final List<GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
        this.permissions = user.isSuperAdmin()
                ? EnumSet.allOf(Permission.class)
                : (user.getPermissions().isEmpty()
                        ? EnumSet.noneOf(Permission.class)
                        : EnumSet.copyOf(user.getPermissions()));

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        permissions.forEach(p -> list.add(new SimpleGrantedAuthority("PERM_" + p.name())));
        this.authorities = Collections.unmodifiableList(list);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }
}
