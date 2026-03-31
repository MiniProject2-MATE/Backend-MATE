package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.UserDto;
import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserById(Long userId);
    List<UserDto> getAllUsers();
    UserDto updateUser(Long userId, UserDto updatedUser);
    void deleteUser(Long userId);
    boolean checkNicknameDuplicate(String nickname);
}