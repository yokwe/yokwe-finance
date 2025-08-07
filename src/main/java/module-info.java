open module yokwe.finance {
	exports yokwe.finance;
	
	// yokwe-util
	requires transitive yokwe.util;	
	
	// selenium
	requires transitive org.seleniumhq.selenium.api;
	requires transitive org.seleniumhq.selenium.json;
	requires transitive org.seleniumhq.selenium.http;
	requires transitive org.seleniumhq.selenium.os;
	requires transitive org.seleniumhq.selenium.remote_driver;
	//
	requires org.seleniumhq.selenium.chrome_driver;
	requires org.seleniumhq.selenium.support;
	requires com.google.common;

	requires jul.to.slf4j;
	requires org.apache.httpcomponents.core5.httpcore5.h2;
}