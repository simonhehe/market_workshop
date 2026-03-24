package com.example.mall.service;

import com.example.mall.entity.User;
import com.example.mall.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("updatePoints: 用户不存在则失败（订单扣/退款会间接触发）")
    void updatePoints_failsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePoints(1L, -100))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    @DisplayName("updatePoints: 成功累加积分")
    void updatePoints_success() {
        User user = new User();
        user.setId(1L);
        user.setPoints(200);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updatePoints(1L, -50);

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(cap.capture());
        assertThat(cap.getValue().getPoints()).isEqualTo(150);
    }
}
