package net.rutger.home.service;

import net.rutger.home.domain.WateringJobData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service
public class EmailService {
    private final static Logger LOG = LoggerFactory.getLogger(EmailService.class);

    @Value("${watering.initial.mm}")
    private int initialMillimeters;

    @Value("${email.sender}")
    private String emailSenderAddress;

    @Value("#{'${email.recipients}'.split(',')}")
    private List<String> emailRecipients;

    @Autowired
    private JavaMailSender emailSender;

    public void emailWateringResult(final WateringJobData wateringJobData) {
        LOG.debug("Sending email with watering results.");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Tuinsproeier <"+emailSenderAddress+">");
        message.setTo(emailRecipients.toArray(new String[emailRecipients.size()]));
        message.setSubject("Tuinsproeier dagrapport");
        if (wateringJobData.getNumberOfMinutes() == 0) {
            message.setText(String.format("Er wordt vandaag NIET gesproeid.\n" +
                    "De hoeveelheid neerslag was groter of gelijk aan de verdamping van het water vermeerderd met %d mm.\n" +
                            "Sproei data:\n\n%s",
                    initialMillimeters, wateringJobData));
        } else {
            if(wateringJobData.getMakkinkIndex() == null) {
                message.setText(String.format("De KNMI data is vandaag helaas niet succesvol ontvangen. " +
                        "Er is gekozen voor het sproeien gedurende de standaard tijdsduur.\n\n" +
                        "Hieronder volgt de sproeidata:" +
                        "\n%s",
                        wateringJobData.toString()));
            } else {
                final DecimalFormat df = new DecimalFormat("###.##");
                message.setText(String.format("De tuinsproeier is zojuist geactiveerd met de volgende gegevens:\n" +
                                "%d mm initiele wateraanvulling, plus\n" +
                                "%s mm verdamping, minus\n" +
                                "%s mm neerslag. Totaal\n" +
                                "%s mm aanvullen, resulterend in %d minuten sproeien.\n\n",
                                "Hieronder volgt de sproeidata:" +
                                "\n%s",
                        initialMillimeters,
                        df.format(wateringJobData.getMakkinkIndex()),
                        df.format(wateringJobData.getPrecipitation()),
                        df.format((initialMillimeters + wateringJobData.getMakkinkIndex()) - wateringJobData.getPrecipitation()),
                        wateringJobData.getNumberOfMinutes(),
                        wateringJobData.toString()));
            }
        }
        emailSender.send(message);
        LOG.debug("Email sent");
    }

}
