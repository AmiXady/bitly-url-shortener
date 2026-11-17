package kz.amixady.auth.service.mail;

import kz.amixady.auth.entity.EmailAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String confirmUrl;
    private final String resetUrl;

    public SmtpMailService(JavaMailSender mailSender,
                           @Value("${app.mail.from}") String from,
                           @Value("${app.mail.confirm-url}") String confirmUrl,
                           @Value("${app.mail.reset-url}") String resetUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.confirmUrl = confirmUrl;
        this.resetUrl = resetUrl;
    }

    @Override
    public void sendEmailConfirmation(EmailAddress to, String token) {
        send(to, "Подтвердите email в sho.rt", """
                Здравствуйте!
                
                Чтобы подтвердить адрес, перейдите по ссылке:
                %s?token=%s
                
                Ссылка действует 24 часа.
                Если вы не регистрировались в sho.rt, просто проигнорируйте это письмо.
                """.formatted(confirmUrl, token));
    }

    @Override
    public void sendAlreadyRegistered(EmailAddress to) {
        send(to, "Попытка регистрации в sho.rt", """
                Здравствуйте!
                
                Кто-то пытался зарегистрироваться в sho.rt с вашим адресом.
                У вас уже есть аккаунт — просто войдите.
                Если вы забыли пароль, восстановите его на странице входа.
                
                Если это были не вы, ничего делать не нужно.
                """);
    }

    @Override
    public void sendPasswordReset(EmailAddress to, String token) {
        send(to, "Сброс пароля в sho.rt", """
                Здравствуйте!
                
                Чтобы задать новый пароль, перейдите по ссылке:
                %s?token=%s
                
                Ссылка действует 1 час и срабатывает один раз.
                Если вы не запрашивали сброс, просто проигнорируйте это письмо — пароль не изменится.
                """.formatted(resetUrl, token));
    }

    @Override
    public void sendPasswordChanged(EmailAddress to) {
        send(to, "Ваш пароль в sho.rt изменён", """
                Здравствуйте!
                
                Пароль от вашего аккаунта sho.rt только что был изменён.
                Если это были вы, ничего делать не нужно.
                
                Если вы не меняли пароль, срочно восстановите его на странице входа.
                """);
    }

    private void send(EmailAddress to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to.value());
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Не удалось отправить письмо «{}» на {}", subject, to, e);
        }
    }
}