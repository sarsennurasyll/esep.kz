package com.esep.account.web;

import com.esep.account.service.AccountDeletionService;
import com.esep.account.service.AccountRegistrationException;
import com.esep.account.service.AccountService;
import com.esep.security.AccountPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Регистрация, вход и страница учётной записи (ADR-0008). Сам вход и выход обрабатывает Spring Security.
 */
@Controller
public class AccountWebController {

    static final String CONSENT_REQUIRED = "Чтобы зарегистрироваться, подтвердите согласие с политикой конфиденциальности.";
    static final String PASSWORDS_DIFFER = "Пароли не совпадают.";

    static final String WRONG_PASSWORD = "Неверный пароль: учётная запись не удалена.";

    private final AccountService accountService;
    private final AccountDeletionService accountDeletionService;
    private final PasswordEncoder passwordEncoder;

    public AccountWebController(
            AccountService accountService,
            AccountDeletionService accountDeletionService,
            PasswordEncoder passwordEncoder
    ) {
        this.accountService = accountService;
        this.accountDeletionService = accountDeletionService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String passwordConfirmation,
            @RequestParam(name = "consent", defaultValue = "false") boolean consent,
            Model model,
            HttpServletResponse response,
            RedirectAttributes attributes
    ) {
        model.addAttribute("email", email);
        if (!consent) {
            return registerError(model, response, CONSENT_REQUIRED);
        }
        if (!password.equals(passwordConfirmation)) {
            return registerError(model, response, PASSWORDS_DIFFER);
        }
        try {
            accountService.register(email, password);
        } catch (AccountRegistrationException exception) {
            return registerError(model, response, messageFor(exception.reason()));
        }
        attributes.addFlashAttribute("successMessage", "Учётная запись создана. Войдите с адресом и паролем.");
        return "redirect:/login";
    }

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("account", accountService.findByEmail(user.getUsername()).orElseThrow());
        return "account";
    }

    @PostMapping("/account/delete")
    public String delete(
            @AuthenticationPrincipal AccountPrincipal user,
            @RequestParam String password,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes attributes
    ) {
        // В сессии хеш пароля стёрт после входа, поэтому он читается из базы.
        String passwordHash = accountService.findByEmail(user.getUsername()).orElseThrow().getPasswordHash();
        if (!passwordEncoder.matches(password, passwordHash)) {
            attributes.addFlashAttribute("deleteError", WRONG_PASSWORD);
            return "redirect:/account";
        }
        accountDeletionService.delete(user.accountId());
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?deleted";
    }

    private static String registerError(Model model, HttpServletResponse response, String message) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        model.addAttribute("registerError", message);
        return "register";
    }

    private static String messageFor(AccountRegistrationException.Reason reason) {
        return switch (reason) {
            case INVALID_EMAIL -> "Проверьте адрес электронной почты.";
            case PASSWORD_TOO_SHORT -> "Пароль должен быть не короче 10 символов.";
            case PASSWORD_TOO_LONG -> "Пароль слишком длинный: используйте не больше 72 латинских или 36 кириллических символов.";
            case PASSWORD_EQUALS_EMAIL -> "Пароль не должен совпадать с адресом почты.";
            // Регистрация открыта, поэтому скрыть занятость адреса нельзя; вход при этом не подсказывает, существует ли адрес.
            case EMAIL_TAKEN -> "Этот адрес уже зарегистрирован. Войдите или используйте другой адрес.";
        };
    }
}
