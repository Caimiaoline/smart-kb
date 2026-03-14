package com.example.smartkb.service;

import com.example.smartkb.domain.KnowledgeDocument;
import com.example.smartkb.domain.KnowledgeSegment;
import com.example.smartkb.repository.KnowledgeDocumentRepository;
import com.example.smartkb.repository.KnowledgeSegmentRepository;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 文档处理流水线：Load(Tika) → Split → Vectorize → Save。
 * 向量化异步执行，不阻塞上传接口。
 */
@Service
public class DocumentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorService.class);

    private static final int SPLIT_MAX_SIZE = 500;
    private static final int SPLIT_OVERLAP = 50;

    private final TikaParseService tikaParseService;
    private final EmbeddingManager embeddingManager;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeSegmentRepository segmentRepository;

    public DocumentProcessorService(TikaParseService tikaParseService,
                                    EmbeddingManager embeddingManager,
                                    KnowledgeDocumentRepository documentRepository,
                                    KnowledgeSegmentRepository segmentRepository) {
        this.tikaParseService = tikaParseService;
        this.embeddingManager = embeddingManager;
        this.documentRepository = documentRepository;
        this.segmentRepository = segmentRepository;
    }

    /**
     * 上传后异步执行：切片 → 向量化 → 落库。由 uploadAndProcess 调用，传入已解析的全文。
     */
    @Async
    @Transactional
    public CompletableFuture<Long> processUploadAsync(Long kbId, Long docId, String fullText) {
        KnowledgeDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalStateException("document not found: " + docId));
        if (doc.getKbId() == null || !doc.getKbId().equals(kbId)) {
            throw new IllegalArgumentException("doc not belong to kb: " + kbId);
        }
        doc.setStatus(KnowledgeDocument.DocumentStatus.PROCESSING);
        documentRepository.save(doc);

        try {
            if (fullText == null || fullText.isBlank()) {
                doc.setStatus(KnowledgeDocument.DocumentStatus.COMPLETED);
                doc.setTokenCount(0);
                documentRepository.save(doc);
                return CompletableFuture.completedFuture(docId);
            }
            DocumentSplitter splitter = DocumentSplitters.recursive(SPLIT_MAX_SIZE, SPLIT_OVERLAP);
            List<TextSegment> segments = splitter.split(dev.langchain4j.data.document.Document.from(fullText));
            List<String> contents = segments.stream()
                    .map(TextSegment::text)
                    .filter(s -> s != null && !s.isBlank())
                    .toList();
            if (contents.isEmpty()) {
                doc.setStatus(KnowledgeDocument.DocumentStatus.COMPLETED);
                doc.setTokenCount(0);
                documentRepository.save(doc);
                return CompletableFuture.completedFuture(docId);
            }
            List<float[]> vectors = embeddingManager.embed(contents);
            int orderNum = 0;
            List<KnowledgeSegment> toSave = new ArrayList<>();
            for (int i = 0; i < contents.size(); i++) {
                if (i >= vectors.size()) break;
                float[] vec = vectors.get(i);
                KnowledgeSegment segment = new KnowledgeSegment();
                segment.setDocId(docId);
                segment.setKbId(kbId);
                segment.setContent(contents.get(i));
                segment.setVector(vec);
                segment.setHitCount(0);
                segment.setOrderNum(orderNum++);
                toSave.add(segment);
            }
            segmentRepository.saveAll(toSave);
            doc.setStatus(KnowledgeDocument.DocumentStatus.COMPLETED);
            doc.setTokenCount(toSave.size());
            documentRepository.save(doc);
            log.info("kbId={} docId={} segments={}", kbId, docId, toSave.size());
            return CompletableFuture.completedFuture(docId);
        } catch (Exception e) {
            log.error("process doc failed kbId={} docId={}", kbId, docId, e);
            doc.setStatus(KnowledgeDocument.DocumentStatus.ERROR);
            documentRepository.save(doc);
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件：Tika 解析 → 创建文档记录 → 异步向量化（@Async）。返回文档记录，向量化在后台执行。
     */
    @Transactional
    public KnowledgeDocument uploadAndProcess(Long kbId, MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String fileType = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";

        String fullText;
        try (InputStream is = file.getInputStream()) {
            fullText = tikaParseService.parseToText(is, fileName);
        } catch (Exception e) {
            log.warn("tika parse failed: {}", fileName, e);
            throw new DocumentParseException("文件解析失败: " + e.getMessage(), e);
        }
        int charCount = fullText != null ? fullText.length() : 0;
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setKbId(kbId);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setStatus(fullText == null || fullText.isBlank()
                ? KnowledgeDocument.DocumentStatus.COMPLETED
                : KnowledgeDocument.DocumentStatus.PENDING);
        doc.setCharCount(charCount);
        doc.setTokenCount(fullText == null || fullText.isBlank() ? 0 : null);
        doc = documentRepository.save(doc);
        if (fullText != null && !fullText.isBlank()) {
            processUploadAsync(kbId, doc.getId(), fullText);
        }
        return doc;
    }

    public static class DocumentParseException extends RuntimeException {
        public DocumentParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
