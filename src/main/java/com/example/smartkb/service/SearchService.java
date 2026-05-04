package com.example.smartkb.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 检索服务：Query Rewrite → 混合召回（向量 + 关键词）→ 轻量 Rerank → 阈值过滤。
 * 向量召回负责语义相似，关键词召回负责术语、数字、错误码等精确命中。
 */
@Service
public class SearchService {

    private static final int DEFAULT_CANDIDATE_LIMIT = 100;

    private final EmbeddingManager embeddingManager;
    private final JdbcTemplate jdbcTemplate;

    public SearchService(EmbeddingManager embeddingManager, JdbcTemplate jdbcTemplate) {
        this.embeddingManager = embeddingManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 命中率测试 / 检索：返回切片内容及相似度分数（1 - 余弦距离）。
     *
     * @param queryText 查询文本
     * @param kbId      知识库 ID
     * @param topN      返回条数
     * @param threshold 最低相似度（0~1），低于此值的结果过滤掉
     */
    public List<SegmentScore> search(String queryText, Long kbId, int topN, double threshold) {
        String rewrittenQuery = rewriteQuery(queryText);
        float[] queryVector = embeddingManager.embed(rewrittenQuery);
        String vectorStr = toVectorLiteral(queryVector);

        String sql = "SELECT ks.id, ks.doc_id, ks.content, ks.order_num, kd.file_name, " +
                "(1 - (ks.vector <=> '" + vectorStr + "'::vector)) AS score " +
                "FROM knowledge_segment ks " +
                "LEFT JOIN knowledge_document kd ON ks.doc_id = kd.id " +
                "WHERE ks.kb_id = ? " +
                "ORDER BY ks.vector <=> '" + vectorStr + "'::vector LIMIT ?";

        List<SegmentScore> vectorCandidates = jdbcTemplate.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            long docId = rs.getLong("doc_id");
            String fileName = rs.getString("file_name");
            String content = rs.getString("content");
            Integer orderNum = rs.getObject("order_num", Integer.class);
            double score = rs.getDouble("score");
            return new SegmentScore(id, docId, fileName, orderNum, content, score, 0.0, rewrittenQuery, "vector", buildCitation(kbId, docId, fileName, orderNum));
        }, kbId, Math.max(topN, DEFAULT_CANDIDATE_LIMIT));

        List<SegmentScore> keywordCandidates = keywordRecall(kbId, rewrittenQuery, Math.max(topN, DEFAULT_CANDIDATE_LIMIT));
        List<SegmentScore> mergedCandidates = mergeCandidates(vectorCandidates, keywordCandidates);
        return rerankAndFilter(mergedCandidates, queryText, rewrittenQuery, topN, threshold);
    }

    private List<SegmentScore> keywordRecall(Long kbId, String rewrittenQuery, int limit) {
        Set<String> queryTerms = extractKeywords(rewrittenQuery);
        if (queryTerms.isEmpty()) {
            return List.of();
        }

        String whereLike = queryTerms.stream()
                .limit(6)
                .map(term -> "LOWER(ks.content) LIKE ?")
                .collect(Collectors.joining(" OR "));
        String sql = "SELECT ks.id, ks.doc_id, ks.content, ks.order_num, kd.file_name " +
                "FROM knowledge_segment ks " +
                "LEFT JOIN knowledge_document kd ON ks.doc_id = kd.id " +
                "WHERE ks.kb_id = ? AND (" + whereLike + ") LIMIT ?";

        List<Object> params = new ArrayList<>();
        params.add(kbId);
        queryTerms.stream().limit(6).forEach(term -> params.add("%" + term.toLowerCase() + "%"));
        params.add(limit);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            long docId = rs.getLong("doc_id");
            String fileName = rs.getString("file_name");
            String content = rs.getString("content");
            Integer orderNum = rs.getObject("order_num", Integer.class);
            double keywordScore = lexicalOverlapScore(content, queryTerms);
            return new SegmentScore(id, docId, fileName, orderNum, content, 0.0, keywordScore, rewrittenQuery, "keyword", buildCitation(kbId, docId, fileName, orderNum));
        }, params.toArray());
    }

    private List<SegmentScore> mergeCandidates(List<SegmentScore> vectorCandidates, List<SegmentScore> keywordCandidates) {
        Map<Long, SegmentScore> merged = new LinkedHashMap<>();
        for (SegmentScore candidate : vectorCandidates) {
            merged.put(candidate.segmentId(), candidate);
        }
        for (SegmentScore candidate : keywordCandidates) {
            SegmentScore existing = merged.get(candidate.segmentId());
            if (existing == null) {
                merged.put(candidate.segmentId(), candidate);
            } else {
                merged.put(candidate.segmentId(), existing.withRecallSource("hybrid"));
            }
        }
        return new ArrayList<>(merged.values());
    }

    private List<SegmentScore> rerankAndFilter(List<SegmentScore> candidates,
                                                String originalQuery,
                                                String rewrittenQuery,
                                                int topN,
                                                double threshold) {
        Set<String> queryTerms = extractKeywords(rewrittenQuery.isBlank() ? originalQuery : rewrittenQuery);
        List<SegmentScore> reranked = new ArrayList<>();

        for (SegmentScore candidate : candidates) {
            double lexicalBoost = lexicalOverlapScore(candidate.content(), queryTerms);
            double coverageBoost = coverageScore(candidate.content(), queryTerms);
            double sourceBoost = "hybrid".equals(candidate.recallSource()) ? 0.08 : 0.0;
            double finalScore = candidate.vectorScore() * 0.65 + lexicalBoost * 0.22 + coverageBoost * 0.13 + sourceBoost;
            reranked.add(candidate.withFinalScore(Math.min(finalScore, 1.0)));
        }

        return reranked.stream()
                .filter(s -> s.finalScore() >= threshold)
                .sorted(Comparator.comparingDouble(SegmentScore::finalScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    private String rewriteQuery(String queryText) {
        if (queryText == null) {
            return "";
        }
        String cleaned = queryText.trim().replaceAll("\\s+", " ");
        if (cleaned.isBlank()) {
            return "";
        }
        String lower = cleaned.toLowerCase();
        StringBuilder rewritten = new StringBuilder(cleaned);

        if (lower.contains("怎么") || lower.contains("如何") || lower.contains("怎样")) {
            rewritten.append(" 操作步骤 方法 说明");
        }
        if (lower.contains("原因") || lower.contains("为什么")) {
            rewritten.append(" 原理 解释 机制");
        }
        if (lower.contains("报错") || lower.contains("失败") || lower.contains("异常")) {
            rewritten.append(" 错误 处理 解决方案");
        }
        if (lower.contains("配置") || lower.contains("参数") || lower.contains("设置")) {
            rewritten.append(" 配置项 参数 示例");
        }
        if (lower.contains("文档") || lower.contains("知识库") || lower.contains("上传")) {
            rewritten.append(" 资料 文本 切片 检索");
        }
        return rewritten.toString();
    }

    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        String normalized = text.toLowerCase().replaceAll("[^\\p{IsHan}a-z0-9]+", " ");
        Set<String> keywords = new LinkedHashSet<>();
        for (String token : normalized.split("\\s+")) {
            if (token == null) {
                continue;
            }
            String trimmed = token.trim();
            if (trimmed.length() >= 2) {
                keywords.add(trimmed);
            }
        }
        return keywords;
    }

    private double lexicalOverlapScore(String content, Set<String> queryTerms) {
        if (content == null || content.isBlank() || queryTerms.isEmpty()) {
            return 0.0;
        }
        String lowerContent = content.toLowerCase();
        long hitCount = queryTerms.stream().filter(lowerContent::contains).count();
        return queryTerms.isEmpty() ? 0.0 : (double) hitCount / queryTerms.size();
    }

    private double coverageScore(String content, Set<String> queryTerms) {
        if (content == null || content.isBlank() || queryTerms.isEmpty()) {
            return 0.0;
        }
        String lowerContent = content.toLowerCase();
        int covered = 0;
        for (String term : queryTerms) {
            if (lowerContent.contains(term)) {
                covered++;
            }
        }
        return (double) covered / queryTerms.size();
    }

    private String buildCitation(Long kbId, long docId, String fileName, Integer orderNum) {
        String safeFileName = (fileName == null || fileName.isBlank()) ? "unknown" : fileName;
        String safeOrder = orderNum == null ? "?" : String.valueOf(orderNum);
        return "知识库#" + kbId + " / 文档#" + docId + " / " + safeFileName + " / chunk-" + safeOrder;
    }

    private static String toVectorLiteral(float[] v) {
        if (v == null || v.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(v[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    public record SegmentScore(long segmentId,
                               long docId,
                               String fileName,
                               Integer orderNum,
                               String content,
                               double vectorScore,
                               double finalScore,
                               String rewrittenQuery,
                               String recallSource,
                               String citation) {
        public SegmentScore withFinalScore(double newFinalScore) {
            return new SegmentScore(segmentId, docId, fileName, orderNum, content, vectorScore, newFinalScore, rewrittenQuery, recallSource, citation);
        }

        public SegmentScore withRecallSource(String newRecallSource) {
            return new SegmentScore(segmentId, docId, fileName, orderNum, content, vectorScore, finalScore, rewrittenQuery, newRecallSource, citation);
        }
    }
}
