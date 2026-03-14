package com.example.smartkb.controller;

import com.example.smartkb.domain.Application;
import com.example.smartkb.repository.ApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin
public class ApplicationController {

    private final ApplicationRepository applicationRepository;

    public ApplicationController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @GetMapping
    public List<Application> list() {
        return applicationRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Application> create(@RequestBody Application application) {
        if (application.getStatus() == null) {
            application.setStatus(Boolean.FALSE);
        }
        Application saved = applicationRepository.save(application);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * 绑定知识库：为智能体设置绑定的知识库 ID 列表，检索时从这些知识库捞数据。
     */
    @PutMapping("/{id}/bind-knowledge")
    public ResponseEntity<Application> bindKnowledge(
            @PathVariable Long id,
            @RequestBody List<Long> kbIds
    ) {
        Application app = applicationRepository.findById(id)
                .orElse(null);
        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        app.setBoundKbIds(kbIds != null ? kbIds : List.of());
        app = applicationRepository.save(app);
        return ResponseEntity.ok(app);
    }
}

