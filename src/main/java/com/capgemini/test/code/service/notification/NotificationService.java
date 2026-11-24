package com.capgemini.test.code.service.notification;

import com.capgemini.test.code.model.NotificationChannel;
import com.capgemini.test.code.model.Role;
import com.capgemini.test.code.model.User;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Elige la estrategia de notificación según el canal que corresponde al rol del usuario.
 *
 * <p>Al construirse comprueba que hay exactamente una estrategia por cada canal que usa algún rol,
 * para que una configuración incompleta falle al arrancar y no con la primera petición.
 */
@Service
public class NotificationService {

  static final String USER_SAVED_MESSAGE = "usuario guardado";

  private final Map<NotificationChannel, NotificationSender> senders = new EnumMap<>(NotificationChannel.class);

  public NotificationService(List<NotificationSender> notificationSenders) {
    notificationSenders.forEach(this::register);
    Arrays.stream(Role.values())
        .map(Role::notificationChannel)
        .filter(channel -> !senders.containsKey(channel))
        .findFirst()
        .ifPresent(channel -> {
          throw new IllegalStateException("No notification sender for channel " + channel);
        });
  }

  public void notifyUserSaved(User user) {
    senders.get(user.notificationChannel()).send(user, USER_SAVED_MESSAGE);
  }

  private void register(NotificationSender sender) {
    if (senders.putIfAbsent(sender.channel(), sender) != null) {
      throw new IllegalStateException("More than one notification sender for channel " + sender.channel());
    }
  }
}
