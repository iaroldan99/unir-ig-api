package com.unir.apigateway.repository;

import com.unir.common.entity.Thread;
import com.unir.common.model.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, UUID> {
    
    Page<Thread> findByChannelAndAccountIdOrderByLastMessageAtDesc(
            Channel channel, UUID accountId, Pageable pageable);
    
    Page<Thread> findByAccountIdOrderByLastMessageAtDesc(UUID accountId, Pageable pageable);
    
    Page<Thread> findByChannelOrderByLastMessageAtDesc(Channel channel, Pageable pageable);
    
    Page<Thread> findAllByOrderByLastMessageAtDesc(Pageable pageable);
    
    @Query("SELECT t FROM Thread t WHERE " +
           "LOWER(t.externalThreadId) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Thread> searchThreads(@Param("query") String query, Pageable pageable);
}

