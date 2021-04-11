package net.rutger.home.service;

import net.rutger.home.domain.WateringJobData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

public class EmailServiceTest {
    final EmailService emailService = new EmailService();

    @Mock
    JavaMailSender javaMailSender = mock(JavaMailSender.class);

    @BeforeEach
    public void init() {
        emailService.setEmailSender(javaMailSender);
        emailService.setEmailRecipients(Arrays.asList(new String[]{"email@test.test"}));
        emailService.setEmailSenderAddress("Sender@sender.sender");
    }

    @Test
    public void testEmail() {
        WateringJobData wateringJobData = new WateringJobData();
        wateringJobData.setMinutesLeftUpper(10);
        wateringJobData.setMinutesLeftLower(5);
        wateringJobData.setMinutesLeftUpper(4);
        wateringJobData.setMinutesLeftLower(2);
        wateringJobData.setNextRun(LocalDateTime.now());
        emailService.emailWateringResult(wateringJobData);
    }
}
