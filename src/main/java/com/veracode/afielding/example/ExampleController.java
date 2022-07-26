package com.veracode.afielding.example;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.Feature;


@RestController
public class ExampleController {
	@Autowired
	private ResourceLoader resourceLoader;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public void index(HttpServletResponse response) throws Exception {
		Resource templateResource = resourceLoader.getResource("classpath:example.xslt");
		Resource inputResource = resourceLoader.getResource("classpath:input.xml");



		// Run the input through this strategy courtesy of OWASP (https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxb-unmarshaller)
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		//Do unmarshall operation
		Source inputSource = new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource(inputResource.getInputStream()));



		TransformerFactoryImpl factory = (TransformerFactoryImpl) TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", getClass().getClassLoader());
		factory.setAttribute(Feature.ALLOWED_PROTOCOLS.name, "");
		factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
		factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");



		Source templateSource = new StreamSource(templateResource.getInputStream());
		Templates templates = factory.newTemplates(templateSource);



		Transformer transformer = templates.newTransformer();
		transformer.transform(inputSource, new StreamResult(response.getOutputStream()));
	}
}