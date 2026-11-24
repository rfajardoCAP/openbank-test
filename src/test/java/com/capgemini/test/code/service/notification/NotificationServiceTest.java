package com.capgemini.test.code.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.model.NotificationChannel;
import com.capgemini.test.code.model.Role;
import com.capgemini.test.code.model.User;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

  @Test
  void sendsTheUserSavedMessageThroughTheChannelOfTheRole() {
    FakeSender email = new FakeSender(NotificationChannel.EMAIL);
    FakeSender sms = new FakeSender(NotificationChannel.SMS);
    NotificationService service = new NotificationService(List.of(email, sms));

    service.notifyUserSaved(user(Role.SUPERADMIN));

    assertThat(sms.messages).containsExactly("usuario guardado");
    assertThat(email.messages).isEmpty();
  }

  @Test
  void refusesToStartWhenARoleHasNoSenderForItsChannel() {
    List<NotificationSender> onlyEmail = List.of(new FakeSender(NotificationChannel.EMAIL));

    assertThatThrownBy(() -> new NotificationService(onlyEmail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("SMS");
  }

  @Test
  void refusesTwoSendersForTheSameChannel() {
    List<NotificationSender> duplicated = List.of(new FakeSender(NotificationChannel.EMAIL),
        new FakeSender(NotificationChannel.EMAIL), new FakeSender(NotificationChannel.SMS));

    assertThatThrownBy(() -> new NotificationService(duplicated))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("EMAIL");
  }

  private static User user(Role role) {
    return User.builder().id(1L).name("pablo").email("email@email.com").phone("677998899")
        .role(role).dni("23454234F").roomId(1L).build();
  }

  private static final class FakeSender implements NotificationSender {

    private final NotificationChannel channel;
    private final List<String> messages = new ArrayList<>();

    private FakeSender(NotificationChannel channel) {
      this.channel = channel;
    }

    @Override
    public NotificationChannel channel() {
      return channel;
    }

    @Override
    public void send(User recipient, String message) {
      messages.add(message);
    }
  }
}
