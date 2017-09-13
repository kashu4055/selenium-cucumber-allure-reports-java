package com.qa.tlv.environment;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import com.qa.tlv.logger.Log;

import methods.PropertiesManagementMethods;

public class BrowserManagement {
	static WebDriver driver = null;
	static String browserName = null;
	static String cloudBrowserConfigFile = null;
	static String cloudPlatformConfigFile = null;
	static String currentPath = System.getProperty("user.dir");
	static Properties prop = new Properties();
	static PropertiesManagementMethods propertiesObj = new PropertiesManagementMethods();
	static ChromeDriverSetup chromeDriverSetup = new ChromeDriverSetup();

	static String chromeDriverPath;

	public static String getChromeDriverPath() {

		if (propertiesObj.isWindows()) {
			chromeDriverPath = propertiesObj.getProperty("winChromeDrverPath");
		}

		else if (propertiesObj.isMac()) {
			chromeDriverPath = propertiesObj.getProperty("macChromeDrverPath");
		}

		else if (propertiesObj.isUnix()) {
			chromeDriverPath = propertiesObj.getProperty("linuxChromeDrverPath");
		}
		return chromeDriverPath;
	}
	
	

	public static String getBrowserName() {
		browserName = System.getProperty("browser");
		cloudBrowserConfigFile = System.getProperty("cloud_config");

		if (cloudBrowserConfigFile != null) {
			Log.INFO("reading config file");
			try {
				browserName = cloudBrowserConfigFile.split("_")[0];
				InputStream input = new FileInputStream(
						currentPath + "/src/main/java/cloudBrowserConfigs/" + cloudBrowserConfigFile + ".properties");
				input.close();

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		} else if (browserName == null)
			browserName = "ff";
		return browserName;
	}

	public static WebDriver SaucelabDriver() {
		Log.INFO("Creating Saucelab Driver");

		try {
			InputStream input = new FileInputStream(
					currentPath + "/src/main/java/cloudPlatformConfigs/saucelab.properties");
			prop.load(input);
			HashMap<String, String> saucelabConfig = new HashMap<String, String>();
			Enumeration<?> enuKeys = prop.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = prop.getProperty(key);
				saucelabConfig.put(key, value);
			}
			input.close();
			prop.clear();

			String url = saucelabConfig.get("protocol") + "://" + saucelabConfig.get("username") + ":"
					+ saucelabConfig.get("access_key") + saucelabConfig.get("url");

			Log.INFO("url :" + url);
			URL remoteDriverURL = new URL(url);

			DesiredCapabilities capability = new DesiredCapabilities();
			input = new FileInputStream(
					currentPath + "/src/main/java/cloudBrowserConfigs/" + cloudBrowserConfigFile + ".properties");
			prop.load(input);
			enuKeys = prop.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = prop.getProperty(key);
				Log.INFO("key :" + key + " Value :" + value);
				capability.setCapability(key, value);
			}
			input.close();

			driver = new RemoteWebDriver(remoteDriverURL, capability);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return driver;
	}

	public static WebDriver CreateWebDriver(String browser) {

		// print initial config data
		String OS = System.getProperty("os.name").toLowerCase();
		String environment = System.getProperty("env").toLowerCase();
		Log.INFO("Operation system: " + OS);
		Log.INFO("Browser: " + browser);
		Log.INFO("Environment: " + environment);
		

		switch (browser.toLowerCase()) {
		case "ff":
		case "firefox":
			driver = new FirefoxDriver();
			break;

		case "ch":
		case "chrome":
			chromeDriverSetup.downloadChromeDriver();
			chromeDriverSetup.unZipIt();
			System.setProperty("webdriver.chrome.driver", chromeDriverSetup.getChromeDriverPath());

			driver = new ChromeDriver();
			break;

		case "ie":
		case "internetexplorer":
			driver = new InternetExplorerDriver();
			break;

		case "safari":
			driver = new SafariDriver();
			break;

		case "saucelab":
			driver = SaucelabDriver();
			break;
		default:
			Log.ERROR("Invalid browser name " + browser);
			System.exit(0);
			break;
		}// switch

		driver.manage().deleteAllCookies();
		driver.manage().window().maximize();
		driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

		return driver;
	}
}