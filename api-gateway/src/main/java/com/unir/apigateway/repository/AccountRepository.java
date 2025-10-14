package com.unir.apigateway.repository;

import com.unir.common.entity.Account;
import com.unir.common.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    List<Account> findByUserIdAndChannel(UUID userId, Channel channel);
    
    Optional<Account> findByUserIdAndChannelAndStatus(UUID userId, Channel channel, String status);
    
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.channel = :channel AND " +
           "jsonb_extract_path_text(CAST(a.externalIds AS string), 'ig_user_id') = :igUserId")
    Optional<Account> findByUserIdAndChannelAndIgUserId(
        @Param("userId") UUID userId, 
        @Param("channel") Channel channel, 
        @Param("igUserId") String igUserId
    );
    
    List<Account> findByUserId(UUID userId);
}

