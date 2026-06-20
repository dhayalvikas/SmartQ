package com.smartq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.MediaType;

import java.util.Base64;

@Service
public class NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

    @Value("${mailgun.api.key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from.email}")
    private String fromEmail;

    // Send "your turn is coming" email
    public void sendTurnComingEmail(
            String toEmail, String customerName,
            String businessName, Integer tokenNumber,
            String counterName) {

        String subject = "Your turn is coming soon - "
                + businessName;

        String body = String.format(
                "Hi %s,\n\n" +
                        "Good news! Your turn is coming up soon at %s.\n\n" +
                        "Your Token: #%d\n" +
                        "Counter: %s\n\n" +
                        "Please head back to the counter now.\n\n" +
                        "Thank you for using SmartQ!\n" +
                        "Queue smarter, wait less.",
                customerName, businessName, tokenNumber, counterName
        );

        sendEmail(toEmail, subject, body);
    }

    // Send feedback request email
    public void sendFeedbackEmail(
            String toEmail, String customerName,
            String businessName, Long tokenId) {

        String subject = "How was your visit to " + businessName + "?";

        String body = String.format(
                "Hi %s,\n\n" +
                        "Thank you for visiting %s today!\n\n" +
                        "We would love to hear your feedback. " +
                        "Please rate your experience using your " +
                        "SmartQ token (#%d).\n\n" +
                        "Your feedback helps businesses improve their service.\n\n" +
                        "Thank you for using SmartQ!\n" +
                        "Queue smarter, wait less.",
                customerName, businessName, tokenId
        );

        sendEmail(toEmail, subject, body);
    }

    // Core email sending method using Mailgun API
    private void sendEmail(String to, String subject, String text) {
        try {
            String credentials = "api:" + apiKey;
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes());

            MultiValueMap<String, String> formData =
                    new LinkedMultiValueMap<>();
            formData.add("from", fromEmail);
            formData.add("to", to);
            formData.add("subject", subject);
            formData.add("text", text);

            WebClient.create()
                    .post()
                    .uri("https://api.mailgun.net/v3/"
                            + domain + "/messages")
                    .header("Authorization", "Basic "
                            + encodedCredentials)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(org.springframework.web.reactive.function.BodyInserters
                            .fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response ->
                            log.info("Email sent successfully to {}", to))
                    .doOnError(error ->
                            log.error("Email failed to {}: {}", to, error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Email error for {}: {}", to, e.getMessage());
        }
    }
}