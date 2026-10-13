package vallegrande.edu.pe.preparationservice.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas funcionales de navegador para la evidencia GI08.
 * Se ejecutan únicamente con: mvn -Pselenium verify.
 */
class PreparationFunctionalSeleniumIT {

    private static WebDriver driver;
    private static String baseUrl;

    @BeforeAll
    static void startBrowser() throws MalformedURLException {
        baseUrl = System.getProperty(
                "selenium.baseUrl",
                System.getenv().getOrDefault("SELENIUM_BASE_URL", "http://localhost:8097"));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1440,1200");

        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");
        driver = remoteUrl == null || remoteUrl.isBlank()
                ? new ChromeDriver(options)
                : new RemoteWebDriver(URI.create(remoteUrl).toURL(), options);
    }

    @AfterAll
    static void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void swaggerDocumentsPreparationOperations() {
        driver.get(baseUrl + "/swagger-ui/index.html");

        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(current -> current.findElements(By.cssSelector(".swagger-ui")).size() == 1);

        String page = driver.getPageSource().toLowerCase();
        assertThat(page).contains("preparation");
    }

    @Test
    void protectedPreparationEndpointRejectsAnonymousUser() {
        driver.get(baseUrl + "/api/v1/preparations/request/1");

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(current -> current.getPageSource().contains("401")
                        || current.getPageSource().toLowerCase().contains("unauthorized"));

        assertThat(driver.getPageSource()).contains("401");
    }
}
