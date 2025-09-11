package com.AIT.Optimanage.Support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // Optional provider so the app can start without mail configuration
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MessageSource messageSource;

    @Value("${spring.mail.username:no-reply@optimanage.com}")
    private String from;

    @Async
    public void enviarCodigo(String destino, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destino);
        String subject = messageSource.getMessage("email.verification.subject", null, LocaleContextHolder.getLocale());
        String text = messageSource.getMessage("email.verification.text", new Object[]{codigo}, LocaleContextHolder.getLocale());
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(from);

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("JavaMailSender não configurado. Ignorando envio de e-mail para {}", destino);
            return;
        }

        try {
            sender.send(message);
        } catch (Exception ex) {
            log.error("Falha ao enviar e-mail para {}: {}", destino, ex.getMessage());
        }
    }
}

