package com.example.bankomat.config;

import com.example.bankomat.dto.EmailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Configuration
public class EmailConfig {
    @Value("${company.email.address}")
    private String email;
    @Value("${company.email.password}")
    private String emailPassword;

    @Bean
    public JavaMailSender send() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setPort(587);
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setUsername(email);
        javaMailSender.setPassword(emailPassword);

        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "true");
        return javaMailSender;
    }

    public boolean sendEmailHtml(EmailDto emailDto){
        try {
            JavaMailSender mailSender = send();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailDto.getMessage(), true);
            helper.setTo(emailDto.getTo());
            helper.setSubject(emailDto.getTitle());
            helper.setFrom(email);
            mailSender.send(mimeMessage);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
