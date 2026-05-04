package com.example.smartkb.controller;

import com.example.smartkb.domain.KnowledgeBase;
import com.example.smartkb.domain.KnowledgeDocument;
import com.example.smartkb.dto.HitTestRequest;
import com.example.smartkb.dto.KnowledgeBaseCreateRequest;
import com.example.smartkb.repository.KnowledgeBaseRepository;
import com.example.smartkb.repository.KnowledgeDocumentRepository;
import com.example.smartkb.service.DocumentProcessorService;
import com.example.smartkb.service.SearchService;
import com.example.smartkb.service.SearchService.SegmentScore;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin
public class KnowledgeController {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentProcessorService documentProcessorService;
    private final SearchService searchService;

    public KnowledgeController(KnowledgeBaseRepository knowledgeBaseRepository,
                               KnowledgeDocumentRepository documentRepository,
                               DocumentProcessorService documentProcessorService,
                               SearchService searchService) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.documentRepository = documentRepository;
        this.documentProcessorService = documentProcessorService;
        this.searchService = searchService;
    }

    @GetMapping
    public List<KnowledgeBase> listAll() {
        return knowledgeBaseRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<KnowledgeBase> create(@Valid @RequestBody KnowledgeBaseCreateRequest request) {
        KnowledgeBase kb = request.toEntity();
        kb = knowledgeBaseRepository.save(kb);
        return ResponseEntity.status(HttpStatus.CREATED).body(kb);
    }

    @PostMapping(value = "/{kbId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KnowledgeDocument> upload(
            @PathVariable Long kbId,
            @RequestPart("file") MultipartFile file
    ) {
        if (!knowledgeBaseRepository.existsById(kbId)) {
            return ResponseEntity.notFound().build();
        }
        KnowledgeDocument doc = documentProcessorService.uploadAndProcess(kbId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @GetMapping("/{kbId}/documents")
    public List<KnowledgeDocument> listDocuments(@PathVariable Long kbId) {
        return documentRepository.findByKbIdOrderByIdDesc(kbId);
    }

    @PostMapping("/{kbId}/hit-test")
    public Map<String, Object> hitTest(
            @PathVariable Long kbId,
            @Valid @RequestBody HitTestRequest request
    ) {
        if (!knowledgeBaseRepository.existsById(kbId)) {
            return Map.of("error", "knowledge base not found", "results", List.<SegmentScore>of());
        }
        List<SegmentScore> results = searchService.search(
                request.queryText(),
                kbId,
                request.topN(),
                request.threshold()
        );
        return Map.of(
                "kbId", kbId,
                "queryText", request.queryText(),
                "results", results,
                "count", results.size(),
                "retrievalMode", "query-rewrite + hybrid-recall(vector+keyword) + lightweight-rerank + citation",
                "citationEnabled", true
        );
    }
}
