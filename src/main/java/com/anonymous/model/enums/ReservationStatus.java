package com.anonymous.model.enums;

import java.util.Map;
import java.util.Set;

public enum ReservationStatus {

    PENDING(0, "待使用"),
    IN_USE(1, "使用中"),
    COMPLETED(2, "已完成"),

    USER_CANCELLED(3, "用户取消"),
    EXPIRED(4, "已违约"),
    ADMIN_CANCELLED(5, "管理员取消"),
    VIOLATED(6, "违规违约");

    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
        0, Set.of(1, 3, 4, 5, 6),
        1, Set.of(2, 6),
        2, Set.of(),
        3, Set.of(),
        4, Set.of(),
        5, Set.of(),
        6, Set.of()
    );

    private final int code;
    private final String description;

    ReservationStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ReservationStatus fromCode(int code) {
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的预约状态码: " + code);
    }

    public static boolean isTransitionAllowed(int fromCode, int toCode) {
        Set<Integer> allowedTargets = ALLOWED_TRANSITIONS.get(fromCode);
        return allowedTargets != null && allowedTargets.contains(toCode);
    }

    public static void validateTransition(int fromCode, int toCode) {
        if (!isTransitionAllowed(fromCode, toCode)) {
            ReservationStatus from = fromCode(fromCode);
            ReservationStatus to = fromCode(toCode);
            throw new IllegalStateException(
                String.format("预约状态转换非法: %s(%d) → %s(%d)",
                    from.description, from.code,
                    to.description, to.code)
            );
        }
    }

    public Set<Integer> getAllowedTargets() {
        Set<Integer> targets = ALLOWED_TRANSITIONS.get(this.code);
        return targets != null ? targets : Set.of();
    }

    public boolean isFinalStatus() {
        return getAllowedTargets().isEmpty();
    }
    
    public boolean isActiveStatus() {
        return this.code == PENDING.code || this.code == IN_USE.code;
    }
}
