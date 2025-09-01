package com.AIT.Optimanage.Support;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@optimanage.com}")
    private String from;

    public void enviarCodigo(String destino, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destino);
        message.setSubject("Código de verificação");
        message.setText("Seu código é: " + codigo);
        message.setFrom(from);
        mailSender.send(message);
    }
}
