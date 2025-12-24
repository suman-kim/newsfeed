package com.suman.newsfeed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.suite.api.IncludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NewsfeedApplication 전체 테스트
 */
@Suite
@SelectPackages({"com.suman.newsfeed.application.usecase", "com.suman.newsfeed"
})
@IncludePackages({"com.suman.newsfeed.application.usecase", "com.suman.newsfeed"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("NewsfeedApplication 전체 테스트 스위트")
class NewsfeedApplicationTests {
    @Test
    void contextLoads() {
    }
}
