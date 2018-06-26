package mockaroo;

import static org.testng.Assert.assertThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class mockarooTest {
	private WebDriver driver = null;
	
	
	@BeforeTest
	public void setUpTest() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	}
	
	@AfterTest
	public void tearDownTest() {
		driver.quit();
		try {
			Path dir = Paths.get("C:\\Users\\HA\\Downloads");
			Files.list(dir)
			    .filter( f -> f.getFileName().toString().startsWith("MOCK_DATA") 
			    		      && f.getFileName().toString().endsWith("csv"))
			    .forEach( f -> f.toFile().delete());		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test(priority = 0, enabled = true)
	public void prepareAndDownloadData() throws InterruptedException {
		driver.get("https://mockaroo.com/");
		String actualText = null;
		actualText = driver.findElement(By.xpath("//div[@class='brand']")).getText();
		Assert.assertEquals(actualText, "mockaroo");
		
		actualText = driver.findElement(By.xpath("//div[@class='tagline']")).getText();
		Assert.assertEquals(actualText, "realistic data generator");
		
		for (WebElement element : 
			driver.findElements(By.xpath("//a[@class='close remove-field remove_nested_fields']"))) {
			element.click();
		}
	
		Assert.assertTrue(Utility.isDisplayed(driver.findElement(By.xpath("//div[@class='column column-header column-name' and .='Field Name']"))));
		Assert.assertTrue(Utility.isDisplayed(driver.findElement(By.xpath("//div[@class='column column-header column-type' and .='Type']"))));
		Assert.assertTrue(Utility.isDisplayed(driver.findElement(By.xpath("//div[@class='column column-header column-options' and .='Options']"))));
		
		Assert.assertTrue(driver.findElement(By.xpath("//a[.='Add another field']")).isEnabled());
		
		Assert.assertTrue(driver.findElement(By.xpath("//input[@value='1000']")).getAttribute("value").equals("1000"));

		Select format = new Select(driver.findElement(By.xpath("//select[@id='schema_file_format']")));
		Assert.assertTrue(format.getFirstSelectedOption().getText().equalsIgnoreCase("csv"));
		
		Select lineEnding = new Select(driver.findElement(By.xpath("//select[@id='schema_line_ending']")));
		Assert.assertTrue(lineEnding.getFirstSelectedOption().getText().equalsIgnoreCase("Unix (LF)"));
		
		Assert.assertTrue(driver.findElement(By.xpath("//input[@id='schema_include_header']")).isSelected());
		Assert.assertFalse(driver.findElement(By.xpath("//input[@id='schema_bom']")).isSelected());
		
		driver.findElement(By.xpath("//a[.='Add another field']")).click();
		
		List <WebElement> lE = driver.findElements(By.xpath("//input[@placeholder='enter name...']"));
		lE.get(lE.size() - 1).sendKeys("City");
		
		lE = driver.findElements(By.xpath("//input[@placeholder='choose type...']"));
		lE.get(lE.size() - 1).click();
		
		Assert.assertTrue(Utility.isPresent(driver,By.xpath("//h3[.='Choose a Type']")));
		
		driver.findElement(By.id("type_search_field")).sendKeys("City");
		driver.findElement(By.xpath("//div[@class='type-name']")).click();
		
		//new WebDriverWait(driver, 10);		//.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[.='Add another field']")));
		Thread.sleep(1000);
		driver.findElement(By.xpath("//a[.='Add another field']")).click();
		
		lE = driver.findElements(By.xpath("//input[@placeholder='enter name...']"));
		lE.get(lE.size() - 1).sendKeys("Country");
		
		lE = driver.findElements(By.xpath("//input[@placeholder='choose type...']"));
		lE.get(lE.size() - 1).click();
		
		Assert.assertTrue(Utility.isPresent(driver,By.xpath("//h3[.='Choose a Type']")));
		
		new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.id("type_search_field")));
		driver.findElement(By.id("type_search_field")).clear();;
		driver.findElement(By.id("type_search_field")).sendKeys("Country");
		driver.findElement(By.xpath("//div[@class='type-name']")).click();
		
		Thread.sleep(1000);
		driver.findElement(By.id("download")).click();
	}
	
	@Test(priority = 1)
	public void loadAndProcessData() throws IOException, InterruptedException {
		List<String> cities = new ArrayList<>();
		List<String> countries = new ArrayList<>();
		
		long fileReadyTimeOut = 5000;
		long fileReadyTimeElapsed = 0;
		
		Path dir = Paths.get("C:\\Users\\HA\\Downloads");
		Optional <Path> lastFilePath;
		do {
			lastFilePath = Files.list(dir)
			    .filter( f -> f.getFileName().toString().startsWith("MOCK_DATA") 
			    		&& f.getFileName().toString().endsWith("csv"))
			    //.peek(f-> System.out.println(f.getFileName() ) ) 
			    .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
			    //.ifPresent(f-> System.out.println("Latest "+ f.getFileName() ) ) ; 
			Thread.sleep(500);
			fileReadyTimeElapsed += 500;
		} while (!lastFilePath.isPresent() && fileReadyTimeElapsed < fileReadyTimeOut);
		
		BufferedReader br = null;
		if ( lastFilePath.isPresent() )
		{
			Path lastFile = lastFilePath.get();
			System.out.println(lastFile.getFileName().toString());
			br = new BufferedReader(new FileReader(lastFile.toFile()));
			br.readLine();
			br.lines().forEach(l -> { String[] items = l.split(","); cities.add(items[0]); countries.add(items[1]); } );
		}
		
		Assert.assertEquals(cities.size(), 1000);
		
		Collections.sort(cities, (String s1, String s2) -> s1.length() - s2.length() );
		System.out.println(cities.get(0));
		
		HashSet<String> countrySet = new HashSet<>(countries);
		for (String country : countrySet) {
			System.out.println(country + "-" +
			Collections.frequency(countries, country));
		}
		
		List<String> uniqueCities = new ArrayList<>();
		for (String city : cities) {
			if(!uniqueCities.contains(city))
				uniqueCities.add(city);
		}
		
		HashSet<String> citySet = new HashSet<>(cities);
		Assert.assertEquals(uniqueCities.size(), citySet.size());
		
		List<String> uniqueCountries = new ArrayList<>();
		for (String country : countries) {
			if(!uniqueCountries.contains(country))
				uniqueCountries.add(country);
		}
		
		Assert.assertEquals(uniqueCountries.size(), countrySet.size());
	}
}
