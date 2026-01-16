package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.service.UserService;
import com.example.demo.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;

@Tag(name = "Authentication", description = "API для аутентификации и управления сессиями")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthServiceImpl authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Логин пользователя", description = "Аутентификация пользователя по логину и паролю. Возвращает JWT-токены.")
    @ApiResponse(responseCode = "200", description = "Успешная аутентификация")
    @ApiResponse(responseCode = "500", description = "Неверные учетные данные пользователя")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @CookieValue(name = "access_token", required = false)
            String accessToken,
            @CookieValue(name = "refresh_token", required = false)
            String refreshToken,
            @RequestBody LoginRequest loginRequest) {

        log.info("Попытка входа пользователя username={}", loginRequest.username());
        try {
            ResponseEntity<LoginResponse> response = authService.login(loginRequest, accessToken, refreshToken);
            if (response.getStatusCode().is2xxSuccessful()) {
                LoginResponse body = response.getBody();
                log.info("Успешный вход пользователя username={}, roles={}", loginRequest.username(),
                        body != null ? body.roles() : null);
            } else {
                log.warn("Неуспешная попытка входа пользователя username={}, status={}",
                        loginRequest.username(), response.getStatusCode());
            }
            return response;
        } catch (Exception e) {
            log.error("Ошибка при аутентификации пользователя username={}: {}", loginRequest.username(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Обновление токена", description = "Генерация нового access-токена по refresh-токену")
    @ApiResponse(responseCode = "200", description = "Токен успешно обновлен")
    @ApiResponse(responseCode = "400", description = "Недействительный refresh-токен (Refresh token is invalid)")
    @ApiResponse(responseCode = "404", description = "Не был получен токен")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        log.info("Запрос обновления токена");
        if (refreshToken == null) {
            log.warn("Обновление токена: refresh_token отсутствует");
            return ResponseEntity.notFound().build();
        }
        try {
            ResponseEntity<LoginResponse> response = authService.refresh(refreshToken);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Токен успешно обновлён");
            } else {
                log.warn("Неуспешное обновление токена, status={}", response.getStatusCode());
            }
            return response;
        } catch (Exception e) {
            log.error("Ошибка при обновлении токена: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Выход из системы", description = "Инвалидация текущих JWT-токенов")
    @ApiResponse(responseCode = "200", description = "Сессия завершена")
    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            @CookieValue(name = "access_token", required = false) String accessToken,
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        log.info("Выход пользователя, access_token={}, refresh_token={}",
                accessToken != null, refreshToken != null);
        try {
            ResponseEntity<LoginResponse> response = authService.logout(accessToken, refreshToken);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Сессия пользователя успешно завершена");
            } else {
                log.warn("Неуспешный logout, status={}", response.getStatusCode());
            }
            return response;
        } catch (Exception e) {
            log.error("Ошибка при завершении сессии: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(
        summary = "Информация о пользователе",
        description = "Получение данных текущего аутентифицированного пользователя",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Данные пользователя")
    @ApiResponse(responseCode = "401", description = "Требуется аутентификация")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public ResponseEntity<UserLoggedDto> userLoggedInfo() {
        log.info("Запрос информации о текущем пользователе");
        try {
            UserLoggedDto info = authService.getUserLoggedInfo();
            log.info("Информация о пользователе получена: username={}, role={}", info.username(), info.role());
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Ошибка при получении информации о текущем пользователе: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/change_password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Запрос смены пароля для текущего пользователя");
        try {
            if (!request.confirmPassword().equals(request.newPassword())) {
                log.warn("Смена пароля отклонена: подтверждение пароля не совпадает");
                return ResponseEntity.badRequest().build();
            }

            UserLoggedDto logged = authService.getUserLoggedInfo();
            UserDto user = userService.getUser(logged.username());
            log.info("Проверка текущего пароля для пользователя username={}", user.username());

            if (passwordEncoder.matches(request.currentPassword(), user.password())) {
                userService.updateUser(
                        user.id(),
                        new UserDto(user.id(), user.username(),
                                request.newPassword(), user.role(), user.permissions()));
                log.info("Пароль успешно изменён для пользователя username={}", user.username());
                return ResponseEntity.ok("пароль успешно изменен");
            }

            log.warn("Смена пароля: текущий пароль не совпадает для пользователя username={}", user.username());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ошибка при смене пароля: {}", e.getMessage(), e);
            throw e;
        }
    }
}
