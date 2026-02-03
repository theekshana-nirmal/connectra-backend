package uwu.connectra.connectra_backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails.
 * Uses Spring Mail with async processing for non-blocking email sending.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends an OTP verification email to the specified address.
     * This method is async to avoid blocking the registration flow.
     */
    @Async
    public void sendOtpEmail(String toEmail, String otp, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your Connectra account - OTP Code");

            String htmlContent = buildOtpEmailTemplate(otp, firstName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}. Error: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Builds an HTML email template for OTP verification.
     */
    private String buildOtpEmailTemplate(String otp, String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #0d9488 0%%, #115e59 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1 style="color: white; margin: 0; font-size: 28px;">Connectra</h1>
                            <p style="color: rgba(255,255,255,0.9); margin: 10px 0 0 0;">University Online Meeting Platform</p>
                        </div>
                        <div style="background: white; padding: 40px 30px; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                            <h2 style="color: #1e293b; margin: 0 0 20px 0;">Hi %s,</h2>
                            <p style="color: #475569; line-height: 1.6; margin: 0 0 25px 0;">
                                Welcome to Connectra! To complete your registration, please use the following verification code:
                            </p>
                            <div style="background: #f0fdfa; border: 2px dashed #0d9488; border-radius: 10px; padding: 25px; text-align: center; margin: 0 0 25px 0;">
                                <span style="font-size: 36px; font-weight: bold; color: #0d9488; letter-spacing: 8px;">%s</span>
                            </div>
                            <p style="color: #475569; line-height: 1.6; margin: 0 0 15px 0;">
                                This code will expire in <strong>10 minutes</strong>.
                            </p>
                            <p style="color: #94a3b8; font-size: 14px; line-height: 1.6; margin: 0;">
                                If you didn't request this code, please ignore this email. Someone might have entered your email address by mistake.
                            </p>
                        </div>
                        <div style="text-align: center; padding: 20px; color: #94a3b8; font-size: 12px;">
                            <p style="margin: 0;">© 2026 Connectra - Uva Wellassa University</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(firstName, otp);
    }
}
