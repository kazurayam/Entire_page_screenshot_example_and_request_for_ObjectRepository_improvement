import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.nio.file.Path

import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver

import com.kazurayam.materials.MaterialRepository
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable as GlobalVariable

/**
 * StepByStep/TC_step3 - Entire page screenshot
 *
 */

URL url = new URL('https://www.nasdaq.com/')

WebUI.openBrowser('')
WebUI.setViewPortSize(1279, 720)
WebUI.navigateToUrl(url.toExternalForm())

// wait for the bannar to appear
WebUI.verifyElementPresent(
	findTestObject('Page_NASDAQ_top/div_global_banner'),
	20, FailureHandling.STOP_ON_FAILURE)


// modify the style of a few elements in the page 
// to make the screenshot pretty looking
// see https://github.com/pazone/ashot/issues/126
JavascriptExecutor js = (JavascriptExecutor)DriverFactory.getWebDriver()
js.executeScript("document.head.appendChild(document.createElement(\"style\")).innerHTML = \"#global_nav {position: static !important; }\"")
js.executeScript("document.head.appendChild(document.createElement(\"style\")).innerHTML = \"#cookieConsent {position: static !important; }\"")
js.executeScript("document.head.appendChild(document.createElement(\"style\")).innerHTML = \"#global_banner {display:none !important; }\"")


// wait for the Index data to appear
WebUI.verifyElementPresent(
	findTestObject('Object Repository/Page_NASDAQ_top/indexTable_tr1_td2'),
	15, FailureHandling.STOP_ON_FAILURE)

// Take screenshot and save into a PNG file in the Materials directory
MaterialRepository mr = (MaterialRepository)GlobalVariable.MATERIAL_REPOSITORY
Path fileFnamedByURL = mr.resolveScreenshotPath(GlobalVariable.CURRENT_TESTCASE_ID, url)
CustomKeywords.'com.kazurayam.ksbackyard.ScreenshotDriver.saveEntirePageImage'(
	fileFnamedByURL.toFile())

WebUI.closeBrowser()