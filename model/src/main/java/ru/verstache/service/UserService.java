package ru.verstache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.verstache.exception.AppException;
import ru.verstache.model.User;
import ru.verstache.dto.UserDto;
import ru.verstache.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByName(username).orElseThrow(() -> new AppException("No user with name " + username + " found in database", HttpStatus.NOT_FOUND));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDto userDto) {
            return userDto.getUsername();
        }
        throw new RuntimeException("No user found in a session");
    }
}
