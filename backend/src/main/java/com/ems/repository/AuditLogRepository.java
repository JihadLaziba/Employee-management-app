package com.ems.repository;

import com.ems.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    @Query("""
            SELECT COUNT(a) FROM AuditLog a
            WHERE a.createdAt > :since
              AND (a.actorId IS NULL OR a.actorId <> :userId)
            """)
    long countUnread(@Param("since") LocalDateTime since, @Param("userId") Long userId);
}
