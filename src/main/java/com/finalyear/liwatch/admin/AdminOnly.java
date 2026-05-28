package com.finalyear.liwatch.admin;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Restricts a controller method or class to users with ROLE_ADMIN only.
 *
 * Usage:
 *   @AdminOnly
 *   public ResponseEntity<?> doAdminThing() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('ADMIN')")
public @interface AdminOnly {
}
