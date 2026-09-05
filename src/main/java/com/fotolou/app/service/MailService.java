package com.fotolou.app.service;

import com.fotolou.app.domain.User;

/**
 * Interface de contrat pour l'envoi d'emails transactionnels.
 */
public interface MailService {
    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);

    void sendEmailFromTemplate(User user, String templateName, String titleKey);

    void sendActivationEmail(User user);

    void sendCreationEmail(User user);

    void sendPasswordResetMail(User user);
}
