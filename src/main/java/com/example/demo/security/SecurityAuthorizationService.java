package com.example.demo.security;

import com.example.demo.entity.Users;
import com.example.demo.repository.UsersRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SecurityAuthorizationService {

    private final UsersRepository usersRepository;

    public SecurityAuthorizationService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isStaff(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
    }

    public Integer getCurrentUserShelterId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        Users user = usersRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || user.getShelter() == null) {
            return null;
        }
        return user.getShelter().getShelterId();
    }

    public boolean canAccessShelter(Authentication authentication, Integer shelterId) {
        if (authentication == null || shelterId == null) {
            return false;
        }

        if (isAdmin(authentication)) {
            return true;
        }

        if (!isStaff(authentication)) {
            return false;
        }

        Integer assignedShelterId = getCurrentUserShelterId(authentication);
        return Objects.equals(assignedShelterId, shelterId);
    }

    public void assertShelterAccess(Authentication authentication, Integer shelterId) {
        if (canAccessShelter(authentication, shelterId)) {
            return;
        }
        throw new AccessDeniedException("Forbidden: staff users may only access their assigned shelter");
    }
}
