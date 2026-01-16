package com.example.demo.Telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class NotificationBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(NotificationBot.class);

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived вызван, update={}", update);

        if (update.hasMessage() && update.getMessage().hasText()) {
            Long incomingChatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            log.info("CHAT ID = {}, text={}", incomingChatId, text);

            SendMessage msg = new SendMessage(
                    String.valueOf(incomingChatId),
                    "Бот запущен. Вы написали: " + text
            );
            try {
                execute(msg);
                log.info("Ответ пользователю {} успешно отправлен", incomingChatId);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа в Telegram: {}", e.getMessage(), e);
            }
        }
    }

    public void sendNotification(String message) {
        SendMessage msg = new SendMessage(chatId, message);
        try {
            execute(msg);
            log.info("Уведомление в Telegram успешно отправлено в чат {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке уведомления в Telegram: {}", e.getMessage(), e);
        }
    }
}
