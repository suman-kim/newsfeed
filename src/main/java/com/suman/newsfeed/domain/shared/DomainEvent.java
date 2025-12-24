package com.suman.newsfeed.domain.shared;

import java.time.LocalDateTime;
public interface DomainEvent {

    // 🆔 이벤트 고유 ID
    String getEventId();

    // ⏰ 이벤트 발생 시간
    LocalDateTime getOccurredOn();

    // 🏷️ 이벤트 타입 (클래스명)
    String getEventType();

    // 🎯 도메인 ID (어그리게이트 ID)
    String getDomainId();

    // 📝 이벤트 버전 (스키마 버전)
    default Integer getVersion() {
        return 1;
    }

}
