package com.pollub.cookie.service;// src/main/java/com/pollub/cookie/service/NotificationService.java

import com.pollub.cookie.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void markAllAsReadForUser(String username) {
        notificationRepository.markAllAsReadForUser(username);
    }
}
