package mockaroo;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Utility {
	public static boolean isDisplayed(WebElement element) {
		try {
			return element.isDisplayed();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean isPresent(WebDriver driver, By locator) {
		try {
			driver.findElement(locator);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
}
