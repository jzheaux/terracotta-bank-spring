package com.joshcummings.codeplay.terracotta.testng;

import com.joshcummings.codeplay.terracotta.Mainer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class TomcatSupport {
	private static ConfigurableApplicationContext context;

	public void startContainer() throws Exception {
		this.context = SpringApplication.run(Mainer.class);
	}
	
	public void stopContainer() throws Exception {
		this.context.close();
	}
}
