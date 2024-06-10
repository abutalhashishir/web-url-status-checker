package qa.testcases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import qa.base.Base;

public class SinglePageAllUrlRunTest extends Base {

    public WebDriver driver;
    private final String testResultDir = "C:\\Users\\Riseup Labs\\eclipse-workspace\\URL_With_StatusCode\\src\\test\\java\\SinglePageTestResult";

    public SinglePageAllUrlRunTest() {
        super();
    }

    @BeforeClass
    public void setup() throws InterruptedException {
        driver = initializeBrowserAndOpenApplicationURL(prop.getProperty("browserName"));
        createDirectoryIfNotExists(testResultDir);
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }

    @Test(priority = 0)
    public void TotalLinkTest() throws InterruptedException {
        List<WebElement> allTags = driver.findElements(By.tagName("a"));
        System.out.println("Total anchor tags are: " + allTags.size());

        try {
            FileWriter writer = new FileWriter(testResultDir + "\\AllUrl.txt");
            int urlCount = 0;
            for (WebElement tag : allTags) {
                String href = tag.getAttribute("href");
                String text = tag.getText().trim();

                if (href != null && !href.isEmpty()) {
                    urlCount++;
                    System.out.println("Link on page is: " + href);
                    System.out.println("Text of link is: " + text);

                    writer.write("url" + urlCount + "=" + href + "\n");
                    writer.flush();
                } else {
                    System.out.println("Skipping empty URL");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 1)
    public void AllUrlTest() throws InterruptedException {
        List<String> passedUrls = new ArrayList<>();
        List<String> failedUrls = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(testResultDir + "\\AllUrl.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("url")) {
                    String url = line.substring(line.indexOf("=") + 1);
                    System.out.println("Navigating to URL: " + url);

                    driver.get(url);
                    Thread.sleep(2000);

                    try {
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
                        wait.until(ExpectedConditions
                                .presenceOfElementLocated(By.xpath("//h4[@class='error-sub-title mb-3']")));

                        failedUrls.add(url);
                    } catch (TimeoutException e) {
                        passedUrls.add(url);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveUrlsToFile(testResultDir + "\\PassUrl.txt", passedUrls);
        saveUrlsToFile(testResultDir + "\\FailUrl.txt", failedUrls);
    }

    private void saveUrlsToFile(String filePath, List<String> urls) {
        try {
            FileWriter writer = new FileWriter(filePath);
            for (String url : urls) {
                writer.write(url + "\n");
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directoryPath);
            } else {
                System.out.println("Failed to create directory: " + directoryPath);
            }
        }
    }
}
