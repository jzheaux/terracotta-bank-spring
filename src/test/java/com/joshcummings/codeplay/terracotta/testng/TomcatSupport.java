package com.joshcummings.codeplay.terracotta.testng;

import com.joshcummings.codeplay.terracotta.Mainer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class TomcatSupport {
	private static ConfigurableApplicationContext context;

	public ApplicationContext startContainer() throws Exception {
		this.context = SpringApplication.run(Mainer.class);
		return this.context;
	}
	
	public void stopContainer() throws Exception {
		this.context.close();
	}
}
