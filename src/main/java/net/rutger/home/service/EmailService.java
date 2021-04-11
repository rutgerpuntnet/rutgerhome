package net.rutger.home.service;

import lombok.Setter;
import net.rutger.home.domain.WateringJobData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Setter
public class EmailService {
    private final static Logger LOG = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.sender}")
    private String emailSenderAddress;

    @Value("#{'${email.recipients}'.split(',')}")
    private List<String> emailRecipients;

    @Autowired
    private JavaMailSender emailSender;

    private LocalDate lastErrorMail = LocalDate.now().minusDays(1);

    public void emailWateringResult(final WateringJobData wateringJobData) {
        LOG.debug("Sending email with watering results.");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Terrassproeier <"+emailSenderAddress+">");
        message.setTo(emailRecipients.toArray(new String[emailRecipients.size()]));
        String subject = "Terrassproeier sproeit vandaag ";
        if (wateringJobData.getNumberOfMinutesUpper() + wateringJobData.getNumberOfMinutesLower() == 0) {
            subject += "NIET";
        } else {
            if (wateringJobData.getMakkinkIndex() == null) {
                subject += "de STANDAARD ";
            }
            subject += wateringJobData.getNumberOfMinutesUpper() + "/" + wateringJobData.getNumberOfMinutesLower() + " minuten";
        }
        message.setSubject(subject);

        String text = "De volgende gegevens van de 'job' zijn van toepassing:\n\n";
        if(wateringJobData.getMakkinkIndex() == null) {
            text += "LET OP, de KNMI data is vandaag niet op tijd ontvangen!\n\n";
        }
        text += wateringJobData.toString();
        message.setText(text);

        emailSender.send(message);
        LOG.debug("Email sent");
    }


    public void emailArduinoException(final RuntimeException e) {
        // Don't send the arduino error mail more then once a day.
        if(LocalDate.now().isAfter(lastErrorMail)) {
            LOG.debug("Sending email with arduino exception.");
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Tuinsproeier <"+emailSenderAddress+">");
            message.setTo(emailRecipients.toArray(new String[emailRecipients.size()]));
            message.setSubject("Tuinsproeier exception occurred!");
            message.setText("Er is een exceptie opgetreden tijdens het aanroepen van de tuin-arduino.\n" +
                    "Het sproeien is niet gelukt!\n\nFoutmelding:" + e.getMessage());
            emailSender.send(message);
            lastErrorMail = LocalDate.now();
        }
    }
}
