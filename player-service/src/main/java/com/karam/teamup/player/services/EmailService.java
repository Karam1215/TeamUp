package com.karam.teamup.player.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationMail(String email,String name ,String token) {
        String subject = "🚀 Welcome to TeamUp! Confirm Your Account Now";
        String verificationUrl = "http://localhost:8080/api/v1/auth/verify?token=" + token;

        String htmlContent = buildEmail(name, verificationUrl);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Arial,sans-serif;font-size:16px;margin:0;padding:20px;color:#333\">\n" +
                "    <table style=\"width:100%;background-color:#f9f9f9;padding:20px;border-radius:10px;\">\n" +
                "        <tr>\n" +
                "            <td style=\"text-align:center;padding:15px 0;background-color:#0073e6;color:#fff;font-size:22px;font-weight:bold;\">\n" +
                "                Welcome to TeamUp! 🎉\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td style=\"padding:25px;background-color:#fff;border:1px solid #ddd;border-radius:10px;text-align:center;\">\n" +
                "                <p style=\"font-size:18px;margin-bottom:15px;\">Hey " + name + ",</p>\n" +
                "                <p style=\"font-size:16px;line-height:1.6;\">\n" +
                "                    You're just one step away from joining the ultimate sports matchmaking experience! 🏆<br>\n" +
                "                    Click the button below to verify your email and start teaming up with players near you. \n" +
                "                </p>\n" +
                "                <p style=\"text-align:center;margin:25px 0;\">\n" +
                "                    <a href=\"" + link + "\" style=\"background-color:#28a745;color:#fff;padding:12px 25px;text-decoration:none;\n" +
                "                        border-radius:8px;font-size:18px;font-weight:bold;display:inline-block;\">✅ Verify My Account</a>\n" +
                "                </p>\n" +
                "                <p style=\"font-size:14px;color:#777;margin-top:20px;\">\n" +
                "                    This link is valid for 30 minutes. Didn’t request this? Just ignore it. No worries! 😊\n" +
                "                </p>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td style=\"text-align:center;font-size:12px;color:#888;padding-top:20px;\">\n" +
                "                🔥 TeamUp - Find. Play. Win. | © 2024 All rights reserved.\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</div>";
    }
}
