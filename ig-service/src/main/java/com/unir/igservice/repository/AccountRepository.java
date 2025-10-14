package com.unir.igservice.repository;

import com.unir.common.entity.Account;
import com.unir.common.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByChannelAndExternalIds(Channel channel, String externalId);
}

