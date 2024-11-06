// src/main/java/com/pollub/cookie/repository/NotificationRepository.java

package com.pollub.cookie.repository;

import com.pollub.cookie.model.Notification;
import com.pollub.cookie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUzytkownikAndPrzeczytaneFalse(User uzytkownik);

    @Modifying
    @Query("UPDATE Notification n SET n.przeczytane = true WHERE n.uzytkownik.email = :username AND n.przeczytane = false")
    void markAllAsReadForUser(@Param("username") String username);
}
