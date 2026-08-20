package com.shopzone.user.repository;

import com.shopzone.user.domain.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecs {

    private UserSpecs() {
    }

    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<User> emailContains(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String pattern = "%" + email.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern);
    }

    public static Specification<User> enabled(Boolean enabled) {
        if (enabled == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("enabled"), enabled);
    }
}
