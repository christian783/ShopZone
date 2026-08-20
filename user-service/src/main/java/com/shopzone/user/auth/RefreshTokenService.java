package com.shopzone.user.auth;

import com.shopzone.user.config.JwtProperties;
import com.shopzone.user.domain.RefreshToken;
import com.shopzone.user.domain.User;
import com.shopzone.user.repository.RefreshTokenRepository;
import com.shopzone.user.web.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    public record IssuedRefresh(String plaintext, RefreshToken entity) {
    }

    public record RotationResult(User user, IssuedRefresh refresh) {
    }

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    public IssuedRefresh issue(User user) {
        return issue(user, UUID.randomUUID());
    }

    public IssuedRefresh issue(User user, UUID familyId) {
        String plaintext = TokenHasher.randomToken();
        RefreshToken token = persist(user, familyId, plaintext);
        return new IssuedRefresh(plaintext, token);
    }

    public RotationResult rotate(String plaintextToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(TokenHasher.sha256(plaintextToken))
                .orElseThrow(() -> BusinessException.unauthorized("auth.refresh.invalid"));
        if (existing.isRevokedOrReplaced()) {
            refreshTokenRepository.revokeFamily(existing.getFamilyId(), Instant.now());
            throw BusinessException.unauthorized("auth.refresh.reused");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.unauthorized("auth.refresh.expired");
        }
        IssuedRefresh replacement = issue(existing.getUser(), existing.getFamilyId());
        existing.setRevokedAt(Instant.now());
        existing.setReplacedBy(replacement.entity().getId());
        return new RotationResult(existing.getUser(), replacement);
    }

    public void revokeFamilyByToken(String plaintextToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(TokenHasher.sha256(plaintextToken))
                .orElseThrow(() -> BusinessException.unauthorized("auth.refresh.invalid"));
        refreshTokenRepository.revokeFamily(token.getFamilyId(), Instant.now());
    }

    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, Instant.now());
    }

    private RefreshToken persist(User user, UUID familyId, String plaintext) {
        Instant now = Instant.now();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(TokenHasher.sha256(plaintext));
        token.setFamilyId(familyId);
        token.setIssuedAt(now);
        token.setExpiresAt(now.plus(jwtProperties.getRefreshTokenTtl()));
        return refreshTokenRepository.save(token);
    }
}
