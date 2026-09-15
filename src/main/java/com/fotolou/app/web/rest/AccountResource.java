package com.fotolou.app.web.rest;

import com.fotolou.app.domain.User;
import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.MailService;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.dto.AdminUserDTO;
import com.fotolou.app.service.dto.PasswordChangeDTO;
import com.fotolou.app.web.rest.errors.*;
import com.fotolou.app.web.rest.vm.KeyAndPasswordVM;
import com.fotolou.app.web.rest.vm.ManagedUserVM;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the current user's account.
 */
@Tag(name = "1. Authentification & OTP", description = "Profil et compte de l'utilisateur connecté")
@RestController
@RequestMapping("/api")
@Validated
public class AccountResource {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Account resource request invalid")
    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    private final UserService userService;

    private final MailService mailService;

    private final PasswordEncoder passwordEncoder;

    public AccountResource(UserService userService, MailService mailService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        LOG.debug("REST request to register account");
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        LOG.debug("REST request to activate account");
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        LOG.debug("REST request to get account");
        return userService
            .getUserWithAuthorities()
            .map(AdminUserDTO::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        LOG.debug("REST request to save account");
        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new AccountResourceException("Current user login not found")
        );
        Optional<User> existingUser = userService.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && !existingUser.orElseThrow().getLogin().equalsIgnoreCase(userLogin)) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userService.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        userService.updateUser(
            userDTO.getFirstName(),
            userDTO.getLastName(),
            userDTO.getEmail(),
            userDTO.getLangKey(),
            userDTO.getImageUrl()
        );
    }

    /**
     * {@code PUT/POST  /account/profile} : update the current user's profile (name, image, etc.).
     *
     * @param profileDTO Map containing profile fields such as "name", "firstName", "lastName", "imageUrl".
     * @return updated user info.
     */
    @RequestMapping(value = "/account/profile", method = { RequestMethod.PUT, RequestMethod.POST })
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileDTO) {
        LOG.debug("REST request to update user profile: {}", profileDTO);
        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new AccountResourceException("Current user login not found")
        );
        User user = userService.findOneByLogin(userLogin).orElseThrow(() -> new AccountResourceException("User could not be found"));

        String name = profileDTO.get("name");
        String firstName = profileDTO.get("firstName");
        String lastName = profileDTO.get("lastName");

        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split(" ", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (profileDTO.containsKey("imageUrl")) {
            user.setImageUrl(profileDTO.get("imageUrl"));
        } else if (profileDTO.containsKey("avatarUrl")) {
            user.setImageUrl(profileDTO.get("avatarUrl"));
        }

        userService.updateUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getLangKey(), user.getImageUrl());

        String fullName = (
            (user.getFirstName() != null ? user.getFirstName() : "") +
            (user.getLastName() != null && !user.getLastName().isBlank() ? " " + user.getLastName() : "")
        ).trim();

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("login", user.getLogin());
        result.put("name", fullName.isEmpty() ? user.getLogin() : fullName);
        result.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
        result.put("lastName", user.getLastName() != null ? user.getLastName() : "");
        result.put("imageUrl", user.getImageUrl());
        result.put("avatarUrl", user.getImageUrl());

        return ResponseEntity.ok(result);
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        LOG.debug("REST request to change password");
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody @Email @Size(min = 5, max = 254) String mail) {
        LOG.debug("REST request to request password reset");
        Optional<User> user = userService.requestPasswordReset(mail);
        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.orElseThrow());
        } else {
            // Pretend the request has been successful to prevent checking which emails really exist
            // but log that an invalid attempt has been made
            LOG.warn("Password reset requested for non existing mail");
        }
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            // Dummy hash to prevent reset-key enumeration via response-time timing attack:
            // mirrors the bcrypt cost of a successful path so both branches take equal time.
            passwordEncoder.encode(keyAndPassword.getNewPassword());
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
            StringUtils.isEmpty(password) ||
            password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
            password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }
}
