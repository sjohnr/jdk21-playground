package com.example;

import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	@DisabledOnJre(JRE.JAVA_17)
	void virtualThreadsAreAvailable() throws Exception {
		ThreadFactory threadFactory = new VirtualThreadTaskExecutor().getVirtualThreadFactory();
		Thread thread = threadFactory.newThread(() -> System.out.println("Hi, I'm on a virtual thread! " + Thread.currentThread().getName()));
		thread.start();
		thread.join();
	}

}
