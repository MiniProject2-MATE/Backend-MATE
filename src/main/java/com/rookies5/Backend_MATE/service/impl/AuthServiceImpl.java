package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.AuthResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.security.JwtTokenProvider;
import com.rookies5.Backend_MATE.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserResponseDto register(UserRequestDto requestDto, MultipartFile profileImage) {
        isEmailAvailable(requestDto.getEmail());
        isPhoneAvailable(requestDto.getPhoneNumber(), null);

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(encodedPassword);

        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImgUrl = "https://mate-s3.com/uploaded-" + profileImage.getOriginalFilename();
            requestDto.setProfileImg(profileImgUrl);
        }

        User user = UserMapper.mapToUser(requestDto);
        isNicknameAvailable(user.getNickname(), null);

        User savedUser = userRepository.save(user);
        return UserMapper.mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(String email, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600)
                    .user(AuthResponseDto.UserInfo.builder()
                            .id(user.getId())
                            .nickname(user.getNickname())
                            .email(user.getEmail())
                            .position(user.getPosition() != null ? user.getPosition().name() : null)
                            .build())
                    .build();

        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isEmailAvailable(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (email == null || !email.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isPhoneAvailable(String phoneNumber, Long userId) {
        if (phoneNumber == null) throw new BusinessException(ErrorCode.INVALID_PHONE_FORMAT);
        String targetPhone = phoneNumber.trim();
        if (!targetPhone.matches("^\\d{11}$")) throw new BusinessException(ErrorCode.INVALID_PHONE_FORMAT);

        boolean isDuplicate = (userId == null) ?
                userRepository.existsByPhoneNumber(targetPhone) :
                userRepository.existsByPhoneNumberAndIdNot(targetPhone, userId);

        if (isDuplicate) throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isNicknameAvailable(String nickname, Long currentUserId) {
        if (nickname == null) throw new BusinessException(ErrorCode.USER_NICKNAME_FORMAT_INVALID);
        String targetNickname = nickname.trim();
        if (!targetNickname.matches("^[a-zA-Z0-9가-힣]{2,10}$")) throw new BusinessException(ErrorCode.USER_NICKNAME_FORMAT_INVALID);

        boolean isDuplicate = (currentUserId == null) ?
                userRepository.existsByNicknameIgnoreCase(targetNickname) :
                userRepository.existsByNicknameIgnoreCaseAndIdNot(targetNickname, currentUserId);

        if (isDuplicate) throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public String findEmailByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, phoneNumber));
        return user.getEmail();
    }

    @Override
    public String resetPassword(String email, String phoneNumber) {
        User user = userRepository.findByEmailAndPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_MATCHED));

        String tempPassword = generateTempPassword();
        user.updatePassword(passwordEncoder.encode(tempPassword));

        return tempPassword;
    }

    private String generateTempPassword() {
        char[] charSet = new char[]{
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };
        StringBuilder tempPw = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int idx = (int) (charSet.length * Math.random());
            tempPw.append(charSet[idx]);
        }
        return tempPw.toString();
    }
}