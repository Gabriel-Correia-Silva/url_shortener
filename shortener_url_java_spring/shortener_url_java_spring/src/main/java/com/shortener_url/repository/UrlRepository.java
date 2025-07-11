package com.shortener_url.repository;

import com.shortener_url.entities.UrlEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository; 

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, String> {
}