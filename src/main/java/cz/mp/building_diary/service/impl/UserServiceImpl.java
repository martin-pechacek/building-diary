package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.controller.v1.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.controller.v1.dto.UserRegistrationResponseDto;
import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.exception.UserRegistrationException;
import cz.mp.building_diary.exception.UserRegistrationException.ErrorCode;
import cz.mp.building_diary.mapper.UserMapper;
import cz.mp.building_diary.repository.UserRepository;
import cz.mp.building_diary.service.KeycloakService;
import cz.mp.building_diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final static Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserRegistrationResponseDto registerUser(UserRegistrationRequestDto request) {
        validateEmailAvailable(request.email());

        String keycloakId = keycloakService.createUser(
                userMapper.toKeycloakUser(request),
                request.password()
        );

        User user = userMapper.toEntity(request, keycloakId);
        userRepository.save(user);

        LOG.info("User registered successfully: {}", request.email());

        return userMapper.toResponseDto(user);
    }

    private void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserRegistrationException("Email already exists", ErrorCode.EMAIL_EXISTS);
        }
    }
}