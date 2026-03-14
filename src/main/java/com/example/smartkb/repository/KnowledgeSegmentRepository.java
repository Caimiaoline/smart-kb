package com.example.smartkb.repository;

import com.example.smartkb.domain.KnowledgeSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeSegmentRepository extends JpaRepository<KnowledgeSegment, Long> {
}
