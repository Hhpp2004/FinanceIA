package com.microservice.repository;

import com.microservice.models.FinanceEmailSender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailRepository extends JpaRepository<FinanceEmailSender, UUID> {
}
