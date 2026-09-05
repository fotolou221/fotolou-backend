package com.fotolou.app.service;

import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.AdminUserDTO;
import com.fotolou.app.service.dto.UserDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing users.
 */
public interface UserService {
    Optional<User> activateRegistration(String key);

    Optional<User> completePasswordReset(String newPassword, String key);

    Optional<User> requestPasswordReset(String mail);

    User registerUser(AdminUserDTO userDTO, String password);

    User createUser(AdminUserDTO userDTO);

    Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO);

    void deleteUser(String login);

    void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl);

    void changePassword(String currentClearTextPassword, String newPassword);

    Page<AdminUserDTO> getAllManagedUsers(Pageable pageable);

    Page<UserDTO> getAllPublicUsers(Pageable pageable);

    Optional<User> getUserWithAuthoritiesByLogin(String login);

    Optional<User> getUserWithAuthorities();

    void removeNotActivatedUsers();

    List<String> getAuthorities();

    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneByLogin(String login);
}
