package com.suman.newsfeed.infrastructure.external.crawler.naver;

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
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class NaverCrawlerStrategy implements CrawlerStrategy {

    @Value("${naver.news.enabled:true}")
    private Boolean enabled;

    @Value("${naver.news.timeout:15000}")
    private Integer timeout;

    @Value("${naver.news.headless:true}")
    private Boolean headless;

    // 네이버 뉴스 검색 URL 패턴
    private static final String NAVER_NEWS_SEARCH_URL = "https://search.naver.com/search.naver?where=news&query=%s&sm=tab_she&qdt=0";

    @Override
    public NewsPlatform getPlatform() {
        return NewsPlatform.NAVER;
    }

    @Override
    public List<NewsDataDto> crawlNews(String keyword, Long pageNumber, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("키워드가 비어있습니다.");
            return new ArrayList<>();
        }

        WebDriver driver = null;
        try {
            log.info("네이버 뉴스 크롤링 시작 (Selenium) - 키워드: {}, 페이지: {}, 크기: {}", keyword, pageNumber, pageSize);

            // WebDriver 초기화
            driver = initializeWebDriver();

            // 검색 URL 생성 및 접속
            String url = buildSearchUrl(keyword);
            log.debug("크롤링 URL: {}", url);

            driver.get(url);

            // 페이지 로딩 대기
            waitForPageLoad(driver);

            // 페이지 구조 디버깅 (문제 해결을 위해 추가)
            debugPageStructure(driver);

            // 뉴스 아이템 파싱
            List<NewsDataDto> newsList = parseNewsItemsWithSelenium(driver, keyword, pageSize);

            log.info("네이버 뉴스 크롤링 완료 - 키워드: {}, 수집된 뉴스: {}개", keyword, newsList.size());
            return newsList;

        } catch (Exception e) {
            log.error("네이버 뉴스 크롤링 중 오류 발생 - 키워드: {}, 오류: {}", keyword, e.getMessage(), e);
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
     * WebDriver 초기화
     */
    private WebDriver initializeWebDriver() {
        try {
            // Chrome WebDriver 자동 설정
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();

            if (headless) {
                options.addArguments("--headless");
            }

            // 성능 및 메모리 최적화 옵션
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            
            // 메모리 사용량 최적화
            options.addArguments("--memory-pressure-off");
            options.addArguments("--max_old_space_size=4096");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-features=TranslateUI");
            options.addArguments("--disable-ipc-flooding-protection");

            // CDP 경고 제거
            options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);

            WebDriver driver = new ChromeDriver(options);
            log.debug("Chrome WebDriver 초기화 완료");
            return driver;

        } catch (Exception e) {
            log.error("WebDriver 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("WebDriver 초기화 실패", e);
        }
    }

    /**
     * 검색 URL 생성
     */
    private String buildSearchUrl(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            return String.format(NAVER_NEWS_SEARCH_URL, encodedKeyword);
        } catch (Exception e) {
            log.error("URL 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("URL 생성 실패", e);
        }
    }

    /**
     * 페이지 로딩 대기
     */
    private void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeout));

            // 페이지 로딩 완료 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // 추가 대기 시간 (동적 콘텐츠 로딩을 위해)
            Thread.sleep(3000);

            log.debug("페이지 로딩 완료");

        } catch (Exception e) {
            log.warn("페이지 로딩 대기 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 페이지 구조 디버깅 (문제 해결을 위해 추가)
     */
    private void debugPageStructure(WebDriver driver) {
        try {
            log.info("=== 페이지 구조 디버깅 시작 ===");

            // 페이지 제목 확인
            String pageTitle = driver.getTitle();
            log.info("페이지 제목: {}", pageTitle);

            // 현재 URL 확인
            String currentUrl = driver.getCurrentUrl();
            log.info("현재 URL: {}", currentUrl);

            // 실제 뉴스 컨테이너 확인 (네이버 뉴스 DOM 구조 기반)
            List<WebElement> newsContainers = driver.findElements(By.cssSelector(".news_wrap"));
            log.info("네이버 뉴스 컨테이너(.news_wrap) 수: {}", newsContainers.size());

            // 대안 선택자들 확인
            List<WebElement> newsArea = driver.findElements(By.cssSelector(".news_area"));
            log.info("뉴스 영역(.news_area) 수: {}", newsArea.size());

            List<WebElement> bxArea = driver.findElements(By.cssSelector(".bx"));
            log.info("뉴스 박스(.bx) 수: {}", bxArea.size());

            List<WebElement> ulLists = driver.findElements(By.cssSelector("ul.list_news"));
            log.info("뉴스 리스트(ul.list_news) 수: {}", ulLists.size());

            List<WebElement> liItems = driver.findElements(By.cssSelector("ul.list_news li"));
            log.info("뉴스 아이템(ul.list_news li) 수: {}", liItems.size());

            // 뉴스 제목 링크 확인
            List<WebElement> newsLinks = driver.findElements(By.cssSelector("a.news_tit"));
            log.info("뉴스 제목 링크(a.news_tit) 수: {}", newsLinks.size());

            // 모든 a 태그 중 뉴스 관련 링크 확인
            List<WebElement> allLinks = driver.findElements(By.cssSelector("a[href*='news']"));
            log.info("뉴스 관련 링크 수: {}", allLinks.size());

            // 추가 디버깅: 다양한 선택자로 뉴스 아이템 찾기
            List<WebElement> newsItems1 = driver.findElements(By.cssSelector("div.news"));
            log.info("뉴스 아이템(div.news) 수: {}", newsItems1.size());

            List<WebElement> newsItems2 = driver.findElements(By.cssSelector("li[class*='news']"));
            log.info("뉴스 아이템(li[class*='news']) 수: {}", newsItems2.size());

            List<WebElement> newsItems3 = driver.findElements(By.cssSelector("div[class*='news']"));
            log.info("뉴스 아이템(div[class*='news']) 수: {}", newsItems3.size());

            List<WebElement> newsItems4 = driver.findElements(By.cssSelector(".news"));
            log.info("뉴스 아이템(.news) 수: {}", newsItems4.size());

            List<WebElement> newsItems5 = driver.findElements(By.cssSelector("#main_pack .news"));
            log.info("뉴스 아이템(#main_pack .news) 수: {}", newsItems5.size());

            List<WebElement> newsItems6 = driver.findElements(By.cssSelector("#main_pack li"));
            log.info("메인 팩 내 li 아이템 수: {}", newsItems6.size());

            List<WebElement> newsItems7 = driver.findElements(By.cssSelector(".api_ani_send"));
            log.info("API 애니 센드 아이템 수: {}", newsItems7.size());

            // 모든 div 태그에서 뉴스 관련 클래스 찾기
            List<WebElement> allDivs = driver.findElements(By.tagName("div"));
            log.info("전체 div 태그 수: {}", allDivs.size());

            // 첫 번째 뉴스 아이템이 있다면 상세 분석
            if (!liItems.isEmpty()) {
                WebElement firstItem = liItems.get(0);
                String itemHtml = firstItem.getAttribute("outerHTML");
                if (itemHtml.length() > 500) {
                    itemHtml = itemHtml.substring(0, 500) + "...";
                }
                log.info("첫 번째 뉴스 아이템 HTML: {}", itemHtml);
            }

            // main_pack 내부 구조 확인
            try {
                WebElement mainPack = driver.findElement(By.id("main_pack"));
                List<WebElement> mainPackChildren = mainPack.findElements(By.xpath("./*"));
                log.info("main_pack 직계 자식 요소 수: {}", mainPackChildren.size());

                for (int i = 0; i < Math.min(3, mainPackChildren.size()); i++) {
                    WebElement child = mainPackChildren.get(i);
                    String tagName = child.getTagName();
                    String className = child.getAttribute("class");
                    String id = child.getAttribute("id");
                    log.info("main_pack 자식[{}] - 태그: {}, 클래스: {}, ID: {}", i, tagName, className, id);
                }
            } catch (Exception e) {
                log.warn("main_pack 요소를 찾을 수 없습니다: {}", e.getMessage());
            }

            log.info("=== 페이지 구조 디버깅 완료 ===");

        } catch (Exception e) {
            log.error("페이지 구조 디버깅 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * Selenium을 사용한 뉴스 아이템 파싱
     */
    private List<NewsDataDto> parseNewsItemsWithSelenium(WebDriver driver, String keyword, int maxItems) {
        List<NewsDataDto> newsList = new ArrayList<>();

        try {
            // 네이버 뉴스의 실제 DOM 구조에 맞는 선택자들 (디버깅 결과 기반)
            List<By> selectors = List.of(
                    By.cssSelector("div[class*='news']"),        // news가 포함된 div (실제 뉴스 4개 발견!)
                    By.cssSelector("#main_pack li"),             // 메인 팩 내 li (228개 발견)
                    By.cssSelector("#news_form li"),             // 뉴스 폼 내 li
                    By.cssSelector("form#news_form li"),         // 뉴스 폼 내 li
                    By.cssSelector("ul.list_news li"),           // 네이버 뉴스 리스트 아이템
                    By.cssSelector(".list_news li"),             // 뉴스 리스트 아이템
                    By.cssSelector(".news_wrap"),                // 뉴스 래퍼
                    By.cssSelector(".news_area"),                // 뉴스 영역
                    By.cssSelector(".bx"),                       // 뉴스 박스
                    By.cssSelector("li.bx"),                     // 뉴스 박스 리스트
                    By.cssSelector("article")                    // fallback
            );

            List<WebElement> containers = null;
            By usedSelector = null;

            // 여러 선택자를 순차적으로 시도
            for (By selector : selectors) {
                try {
                    containers = driver.findElements(selector);
                    if (containers != null && !containers.isEmpty()) {
                        usedSelector = selector;
                        log.info("선택자 '{}'로 {}개의 뉴스 컨테이너를 찾았습니다", selector, containers.size());
                        break;
                    }
                } catch (Exception e) {
                    log.debug("선택자 '{}' 실패: {}", selector, e.getMessage());
                }
            }

            if (containers == null || containers.isEmpty()) {
                log.warn("네이버 뉴스 컨테이너를 찾을 수 없습니다.");
                return newsList;
            }

            // 첫 번째 컨테이너 HTML 구조 분석 (디버깅용)
            if (!containers.isEmpty()) {
                WebElement firstContainer = containers.get(0);
                String containerHtml = firstContainer.getAttribute("outerHTML");
                if (containerHtml.length() > 500) {
                    containerHtml = containerHtml.substring(0, 500) + "...";
                }
                log.info("첫 번째 컨테이너 HTML (전체): {}", containerHtml);

                // 컨테이너 내부의 링크들 확인
                List<WebElement> linksInContainer = firstContainer.findElements(By.tagName("a"));
                log.info("첫 번째 컨테이너 내 링크 수: {}", linksInContainer.size());

                if (!linksInContainer.isEmpty()) {
                    WebElement firstLink = linksInContainer.get(0);
                    String linkText = firstLink.getText().trim();
                    String linkHref = firstLink.getAttribute("href");
                    log.info("첫 번째 링크 - 텍스트: '{}', href: '{}'", linkText, linkHref);
                }
            }

            // 뉴스 아이템 파싱
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < Math.min(containers.size(), maxItems); i++) {
                try {
                    WebElement container = containers.get(i);
                    NewsDataDto news = parseNewsItem(container, keyword);

                    if (news != null && news.getTitle() != null && !news.getTitle().trim().isEmpty()) {
                        newsList.add(news);
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.warn("뉴스 아이템 {} 파싱 실패: {}", i, e.getMessage());
                    failCount++;
                }
            }

            log.info("파싱 결과 - 성공: {}개, 실패: {}개, 총 수집: {}개", successCount, failCount, newsList.size());

        } catch (Exception e) {
            log.error("뉴스 아이템 파싱 중 오류: {}", e.getMessage(), e);
        }

        return newsList;
    }

    /**
     * 개별 뉴스 아이템 파싱
     */
    private NewsDataDto parseNewsItem(WebElement container, String keyword) {
        try {
            // 제목 추출
            String title = extractTitleWithSelenium(container);
            if (title == null || title.trim().isEmpty()) {
                return null;
            }

            // 링크 추출
            String link = extractLinkWithSelenium(container);
            if (link == null || link.trim().isEmpty()) {
                return null;
            }

            // 설명 추출 (언론사 + 시간 정보)
            String description = extractDescriptionWithSelenium(container);

            // NewsDataDto 생성
            NewsDataDto news = new NewsDataDto(
                    title,
                    "",
                    description,
                    link,
                    keyword,
                    getPlatform()
            );

            log.debug("뉴스 아이템 파싱 성공 - 제목: {}, 링크: {}", title, link);
            return news;

        } catch (Exception e) {
            log.warn("뉴스 아이템 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Selenium을 사용한 제목 추출 (네이버 뉴스 실제 구조 기반)
     */
    private String extractTitleWithSelenium(WebElement container) {
        try {
            // 네이버 뉴스의 실제 제목 선택자들
            List<By> titleSelectors = List.of(
                    By.cssSelector("a.news_tit"),                // 네이버 뉴스 제목 링크
                    By.cssSelector(".news_tit"),                 // 뉴스 제목 클래스
                    By.cssSelector("a[class*='news_tit']"),      // news_tit이 포함된 클래스
                    By.cssSelector("a.tit"),                     // 제목 링크
                    By.cssSelector(".tit"),                      // 제목 클래스
                    By.cssSelector("h3 a"),                      // h3 내 링크
                    By.cssSelector("h4 a"),                      // h4 내 링크
                    By.cssSelector("a[href*='news']"),           // 뉴스 링크
                    By.cssSelector("a"),                         // 모든 링크
                    By.cssSelector("dt a"),                      // dt 내 링크
                    By.cssSelector(".title")                     // 제목 클래스
            );

            for (By selector : titleSelectors) {
                try {
                    WebElement titleElement = container.findElement(selector);
                    String title = titleElement.getText().trim();
                    if (title != null && !title.isEmpty() && title.length() > 5) {
                        log.debug("제목 추출 성공 (선택자: {}): {}", selector, title);
                        return title;
                    }
                } catch (Exception e) {
                    // 다음 선택자 시도
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
            // 네이버 뉴스의 실제 링크 선택자들
            List<By> linkSelectors = List.of(
                    By.cssSelector("a.news_tit"),                // 네이버 뉴스 제목 링크
                    By.cssSelector("a[class*='news_tit']"),      // news_tit이 포함된 클래스
                    By.cssSelector("a.tit"),                     // 제목 링크
                    By.cssSelector("a[href*='news']"),           // 뉴스 링크
                    By.cssSelector("a[href*='naver']"),          // 네이버 링크
                    By.cssSelector("a[href]"),                   // href 속성이 있는 링크
                    By.cssSelector("a"),                         // 모든 링크
                    By.cssSelector("dt a")                       // dt 내 링크
            );

            for (By selector : linkSelectors) {
                try {
                    WebElement linkElement = container.findElement(selector);
                    String href = linkElement.getAttribute("href");
                    if (href != null && !href.trim().isEmpty() && !href.startsWith("javascript:")) {
                        log.debug("링크 추출 성공 (선택자: {}): {}", selector, href);
                        return href;
                    }
                } catch (Exception e) {
                    // 다음 선택자 시도
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
     * Selenium을 사용한 설명 추출 (언론사 정보 포함)
     */
    private String extractDescriptionWithSelenium(WebElement container) {
        try {
            // 언론사 정보 추출 (네이버 뉴스 전용)
            String source = extractSourceWithSelenium(container);

            // 시간 정보 추출
            String time = extractTimeWithSelenium(container);

            // 설명 조합
            StringBuilder description = new StringBuilder();
            if (source != null && !source.isEmpty()) {
                description.append("출처: ").append(source);
            }
            if (time != null && !time.isEmpty()) {
                if (description.length() > 0) {
                    description.append(" | ");
                }
                description.append("시간: ").append(time);
            }

            if (description.length() > 0) {
                String result = description.toString();
                // 설명 길이 제한 (200자)
                if (result.length() > 200) {
                    result = result.substring(0, 200) + "...";
                }
                log.debug("설명 추출 성공: {}", result);
                return result;
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

    /**
     * Selenium을 사용한 언론사 정보 추출
     */
    private String extractSourceWithSelenium(WebElement container) {
        try {
            // 네이버 뉴스의 실제 언론사 선택자들
            List<By> sourceSelectors = List.of(
                    By.cssSelector(".press"),                     // 언론사 클래스
                    By.cssSelector(".source"),                    // 출처 클래스
                    By.cssSelector(".media"),                     // 미디어 클래스
                    By.cssSelector(".company"),                   // 회사 클래스
                    By.cssSelector(".news_agency"),               // 뉴스 에이전시 클래스
                    By.cssSelector(".news_source"),               // 뉴스 출처 클래스
                    By.cssSelector(".news_company"),              // 뉴스 회사 클래스
                    By.cssSelector("span[class*='press']"),       // press가 포함된 클래스
                    By.cssSelector("span[class*='source']"),      // source가 포함된 클래스
                    By.cssSelector("span[class*='media']"),       // media가 포함된 클래스
                    By.cssSelector("cite"),                       // cite 태그
                    By.cssSelector(".info_group cite"),           // 정보 그룹 내 cite
                    By.cssSelector(".info cite")                  // 정보 내 cite
            );

            for (By selector : sourceSelectors) {
                try {
                    WebElement sourceElement = container.findElement(selector);
                    String source = sourceElement.getText().trim();
                    if (source != null && !source.isEmpty()) {
                        log.debug("언론사 추출 성공 (선택자: {}): {}", selector, source);
                        return source;
                    }
                } catch (Exception e) {
                    // 다음 선택자 시도
                }
            }

            log.debug("모든 언론사 선택자 실패");
            return null;

        } catch (Exception e) {
            log.warn("언론사 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Selenium을 사용한 시간 정보 추출
     */
    private String extractTimeWithSelenium(WebElement container) {
        try {
            // 네이버 뉴스의 실제 시간 선택자들
            List<By> timeSelectors = List.of(
                    By.cssSelector("time"),                       // time 태그
                    By.cssSelector(".time"),                      // 시간 클래스
                    By.cssSelector(".date"),                      // 날짜 클래스
                    By.cssSelector(".timestamp"),                 // 타임스탬프 클래스
                    By.cssSelector(".news_time"),                 // 뉴스 시간 클래스
                    By.cssSelector(".news_date"),                 // 뉴스 날짜 클래스
                    By.cssSelector("span[class*='time']"),        // time이 포함된 클래스
                    By.cssSelector("span[class*='date']"),        // date가 포함된 클래스
                    By.cssSelector("span[class*='ago']"),         // ago가 포함된 클래스 (몇 시간 전 등)
                    By.cssSelector(".info_group span"),           // 정보 그룹 내 span
                    By.cssSelector(".info span")                  // 정보 내 span
            );

            for (By selector : timeSelectors) {
                try {
                    WebElement timeElement = container.findElement(selector);
                    String time = timeElement.getText().trim();
                    if (time != null && !time.isEmpty() && (time.contains("전") || time.contains("시간") || time.contains("분"))) {
                        log.debug("시간 추출 성공 (선택자: {}): {}", selector, time);
                        return time;
                    }
                } catch (Exception e) {
                    // 다음 선택자 시도
                }
            }

            log.debug("모든 시간 선택자 실패");
            return null;

        } catch (Exception e) {
            log.warn("시간 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * URL 유효성 검사
     */
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * HTML 태그 제거
     */
    private String removeHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").trim();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

