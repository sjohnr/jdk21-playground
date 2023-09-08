/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Riesenberg
 */
public class SecurityContextHolderPerformanceTest {

	private static final String NAME = "Spring Framework";

	private static final int NUM_THREADS = 500_000;

	private CountDownLatch countDownLatch;

	private static final Runnable TASK1 = () -> {
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(new TestingAuthenticationToken("Spring Framework", null));
		SecurityContextHolder.setContext(securityContext);
	};

	private static final Runnable TASK2 = () -> {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		assertThat(name).isSameAs(NAME);
		SecurityContextHolder.clearContext();
	};

	@BeforeEach
	public void setUp() {
		this.countDownLatch = new CountDownLatch(NUM_THREADS);
	}

	@Test
	public void test1() throws Exception {
		try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
			runTest(executor, 1);
		}
	}

	@Test
	public void test2() throws Exception {
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			runTest(executor, 2);
		}
	}

	private void runTest(Executor executor, int number) throws Exception {
		List<Task> tasks = new ArrayList<>(NUM_THREADS);
		for (int i = 0; i < NUM_THREADS; i++) {
			Task task = new Task(TASK1, TASK2, () -> this.countDownLatch.countDown());
			tasks.add(task);
			executor.execute(task);
		}
		this.countDownLatch.await();

		long total = tasks.stream().mapToLong(t -> t.elapsedTime).sum();
		System.out.printf("Test %d took an average of %s ns per thread%n", number, total * 1.0 / NUM_THREADS);
	}

	static class Task implements Runnable {
		private final List<Runnable> delegates;

		long elapsedTime;

		Task(Runnable... delegates) {
			this.delegates = List.of(delegates);
		}

		@Override
		public void run() {
			long start = System.nanoTime();
			this.delegates.forEach(Runnable::run);
			this.elapsedTime = System.nanoTime() - start;
		}
	}

}
