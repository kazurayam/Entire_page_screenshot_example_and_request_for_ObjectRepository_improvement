
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.nio.file.Path
import java.text.MessageFormat

import org.apache.commons.lang.text.StrSubstitutor
import org.dom4j.DocumentException
import org.dom4j.Element
import org.dom4j.io.SAXReader
import org.openqa.selenium.JavascriptExecutor

import com.kazurayam.ksbackyard.ScreenshotDriver
import com.kazurayam.ksbackyard.ScreenshotDriver.Options
import com.kazurayam.ksbackyard.ScreenshotDriver.Options.Builder
import com.kazurayam.materials.MaterialRepository
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.constants.StringConstants
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.SelectorMethod
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.util.internal.ExceptionsUtil
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import groovy.json.JsonOutput
import internal.GlobalVariable as GlobalVariable

/**
 * problems/case2_proposal_for_improvment
 *
 */

/**
 * modify com.kms.katalon.core.testobject.ObjectRepository class
 * using Groovy MetaProgramming technique.
 */
def hackObjectRepository() {
	println ">>> hackObjectRepository() was invoked"
	ObjectRepository.metaClass.'static'.findTestObject  = { String testObjectRelativeId, Map<String, Object> variables ->
		println ">>> ObjectRepository#findTestObject(\'" + testObjectRelativeId + "\' was invoked"
		if (testObjectRelativeId == null) {
			logger.logWarning(StringConstants.TO_LOG_WARNING_TEST_OBJ_NULL);
			return null;
		}
	
		String testObjectId = getTestObjectId(testObjectRelativeId);
		logger.logDebug(MessageFormat.format(StringConstants.TO_LOG_INFO_FINDING_TEST_OBJ_W_ID, testObjectId));
	
		// Read test objects cached in temporary in record session.
		//Map<String, TestObject> testObjectsCached = getCapturedTestObjects();
		//
		//if (testObjectRelativeId != null && testObjectsCached.containsKey(testObjectRelativeId)) {
		//	return testObjectsCached.get(testObjectRelativeId);
		//}
	
		File objectFile = new File(RunConfiguration.getProjectDir(), testObjectId + WEBELEMENT_FILE_EXTENSION);
		if (!objectFile.exists()) {
			logger.logWarning(
					MessageFormat.format(StringConstants.TO_LOG_WARNING_TEST_OBJ_DOES_NOT_EXIST, testObjectId));
			return null;
		}
		return readTestObjectFile(testObjectId, objectFile, RunConfiguration.getProjectDir(), variables);
	}
	
	// It was revealed that I can not override the ObjectRepository#findWebUIObject() method because it is private
	// Goovy's bug. See https://issues.apache.org/jira/browse/GROOVY-7368 for detail.
	
	// As an workaround, I will override the ObjectRepository#readTestObjectFile() method as it is public	
	ObjectRepository.metaClass.'static'.readTestObjectFile = { 
				String testObjectId, File objectFile, String projectDir,
				Map<String, Object> variables ->
		println ">>> ObjectRepository#readTestObjectFile(\'" + testObjectId + "\' was invoked"
		try {
            Element rootElement = new SAXReader().read(objectFile).getRootElement();
            String elementName = rootElement.getName();
            if (WEB_ELEMENT_TYPE_NAME.equals(elementName)) {
				TestObject testObject = findWebUIObject(testObjectId, rootElement, variables);
				
				// modify TestObject instance, replace Selector value
				// replace ${Y} and ${X} to 1 and 3
				StrSubstitutor strSubstitutor = new StrSubstitutor(variables)
				SelectorMethod selectorMethod = testObject.getSelectorMethod()
				String selectorValue = testObject.getSelectorCollection().get(selectorMethod)
				String interpolated = strSubstitutor.replace(selectorValue)
				testObject.setSelectorValue(selectorMethod, interpolated)
				//
				return testObject
            }

            if (WEB_SERVICES_TYPE_NAME.equals(elementName)) {
                return findRequestObject(testObjectId, rootElement, projectDir, variables);
            }
            return null;
        } catch (DocumentException e) {
            logger.logWarning(MessageFormat.format(StringConstants.TO_LOG_WARNING_CANNOT_GET_TEST_OBJECT_X_BECAUSE_OF_Y,
                    testObjectId, ExceptionsUtil.getMessageForThrowable(e)));
            return null;
        }
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

//
println ">>> let's see what was returned by ObjectRepository#findTestObject(" + 
		"'Page_LSE_home/tab0_trY_trx',['Y':1,'X':3]'):\n" +
	JsonOutput.prettyPrint(JsonOutput.toJson(
		findTestObject('Page_LSE_home/tab0_trY_tdX', ['Y':1, 'X':3])
	)
)

// Take full page screenshot of the Stock Exchange home page,
// save it into a PNG file in the Materials directory
MaterialRepository mr = (MaterialRepository)GlobalVariable.MATERIAL_REPOSITORY
Path fileFnamedByURL = mr.resolveScreenshotPath(GlobalVariable.CURRENT_TESTCASE_ID, url)
Options options = new ScreenshotDriver.Options.Builder().
		timeout(300).
		addIgnoredElement(findTestObject('Object Repository/Page_LSE_home/slideshow-wrapper-news')).
		
		addIgnoredElement(findTestObject('Page_LSE_home/tab0_trY_tdX', ['Y':1, 'X':3])).
		/* Fails here
		 * org.openqa.selenium.InvalidSelectorException: Given xpath expression
		 * "//div[@id="tab0"]/div/table/tbody/tr[${Y}]/td[${X}]" is invalid:
		 * SyntaxError: The expression is not a legal expression.
		 */
		
		addIgnoredElement(findTestObject('Page_LSE_home/tab0_trY_tdX', ['Y':1, 'X':5])).
		addIgnoredElement(findTestObject('Page_LSE_home/tab0_trY_tdX', ['Y':2, 'X':3])).
		addIgnoredElement(findTestObject('Page_LSE_home/tab0_trY_tdX', ['Y':2, 'X':5])).
		build()
CustomKeywords.'com.kazurayam.ksbackyard.ScreenshotDriver.saveEntirePageImage'(
	fileFnamedByURL.toFile(),
	options)

WebUI.closeBrowser()
