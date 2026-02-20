package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.dto.UserRegistrationDto;
import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.exception.UserNotFoundException;
import cz.mp.construction_site_diary.exception.UserRegistrationException;
import cz.mp.construction_site_diary.exception.UserRegistrationException.ErrorCode;
import cz.mp.construction_site_diary.mapper.UserMapper;
import cz.mp.construction_site_diary.repository.UserRepository;
import cz.mp.construction_site_diary.service.KeycloakService;
import cz.mp.construction_site_diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserRegistrationDto registerUser(UserRegistrationDto dto) {
        validateEmailAvailable(dto.email());

        String keycloakId = keycloakService.createUser(
                userMapper.toKeycloakUser(dto),
                dto.password()
        );

        User user = userMapper.toEntity(dto, keycloakId);
        userRepository.save(user);

        LOG.info("User registered successfully: {}", dto.email());

        return userMapper.toDto(user);
    }

    private void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserRegistrationException("Email already exists", ErrorCode.EMAIL_EXISTS);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void userExists(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
    }
}