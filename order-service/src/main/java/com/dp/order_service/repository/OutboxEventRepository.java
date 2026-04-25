package com.dp.order_service.repository;

import com.dp.order_service.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.published = false ORDER BY o.createdAt ASC")
    List<OutboxEvent> findUnpublishedEvents();

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.published = true, o.publishedAt = :now WHERE o.id = :id")
    void markAsPublished(@Param("id") Long id, @Param("now") LocalDateTime now);

}
