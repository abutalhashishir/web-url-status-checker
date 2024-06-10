package qa.testcases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import qa.base.Base;

public class AllUrlWithStatusCodeTest extends Base {

	public WebDriver driver;
	private final String testResultDir = "C:\\Users\\Riseup Labs\\eclipse-workspace\\URL_With_StatusCode\\src\\test\\java\\TestResult";

	@BeforeClass
	public void setup() {
		driver = initializeBrowserAndOpenApplicationURL(prop.getProperty("browserName"));
		createDirectoryIfNotExists(testResultDir);
	}

	@AfterClass
	public void tearDown() {
		driver.quit();
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

	@Test(priority = 0)
	public void totalLinkTest() {
	    List<WebElement> allTags = driver.findElements(By.tagName("a"));
	    System.out.println("Total anchor tags are: " + allTags.size());

	    try (FileWriter writer = new FileWriter(testResultDir + "\\AllUrl.txt")) {
	        int urlCount = 0;
	        for (WebElement tag : allTags) {
	            String href = tag.getAttribute("href");
	            if (href != null && !href.isEmpty()) {
	                urlCount++;
	                String text = tag.getText().trim();
	                System.out.println("Link on page is: " + href);
	                System.out.println("Text of link is: " + text);

	                writer.write("url" + urlCount + "=" + href + "\n");
	                writer.flush();
	            } else {
	                System.out.println("Skipping empty URL");
	            }
	            
	            // Introducing a delay of 1.5 seconds
	            try {
	                Thread.sleep(3000); // Sleep for 1.5 seconds
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


	@Test(priority = 1)
	public void searchUrls() {
	    try (BufferedReader reader = new BufferedReader(new FileReader(testResultDir + "\\AllUrl.txt"))) {
	        String line;
	        int urlIndex = 1;
	        while ((line = reader.readLine()) != null) {
	            if (line.startsWith("url")) {
	                String[] parts = line.split("=");
	                String url = parts[1];

	                if (shouldSkipUrl(url)) {
	                    System.out.println("Skipping URL: " + url);
	                    continue;
	                }

	                searchAndSavePageUrls("url" + urlIndex, url);
	                urlIndex++;
	                
	                // Introducing a delay of 1.5 seconds after each URL search
	                try {
	                    Thread.sleep(3000); // Sleep for 1.5 seconds
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	private boolean shouldSkipUrl(String url) {
		String[] urlsToSkip = { "https://riseuplabs.com/qa/", "https://admagic.riseuplabs.com/redirect/link/39",
				"https://riseuplabs.com/" };

		for (String skipUrl : urlsToSkip) {
			if (url.equals(skipUrl)) {
				return true;
			}
		}

		return false;
	}

	private void searchAndSavePageUrls(String urlName, String url) {
		driver.get(url);
		List<WebElement> pageUrls = driver.findElements(By.tagName("a"));
		List<String> pageUrlStrings = new ArrayList<>();
		for (WebElement pageUrl : pageUrls) {
			String href = pageUrl.getAttribute("href");
			if (href != null && !href.isEmpty()) {
				pageUrlStrings.add(href);
			}
		}
		savePageUrlsToFile(urlName, pageUrlStrings);
	}

	private void savePageUrlsToFile(String urlName, List<String> pageUrls) {
		try (FileWriter writer = new FileWriter(testResultDir + "\\Page" + urlName + ".txt");
				FileWriter writer2 = new FileWriter(testResultDir + "\\AllPageUrls.txt", true)) {

			for (int i = 0; i < pageUrls.size(); i++) {
				String pageUrl = pageUrls.get(i);
				String formattedUrl = "url" + (i + 1) + "=" + pageUrl;
				writer.write(pageUrl + "\n");
				writer2.write(formattedUrl + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test(priority = 2)
	public void testUrlResponseCodes() {
		try (BufferedReader allUrlReader = new BufferedReader(new FileReader(testResultDir + "\\AllUrl.txt"));
				BufferedReader allPageUrlsReader = new BufferedReader(
						new FileReader(testResultDir + "\\AllPageUrls.txt"));
				FileWriter writer = new FileWriter(testResultDir + "\\ResponseCodes.txt", true)) {

			readAndTestResponseCodes(allUrlReader, writer);
			readAndTestResponseCodes(allPageUrlsReader, writer);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAndTestResponseCodes(BufferedReader reader, FileWriter writer) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("=")) {
				String[] parts = line.split("=");
				if (parts.length > 1) {
					String url = parts[1];
					int responseCode = getResponseCode(url);
					System.out.println(url + " response code: " + responseCode);
					writer.write(url + " response code: " + responseCode + "\n");
				}
			}
		}
	}

	private int getResponseCode(String urlString) throws IOException {
		if (urlString.startsWith("mailto:")) {
			return 0;
		}

		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		int responseCode = connection.getResponseCode();
		connection.disconnect();
		return responseCode;
	}

	@Test(priority = 3)
	public void removeDuplicateResponseCodes() {
		String responseCodesFilePath = testResultDir + "\\ResponseCodes.txt";
		String uniqueResponseCodesFilePath = testResultDir + "\\ExtractResponseCodes.txt";

		try (BufferedReader reader = new BufferedReader(new FileReader(responseCodesFilePath));
				FileWriter writer = new FileWriter(uniqueResponseCodesFilePath)) {

			Set<String> uniqueResponseCodes = new HashSet<>();

			String line;
			while ((line = reader.readLine()) != null) {
				uniqueResponseCodes.add(line.trim());
			}

			for (String responseCode : uniqueResponseCodes) {
				writer.write(responseCode + "\n");
			}

			System.out.println("Duplicate response codes removed and saved successfully!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
