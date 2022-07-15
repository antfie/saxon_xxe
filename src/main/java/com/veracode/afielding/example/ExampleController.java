package com.veracode.afielding.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Templates;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;

import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.spi.XmlReader;


@RestController
public class ExampleController {
	@Autowired
	private ResourceLoader resourceLoader;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public void index(HttpServletResponse response) throws Exception {
		Resource templateResource = resourceLoader.getResource("classpath:example.xslt");
		Source templateSource = new StreamSource(templateResource.getInputStream());

		Resource inputResource = resourceLoader.getResource("classpath:input.xml");
		Source inputSource = new StreamSource(inputResource.getInputStream());




		//
		// Security Shunt as strategy on input source ???
		//



		//
		// Attempt 1 with XMLInputFactory = FAIL
		//

		// XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

		// // This disables DTDs entirely for that factory
		// xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		// // This causes XMLStreamException to be thrown if external DTDs are accessed.
		// xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		// // disable external entities
		// xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

		// XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputResource.getInputStream());
		
		// inputSource = ? this reader some how

		

		
		//
		// Attempt 2 with XMLReader = FAIL
		//

		// XMLReader xmlReader = XMLReaderFactory.createXMLReader();

		// xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		// xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		// xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

		// xmlReader.parse(new InputSource(inputResource.getInputStream()));  // parse xml
		// reader to ?


		

		
		//
		// Attempt 3 with SAXParserFactory = FAIL
		//

		// SAXParserFactory spf = SAXParserFactory.newInstance();
		// spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		// SAXParser p = spf.newSAXParser();
		// p.getXMLReader();
		// p.parse(new InputSource(inputResource.getInputStream()));


		//
		// Security Shunt as strategy on input source ???
		//



		Result result = new StreamResult(response.getOutputStream());

		TransformerFactoryImpl factory = (TransformerFactoryImpl) TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", getClass().getClassLoader());

		// (1)
		// This does not address the issue
		//factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
		
		// (2)
		// These don't work - exception: "Unknown attribute"
		//factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		//factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

		// (3)
		// Reference
		// https://www.saxonica.com/documentation11/#!sourcedocs/resources-and-uris/resolving-entities
		// "The value "" (empty string) disallows all external resource access." - https://www.saxonica.com/documentation11/#!configuration/config-features@ALLOWED_PROTOCOLS
		//
		// "In Saxon 10 we introduced a configuration property Feature.ALLOWED_PROTOCOLS" - https://saxonica.plan.io/issues/4729
		//
		// Results in exception: "Unknown attribute"
		//factory.setAttribute("http://saxon.sf.net/feature/allowedProtocols", "");

		// (4)
		// factory.setURIResolver(null);



		Templates templates = factory.newTemplates(templateSource);

		Transformer transformer = templates.newTransformer();
		transformer.transform(inputSource, result);
	}
}