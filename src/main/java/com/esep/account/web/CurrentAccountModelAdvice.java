package com.esep.account.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Передаёт в шаблоны адрес вошедшего пользователя, чтобы меню показывало «Войти» или «Аккаунт».
 */
@ControllerAdvice
public class CurrentAccountModelAdvice {

    @ModelAttribute("currentAccountEmail")
    public String currentAccountEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}
