package net.rutger.home.service;

import net.rutger.home.domain.WateringJobData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

public class EmailServiceTest {
    final EmailService emailService = new EmailService();

    @Mock
    JavaMailSender javaMailSender = mock(JavaMailSender.class);

    @Before
    public void init() {
        emailService.setEmailSender(javaMailSender);
        emailService.setEmailRecipients(Arrays.asList(new String[]{"email@test.test"}));
        emailService.setEmailSenderAddress("Sender@sender.sender");
        emailService.setInitialMillimeters(3);
    }

    @Test
    public void testEmail() {
        WateringJobData wateringJobData = new WateringJobData();
        wateringJobData.setMinutesLeft(10);
        wateringJobData.setMakkinkIndex(1.2);
        wateringJobData.setMinutesLeft(4);
        wateringJobData.setNumberOfMinutes(6);
        wateringJobData.setUpdatedOn(LocalDateTime.now());
        emailService.emailWateringResult(wateringJobData);
    }
}
