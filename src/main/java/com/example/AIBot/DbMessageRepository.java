package com.example.AIBot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DbMessageRepository extends JpaRepository<DbMessage, Long> {
}
