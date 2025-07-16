package com.shortener_url.shortener_url_java_spring.Controller;

import java.net.URI;
import java.time.LocalDateTime;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shortener_url.entities.UrlEntity;
import com.shortener_url.repository.UrlRepository;
import com.shortener_url.shortener_url_java_spring.Controller.dto.ShortenUrlResponse;
import com.shortener_url.shortener_url_java_spring.Controller.dto.ShortenUrlRequest;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlRepository urlRepository;

    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @PostMapping(value = "/shorten-url")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@RequestBody ShortenUrlRequest request,
                                                         HttpServletRequest servletRequest) {
            if (request.url() == null || request.url().trim().isEmpty()) {
    
                return ResponseEntity.badRequest().build();
        }

        String id;
        do {
            id = RandomStringUtils.randomAlphanumeric(5, 10);
        } while (urlRepository.existsById(id));

    
        UrlEntity urlEntity = new UrlEntity(id, request.url(), LocalDateTime.now().plusMinutes(120));
        urlRepository.save(urlEntity);

        
        String baseUrl = servletRequest.getRequestURL().toString().replace("/api/shorten-url", "");
        String redirectUrl = baseUrl + "/" + id;

        return ResponseEntity.ok(new ShortenUrlResponse(redirectUrl));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> redirect(@PathVariable("id") String id) {
        
        var urlOptional = urlRepository.findById(id);

        if (urlOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UrlEntity urlEntity = urlOptional.get();
        
    
        if (urlEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            urlRepository.delete(urlEntity);
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(urlEntity.getFullUrl()));

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @GetMapping("/api/stats/{id}")
    public ResponseEntity<UrlEntity> getUrlStats(@PathVariable("id") String id) {
        var urlOptional = urlRepository.findById(id);
        
        if (urlOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(urlOptional.get());
    }
}