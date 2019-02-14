
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.nio.file.Path
import java.text.MessageFormat

import org.openqa.selenium.JavascriptExecutor

import com.kazurayam.ksbackyard.ScreenshotDriver
import com.kazurayam.ksbackyard.ScreenshotDriver.Options
import com.kazurayam.ksbackyard.ScreenshotDriver.Options.Builder
import com.kazurayam.materials.MaterialRepository
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.constants.StringConstants
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable as GlobalVariable

/**
 * Test Cases/problems/case1_proposal_for_improvement
 */

/**
 * modify com.kms.katalon.core.testobject.ObjectRepository
 * using Groovy MetaProgramming technique.
 * 
 * the source code is here: https://github.com/katalon-studio/katalon-studio-testing-framework/blob/master/Include/scripts/groovy/com/kms/katalon/core/testobject/ObjectRepository.java
 */
def hackObjectRepository() {
	ObjectRepository.metaClass.'static'.findTestObject = { String testObjectRelativeId, Map<String, Object> variables ->
		println ">>> ObjectRepository#findTestObject(" + testObjectRelativeId + ") was called"
		if (testObjectRelativeId == null) {
			logger.logWarning(StringConstants.TO_LOG_WARNING_TEST_OBJ_NULL);
			return null;
		}
		String testObjectId = getTestObjectId(testObjectRelativeId);
		logger.logDebug(MessageFormat.format(StringConstants.TO_LOG_INFO_FINDING_TEST_OBJ_W_ID, testObjectId));
		// Read test objects cached in temporary in record session.
		Map<String, TestObject> testObjectsCached = getCapturedTestObjects();
		if (testObjectRelativeId != null && testObjectsCached.containsKey(testObjectRelativeId)) {
			return testObjectsCached.get(testObjectRelativeId);
		}
		File objectFile = new File(RunConfiguration.getProjectDir(), testObjectId + WEBELEMENT_FILE_EXTENSION);
		if (!objectFile.exists()) {
			//logger.logWarning(
			//		MessageFormat.format(StringConstants.TO_LOG_WARNING_TEST_OBJ_DOES_NOT_EXIST, testObjectId));
			//return null;
			throw new FileNotFoundException(objectFile.toString() + " is not found")
		}
		return readTestObjectFile(testObjectId, objectFile, RunConfiguration.getProjectDir(), variables);
	}
}
hackObjectRepository()

URL url = new URL('https://www.londonstockexchange.com/home/homepage.htm')

WebUI.openBrowser('')
WebUI.setViewPortSize(1279, 720)
WebUI.navigateToUrl(url.toExternalForm())

// wait for the bannar to appear
WebUI.verifyElementPresent(
	findTestObject('Page_LSE_home/slideshow-wrapper-news'),
	10, FailureHandling.STOP_ON_FAILURE)

// modify the style of a few elements in the page
// to make the screenshot pretty looking
// see https://github.com/pazone/ashot/issues/126
JavascriptExecutor js = (JavascriptExecutor)DriverFactory.getWebDriver()
js.executeScript("document.head.appendChild(document.createElement(\"style\"))" +
	".innerHTML = \"#bannerCookie {position: static !important; }\"")

// wait for the Index data to appear
WebUI.verifyElementPresent(
	findTestObject('Object Repository/Page_LSE_home/tab0_tr1_td3'),
	15, FailureHandling.CONTINUE_ON_FAILURE)

// Take full page screenshot of the Stock Exchange home page,
// save it into a PNG file in the Materials directory
MaterialRepository mr = (MaterialRepository)GlobalVariable.MATERIAL_REPOSITORY
Path fileFnamedByURL = mr.resolveScreenshotPath(GlobalVariable.CURRENT_TESTCASE_ID, url)
Options options = new ScreenshotDriver.Options.Builder().
						timeout(300).
						addIgnoredElement(findTestObject('Object Repository/Page_LSE_home/slideshow-wrapper-news')).
						addIgnoredElement(findTestObject('Object Repository/Page_LSE_home/tab0_tr1_td3')).
						addIgnoredElement(findTestObject('Object Repository/Page_LSE_home/tab0_tr1_td5')).
						
						addIgnoredElement(findTestObject('Object Repository/Page_LSE_home/tab0_tr2_td3')).
						addIgnoredElement(findTestObject('Object Repository/Page_LSE_home/tab0_tr2_td5')).   // ERROR
						
						build()
CustomKeywords.'com.kazurayam.ksbackyard.ScreenshotDriver.saveEntirePageImage'(
	fileFnamedByURL.toFile(),
	options)

WebUI.closeBrowser()
