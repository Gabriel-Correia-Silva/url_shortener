package com.shortener_url.repository;

import com.shortener_url.entities.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, String> {
    
    
    @Query("SELECT u FROM UrlEntity u WHERE u.expiresAt > :now")
    List<UrlEntity> findAllActiveUrls(@Param("now") LocalDateTime now);
    
    
    @Query("SELECT u FROM UrlEntity u WHERE u.expiresAt <= :now")
    List<UrlEntity> findAllExpiredUrls(@Param("now") LocalDateTime now);
    
   
    @Query("DELETE FROM UrlEntity u WHERE u.expiresAt <= :now")
    void deleteExpiredUrls(@Param("now") LocalDateTime now);
    
    
    Optional<UrlEntity> findByFullUrl(String fullUrl);
}