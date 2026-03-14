package com.example.smartkb.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量检索：query 向量化后，在库内按余弦相似度排序并过滤阈值。
 * 检索在数据库层面完成，不将全量向量拉入内存。
 */
@Service
public class SearchService {

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
        float[] queryVector = embeddingManager.embed(queryText);
        String vectorStr = toVectorLiteral(queryVector);

        // pgvector: <=> 为余弦距离，1 - 距离 = 相似度。按距离升序 LIMIT 取前 topN，再在应用层按 threshold 过滤。
        String sql = "SELECT id, content, (1 - (vector <=> '" + vectorStr + "'::vector)) AS score " +
                "FROM knowledge_segment WHERE kb_id = ? ORDER BY vector <=> '" + vectorStr + "'::vector LIMIT ?";

        List<SegmentScore> raw = jdbcTemplate.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String content = rs.getString("content");
            double score = rs.getDouble("score");
            return new SegmentScore(id, content, score);
        }, kbId, Math.max(topN, 100));

        return raw.stream()
                .filter(s -> s.score() >= threshold)
                .limit(topN)
                .collect(Collectors.toList());
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

    public record SegmentScore(long segmentId, String content, double score) {}
}
