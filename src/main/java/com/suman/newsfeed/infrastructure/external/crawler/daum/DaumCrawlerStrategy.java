package com.suman.newsfeed.infrastructure.external.crawler.daum;

import com.suman.newsfeed.infrastructure.external.crawler.CrawlerStrategy;
import com.suman.newsfeed.infrastructure.external.crawler.NewsDataDto;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component
public class DaumCrawlerStrategy implements CrawlerStrategy {

    @Value("${daum.enabled:true}")
    private Boolean enabled;

    @Value("${daum.timeout:15000}")
    private Integer timeout;

    @Value("${daum.headless:true}")
    private Boolean headless;

    // 다음 뉴스 검색 URL 패턴
    private static final String DAUM_NEWS_SEARCH_URL = "https://search.daum.net/search?nil_suggest=btn&w=news&DA=SBC&cluster=y&q=%s";


    @Override
    public NewsPlatform getPlatform() {
        return NewsPlatform.DAUM;
    }

    @Override
    public List<NewsDataDto> crawlNews(String keyword, Long pageNumber, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("키워드가 비어있습니다.");
            return new ArrayList<>();
        }

        WebDriver driver = null;
        try {
            log.info("다음 뉴스 크롤링 시작 (Selenium) - 키워드: {}, 페이지: {}, 크기: {}", keyword, pageNumber, pageSize);

            // WebDriver 초기화
            driver = initializeWebDriver();

            // 검색 URL 생성 및 접속
            String url = buildSearchUrl(keyword);
            log.debug("크롤링 URL: {}", url);

            driver.get(url);

            // 페이지 로딩 대기
            waitForPageLoad(driver);

            // 뉴스 아이템 파싱
            List<NewsDataDto> newsList = parseNewsItemsWithSelenium(driver, keyword, pageSize);

            log.info("다음 뉴스 크롤링 완료 - 키워드: {}, 수집된 뉴스: {}개", keyword, newsList.size());
            return newsList;

        } catch (Exception e) {
            log.error("다음 뉴스 크롤링 중 오류 발생 - 키워드: {}, 오류: {}", keyword, e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            // WebDriver 정리
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("WebDriver 종료 중 오류: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * WebDriver 초기화 (WebDriverManager 사용)
     */
    private WebDriver initializeWebDriver() {
        try {
            // Chrome WebDriver 자동 다운로드 및 설정
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();

            // 헤드리스 모드 설정
            if (headless) {
                options.addArguments("--headless");
            }

            // 성능 최적화 옵션
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            // 추가 설정
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            WebDriver driver = new ChromeDriver(options);

            // JavaScript 실행으로 자동화 감지 방지
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            log.debug("WebDriver 초기화 완료");
            return driver;

        } catch (Exception e) {
            log.error("WebDriver 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("WebDriver 초기화 실패", e);
        }
    }

    /**
     * 검색 URL 생성
     */
    private String buildSearchUrl(String keyword) throws Exception {
        String encodedKeyword = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        return String.format(DAUM_NEWS_SEARCH_URL, encodedKeyword);
    }

    /**
     * 페이지 로딩 대기
     */
    private void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 페이지 로딩 완료 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // 추가 대기 (JavaScript 실행 완료 대기)
            Thread.sleep(3000);

            log.debug("페이지 로딩 완료");
        } catch (Exception e) {
            log.warn("페이지 로딩 대기 중 오류: {}", e.getMessage());
        }
    }

    /**
     * Selenium을 사용한 뉴스 아이템 파싱
     */
    private List<NewsDataDto> parseNewsItemsWithSelenium(WebDriver driver, String keyword, int maxItems) {
        List<NewsDataDto> newsList = new ArrayList<>();

        try {
            // 다음 뉴스 검색 결과 컨테이너 (여러 선택자 시도)
            List<By> selectors = List.of(
                    By.cssSelector("li"),
                    By.cssSelector(".wrap_cont"),
                    By.cssSelector("[class*='news']"),
                    By.cssSelector(".c-item"),
                    By.cssSelector(".item")
            );

            List<WebElement> newsContainers = new ArrayList<>();
            String usedSelector = null;

            for (By selector : selectors) {
                try {
                    List<WebElement> elements = driver.findElements(selector);
                    if (!elements.isEmpty()) {
                        newsContainers = elements;
                        usedSelector = selector.toString();
                        break;
                    }
                } catch (Exception e) {
                    log.debug("선택자 {} 실패: {}", selector, e.getMessage());
                }
            }

            if (newsContainers.isEmpty()) {
                log.warn("뉴스 컨테이너를 찾을 수 없습니다. 페이지 소스 일부: {}",
                        driver.getPageSource().substring(0, Math.min(1000, driver.getPageSource().length())));
                return newsList;
            }

            log.info("선택자 '{}'로 {}개의 뉴스 컨테이너를 찾았습니다", usedSelector, newsContainers.size());

            // 디버깅: 첫 번째 컨테이너의 HTML 구조 상세 분석
            if (!newsContainers.isEmpty()) {
                WebElement firstContainer = newsContainers.get(0);
                String containerHtml = firstContainer.getAttribute("outerHTML");
                log.info("첫 번째 컨테이너 HTML (전체): {}", containerHtml);

                // 컨테이너 내부의 모든 요소 분석
                analyzeContainerStructure(firstContainer);
            }

            int itemCount = 0;
            int parseSuccessCount = 0;
            int parseFailCount = 0;

            for (WebElement container : newsContainers) {
                if (itemCount >= maxItems) break;

                try {
                    NewsDataDto news = parseNewsItemWithSelenium(container, keyword);
                    if (news != null) {
                        newsList.add(news);
                        itemCount++;
                        parseSuccessCount++;
                        log.debug("뉴스 추가됨: {}", news.getTitle());
                    } else {
                        parseFailCount++;
                    }
                } catch (Exception e) {
                    parseFailCount++;
                    log.warn("개별 뉴스 아이템 파싱 실패: {}", e.getMessage());
                }
            }

            log.info("파싱 결과 - 성공: {}개, 실패: {}개, 총 수집: {}개",
                    parseSuccessCount, parseFailCount, newsList.size());

        } catch (Exception e) {
            log.error("뉴스 아이템 파싱 중 오류: {}", e.getMessage(), e);
        }

        return newsList;
    }

    /**
     * 컨테이너 구조 분석 (디버깅용)
     */
    private void analyzeContainerStructure(WebElement container) {
        try {
            log.info("=== 컨테이너 구조 분석 시작 ===");

            // 모든 하위 요소 찾기
            List<WebElement> allElements = container.findElements(By.cssSelector("*"));
            log.info("컨테이너 내 총 요소 수: {}", allElements.size());

            // 각 요소의 태그명, 클래스, 텍스트 분석
            for (int i = 0; i < Math.min(20, allElements.size()); i++) {
                WebElement element = allElements.get(i);
                String tagName = element.getTagName();
                String className = element.getAttribute("class");
                String text = element.getText().trim();
                String href = element.getAttribute("href");

                if (!text.isEmpty() || (href != null && !href.isEmpty())) {
                    log.info("요소 {}: 태그={}, 클래스={}, 텍스트='{}', href='{}'",
                            i, tagName, className, text, href);
                }
            }

            // 특정 패턴의 요소들 찾기
            List<WebElement> links = container.findElements(By.tagName("a"));
            log.info("링크 요소 수: {}", links.size());
            for (int i = 0; i < Math.min(5, links.size()); i++) {
                WebElement link = links.get(i);
                log.info("링크 {}: href='{}', 텍스트='{}', 클래스='{}'",
                        i, link.getAttribute("href"), link.getText().trim(), link.getAttribute("class"));
            }

            // 제목 관련 요소들 찾기
            List<WebElement> headings = container.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6"));
            log.info("제목 요소 수: {}", headings.size());
            for (int i = 0; i < Math.min(5, headings.size()); i++) {
                WebElement heading = headings.get(i);
                log.info("제목 {}: 태그={}, 텍스트='{}', 클래스='{}'",
                        i, heading.getTagName(), heading.getText().trim(), heading.getAttribute("class"));
            }

            log.info("=== 컨테이너 구조 분석 완료 ===");

        } catch (Exception e) {
            log.warn("컨테이너 구조 분석 실패: {}", e.getMessage());
        }
    }

    /**
     * Selenium을 사용한 개별 뉴스 아이템 파싱
     */
    private NewsDataDto parseNewsItemWithSelenium(WebElement container, String keyword) {
        try {
            // 제목 추출
            String title = extractTitleWithSelenium(container);
            if (title == null || title.trim().isEmpty()) {
                log.debug("제목 추출 실패");
                return null;
            }

            // 링크 추출
            String link = extractLinkWithSelenium(container);
            if (link == null || link.trim().isEmpty()) {
                log.debug("링크 추출 실패 - 제목: {}", title);
                return null;
            }

            // 설명 추출
            String description = extractDescriptionWithSelenium(container);

            // 뉴스 데이터 생성
            NewsDataDto news = new NewsDataDto(title,"", description, link, keyword, getPlatform());

            log.debug("파싱된 뉴스: {} | 링크: {}", title, link);
            return news;

        } catch (Exception e) {
            log.warn("뉴스 아이템 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Selenium을 사용한 제목 추출
     */
    private String extractTitleWithSelenium(WebElement container) {
        try {
            // 다음 뉴스의 실제 제목 선택자들 (우선순위 순)
            List<By> titleSelectors = List.of(
                    By.cssSelector("a"),              // 링크 텍스트
                    By.cssSelector("strong"),         // 강조 텍스트
                    By.cssSelector(".tit, .title"),   // 제목 클래스
                    By.cssSelector("h3, h4"),         // 제목 태그
                    By.cssSelector("[class*='title']"), // 제목 관련 클래스
                    By.cssSelector("[class*='tit']")   // 제목 관련 클래스
            );

            for (By selector : titleSelectors) {
                try {
                    List<WebElement> elements = container.findElements(selector);
                    if (!elements.isEmpty()) {
                        WebElement element = elements.get(0);
                        String title = element.getText().trim();

                        // 링크 요소인 경우 href 속성도 확인
                        if (title.isEmpty() && element.getTagName().equals("a")) {
                            title = element.getAttribute("title");
                            if (title == null || title.isEmpty()) {
                                title = element.getAttribute("aria-label");
                            }
                        }

                        if (title != null && !title.isEmpty()) {
                            // 제목 길이 제한 (100자)
                            if (title.length() > 100) {
                                title = title.substring(0, 100) + "...";
                            }
                            log.debug("제목 추출 성공 (선택자: {}): {}", selector, title);
                            return title;
                        }
                    }
                } catch (Exception e) {
                    log.debug("제목 선택자 {} 실패: {}", selector, e.getMessage());
                }
            }

            log.debug("모든 제목 선택자 실패");
            return null;

        } catch (Exception e) {
            log.warn("제목 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Selenium을 사용한 링크 추출
     */
    private String extractLinkWithSelenium(WebElement container) {
        try {
            // 여러 방법으로 링크 찾기
            List<By> linkSelectors = List.of(
                    By.cssSelector("a[href*='news']"),      // news가 포함된 링크
                    By.cssSelector("a[href*='article']"),   // article이 포함된 링크
                    By.cssSelector("a[href^='http']"),      // 절대 URL
                    By.cssSelector("a[href^='/']"),         // 상대 URL
                    By.cssSelector("a")                     // 모든 링크
            );

            for (By selector : linkSelectors) {
                try {
                    List<WebElement> links = container.findElements(selector);
                    for (WebElement link : links) {
                        String href = link.getAttribute("href");
                        if (href != null && !href.isEmpty()) {
                            // 유효한 뉴스 링크인지 확인
                            if (isValidNewsLink(href)) {
                                log.debug("링크 추출 성공: {}", href);
                                return href;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("링크 선택자 {} 실패: {}", selector, e.getMessage());
                }
            }

            log.debug("모든 링크 선택자 실패");
            return null;

        } catch (Exception e) {
            log.warn("링크 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 유효한 뉴스 링크인지 확인
     */
    private boolean isValidNewsLink(String href) {
        if (href == null || href.isEmpty()) {
            return false;
        }

        // 다음 뉴스 내부 링크는 제외
        if (href.contains("search.daum.net") && !href.contains("news")) {
            return false;
        }

        // 일반적인 뉴스 도메인 패턴 확인
        return href.startsWith("http") &&
                (href.contains("articles") ||
                        href.contains("news") ||
                        href.contains("story") ||
                        href.contains("content"));
    }

    /**
     * Selenium을 사용한 설명 추출
     */
    private String extractDescriptionWithSelenium(WebElement container) {
        try {
            // 다음 뉴스의 실제 설명 선택자들
            List<By> descSelectors = List.of(
                    By.cssSelector(".desc"),          // 설명 클래스
                    By.cssSelector(".summary"),       // 요약 클래스
                    By.cssSelector(".cont"),          // 내용 클래스
                    By.cssSelector("p"),              // 단락 태그
                    By.cssSelector(".description")    // 설명 클래스
            );

            for (By selector : descSelectors) {
                try {
                    List<WebElement> elements = container.findElements(selector);
                    if (!elements.isEmpty()) {
                        String description = elements.get(0).getText().trim();
                        if (!description.isEmpty()) {
                            // 설명 길이 제한 (200자)
                            if (description.length() > 200) {
                                description = description.substring(0, 200) + "...";
                            }
                            log.debug("설명 추출 성공: {}", description);
                            return description;
                        }
                    }
                } catch (Exception e) {
                    log.debug("설명 선택자 {} 실패: {}", selector, e.getMessage());
                }
            }

            // 설명이 없으면 제목을 사용
            String title = extractTitleWithSelenium(container);
            if (title != null && !title.isEmpty()) {
                log.debug("설명 없음, 제목을 설명으로 사용: {}", title);
                return title;
            }

            log.debug("모든 설명 선택자 실패");
            return "";

        } catch (Exception e) {
            log.warn("설명 추출 실패: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}