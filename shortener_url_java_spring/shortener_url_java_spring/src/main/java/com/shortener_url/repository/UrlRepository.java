package com.shortener_url.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.shortener_url.entities.UrlEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, String> {
}