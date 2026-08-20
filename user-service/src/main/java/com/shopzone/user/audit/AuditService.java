package com.shopzone.user.audit;

import com.shopzone.user.api.dto.AuditLogResponse;
import com.shopzone.user.domain.AuditLogEntry;
import com.shopzone.user.repository.AuditLogRepository;
import com.shopzone.user.web.PageResponse;
import com.shopzone.user.web.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(UUID actorUserId, String action, String entityType, UUID entityId, Map<String, Object> metadata) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setActorUserId(actorUserId);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setMetadata(metadata);
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(Pageable pageable) {
        Page<AuditLogResponse> page = auditLogRepository.findAll(pageable).map(this::toResponse);
        return SecurityUtils.toPage(page);
    }

    private AuditLogResponse toResponse(AuditLogEntry entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getActorUserId(),
                entry.getAction(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getMetadata(),
                entry.getOccurredAt());
    }
}
