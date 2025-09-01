package com.AIT.Optimanage.Support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // Optional provider so the app can start without mail configuration
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:no-reply@optimanage.com}")
    private String from;

    public void enviarCodigo(String destino, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destino);
        message.setSubject("Código de verificação");
        message.setText("Seu código é: " + codigo);
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

