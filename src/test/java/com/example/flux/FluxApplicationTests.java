package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class FluxApplicationTests {

	@Test
	void just() {
		Flux<String> color = Flux.just("red", "yellow");
		color.log().subscribe(System.out::println);
	}

	@Test
	void mapExample() {
		Flux<String> fluxColors = Flux.just("red", "green", "blue");
		fluxColors.map(color -> color.charAt(0)).subscribe(System.out::println);
	}

	@Test
	void zipExample() {
		Flux<String> fluxFruits = Flux.just("apple", "pear", "plum");
		Flux<String> fluxColors = Flux.just("red", "green", "blue");
		Flux<Integer> fluxAmounts = Flux.just(10, 20, 30);
		Flux.zip(fluxFruits, fluxColors, fluxAmounts).subscribe(System.out::println);
	}

	@Test
	public void onErrorExample() {
		Flux<String> fluxCalc = Flux.just(-1, 2, 0, 4)
				.map(i -> "10 / " + i + " = " + (10 / i));
		fluxCalc.subscribe(
				value -> System.out.println("Next: " + value),
				error -> System.err.println("Error: " + error),
				()->System.out.println("--- Done ---" )
				);
	}

	@Test
	public void onErrorReturnExample() {
		Flux<String> fluxCalc = Flux.just(-1, 0, 1)
				.map(i -> "10 / " + i + " = " + (10 / i))
				.onErrorReturn(ArithmeticException.class, "Division by 0 not allowed");

		fluxCalc.subscribe(value -> System.out.println("Next: " + value),
				error -> System.err.println("Error: " + error));
	}

	@Test
	public void onErrorResumeExample() {
		Flux<String> fluxCalc = Flux.just(-1, 0, 1)
				.map(i -> "10 / " + i + " = " + (10 / i))
				.onErrorResume((e)-> Flux.just("None"));

		fluxCalc.subscribe(value -> System.out.println("Next: " + value),
				error -> System.err.println("Error: " + error));
	}

	@Test
	public void stepVerifierTest() {
		Flux<String> fluxCalc = Flux.just(-1, 0, 1)
				.map(i -> "10 / " + i + " = " + (10 / i));

		StepVerifier.create(fluxCalc)
				.expectNextCount(1)
				.expectError(ArithmeticException.class)
				.verify();
	}

	@Test
	void testGenFlux(){
		Flux<String> flux = Flux.generate(
				() -> 0,
				(state, sink) -> {
					sink.next("3 x " + state + " = " + 3*state);
					if (state == 10) sink.complete();
					return state + 1;
				});
		flux.log().blockLast();
	}

	@Test
	void testFlatMap1(){
		String[] p1 = new String[]{"aaa", "bbb"};
		String[] p2 = new String[]{"hello", "world"};
		Flux<String[]> flux = Flux.just(
				p1, p2);

		flux.log()
				.flatMap(x-> Flux.just(x))
				.log()
				.blockLast();
	}

	@Test
	void testFlatMap2(){
		Flux<String> flux = Flux.just(
				"aaa bbb", "hello world");

		flux.log()
				.flatMap(x-> Flux.just(x.split(" ")))
				.log()
				.blockLast();
	}

	@Test
	void testFlatMap3(){
		Flux<String> flux = Flux.just(
				"aaa");

		flux.log()
				.map(x-> Mono.just(x))
				//.log()
				.flatMap(x->x)
				.log()
				.blockLast();
	}
}
