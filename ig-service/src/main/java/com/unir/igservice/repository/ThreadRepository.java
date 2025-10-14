package com.unir.igservice.repository;

import com.unir.common.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, UUID> {
    Optional<Thread> findByAccountIdAndExternalThreadId(UUID accountId, String externalThreadId);
}

