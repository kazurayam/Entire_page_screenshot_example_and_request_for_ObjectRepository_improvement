
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.nio.file.Path
import java.text.MessageFormat
import java.util.Map.Entry

import org.apache.commons.lang.text.StrSubstitutor;
import org.dom4j.Element;
import org.openqa.selenium.JavascriptExecutor

import com.kazurayam.ksbackyard.ScreenshotDriver
import com.kazurayam.ksbackyard.ScreenshotDriver.Options
import com.kazurayam.ksbackyard.ScreenshotDriver.Options.Builder
import com.kazurayam.materials.MaterialRepository
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.constants.StringConstants;
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.SelectorMethod
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.TestObjectProperty
import com.kms.katalon.core.testobject.TestObjectXpath
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
	//
	//ObjectRepository.metaClass.'static'.invokeMethod = { String name, args ->
	//	println ">>> ObjectRepository#${name} was invoked"
	//	def result
	//	try {
	//		result = delegate.metaClass.getMetaMethod(name, args).invoke(delegate, args)
	//	} catch(Exception e) {
	//		System.out.println("Handling exception for method $name()")
	//	}
	//	return result
	//}
	//
	ObjectRepository.metaClass.'static'.findTestObject  = { String testObjectRelativeId, Map<String, Object> variables ->
		println ">>> ObjectRepository#findTestObject(" + testObjectRelativeId + " was invoked"
		if (testObjectRelativeId == null) {
			logger.logWarning(StringConstants.TO_LOG_WARNING_TEST_OBJ_NULL);
			return null;
		}
	
		String testObjectId = getTestObjectId(testObjectRelativeId);
		logger.logDebug(MessageFormat.format(StringConstants.TO_LOG_INFO_FINDING_TEST_OBJ_W_ID, testObjectId));
	
		// Read test objects cached in temporary in record session.
		Map<String, TestObject> testObjectsCached = getCapturedTestObjects();
	
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
	//
	ObjectRepository.metaClass.'static'.findWebUIObject = { String testObjectId, Element element, Map<String, Object> variables ->
		
		println ">>> hacked ObjectRepository#findWebUIObject(" + testObjectId + ",...) was invoked"
		
		TestObject testObject = new TestObject(testObjectId);

		// For image
		Element imagePathElement = element.element("imagePath");
		if (imagePathElement != null) {
			String imagePath = imagePathElement.getText();
			testObject.setImagePath(imagePath);
		}

		Element relativeImagePathElement = element.element("useRalativeImagePath");
		if (relativeImagePathElement != null) {
			String useRelavitePathString = relativeImagePathElement.getText();
			testObject.setUseRelativeImagePath(Boolean.parseBoolean(useRelavitePathString));
		}

		Element dfSelectorMethodElement = element.element(PROPERTY_SELECTOR_METHOD);
		if (dfSelectorMethodElement != null) {
			testObject.setSelectorMethod(SelectorMethod.valueOf(dfSelectorMethodElement.getText()));
		}

		Element propertySelectorCollection = element.element(PROPERTY_SELECTOR_COLLECTION);
		if (propertySelectorCollection != null) {
			List<?> selectorEntry = propertySelectorCollection.elements(PROPERTY_ENTRY);
			if (selectorEntry != null) {
				for (Object entry: selectorEntry) {
					Element selectorMethodElement = ((Element) entry);
					SelectorMethod entryKey = SelectorMethod.valueOf(selectorMethodElement.elementText(PROPERTY_KEY));
					String entryValue = selectorMethodElement.elementText(PROPERTY_VALUE);
					testObject.setSelectorValue(entryKey, entryValue);
				}
			}
		}

		for (Object propertyElementObject : element.elements(WEB_ELEMENT_PROPERTY_NODE_NAME)) {
			TestObjectProperty objectProperty = new TestObjectProperty();
			Element propertyElement = (Element) propertyElementObject;

			String propertyName = StringEscapeUtils.unescapeXml(propertyElement.elementText(PROPERTY_NAME));
			ConditionType propertyCondition = ConditionType
					.fromValue(StringEscapeUtils.unescapeXml(propertyElement.elementText(PROPERTY_CONDITION)));
			String propertyValue = StringEscapeUtils.unescapeXml(propertyElement.elementText(PROPERTY_VALUE));
			boolean isPropertySelected = Boolean
					.valueOf(StringEscapeUtils.unescapeXml(propertyElement.elementText(PROPERTY_IS_SELECTED)));

			objectProperty.setName(propertyName);
			objectProperty.setCondition(propertyCondition);
			objectProperty.setValue(propertyValue);
			objectProperty.setActive(isPropertySelected);

			// Check if this element is inside a frame
			if (Arrays.asList(PARENT_FRAME_ATTRS).contains(propertyName) && isPropertySelected) {
				TestObject parentObject = findTestObject(propertyValue);
				testObject.setParentObject(parentObject);
			} else if (PARENT_SHADOW_ROOT_ATTRIBUTE.equals(propertyName)) {
				testObject.setParentObjectShadowRoot(true);
			} else {
				testObject.addProperty(objectProperty);
			}
		}
		
		for (Object xpathElementObject : element.elements(WEB_ELEMENT_XPATH_NODE_NAME)) {
			TestObjectXpath objectXpath = new TestObjectXpath();
			Element xpathElement = (Element) xpathElementObject;

			String propertyName = StringEscapeUtils.unescapeXml(xpathElement.elementText(PROPERTY_NAME));
			ConditionType propertyCondition = ConditionType
					.fromValue(StringEscapeUtils.unescapeXml(xpathElement.elementText(PROPERTY_CONDITION)));
			String propertyValue = StringEscapeUtils.unescapeXml(xpathElement.elementText(PROPERTY_VALUE));
			boolean isPropertySelected = Boolean
					.valueOf(StringEscapeUtils.unescapeXml(xpathElement.elementText(PROPERTY_IS_SELECTED)));

			objectXpath.setName(propertyName);
			objectXpath.setCondition(propertyCondition);
			objectXpath.setValue(propertyValue);
			objectXpath.setActive(isPropertySelected);

			// Check if this element is inside a frame
			if (Arrays.asList(PARENT_FRAME_ATTRS).contains(propertyName) && isPropertySelected) {
				TestObject parentObject = findTestObject(propertyValue);
				testObject.setParentObject(parentObject);
			} else if (PARENT_SHADOW_ROOT_ATTRIBUTE.equals(propertyName)) {
				testObject.setParentObjectShadowRoot(true);
			} else {
				testObject.addXpath(objectXpath);
			}
		}

		if (testObject == null || variables == null || variables.isEmpty()) {
			return testObject;
		}
		Map<String, Object> variablesStringMap = new HashMap<String, Object>();
		for (Entry<String, Object> entry : variables.entrySet()) {
			variablesStringMap.put(String.valueOf(entry.getKey()), entry.getValue());
		}

		StrSubstitutor strSubtitutor = new StrSubstitutor(variablesStringMap);
		for (TestObjectProperty objectProperty : testObject.getProperties()) {
			objectProperty.setValue(strSubtitutor.replace(objectProperty.getValue()));
		}

		return testObject;
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

// let's see what is retured by the ObjectRepository#findTestObject() call
println ">>> findTestObject('Page_LSE_home/tab0_trY_trx',['Y':1,'X':3]'):\n" +
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
