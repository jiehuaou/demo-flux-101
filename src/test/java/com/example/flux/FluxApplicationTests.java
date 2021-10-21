package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Supplier;

//@SpringBootTest
class FluxApplicationTests {

	@Test
	void just() {
		Flux<String> color = Flux.just("red", "yellow");
		color.log().subscribe(System.out::println);
	}

	/**
	 * [a1,a2, ...] -> map( λ ) -> [b1, b2, ...]
	 */

	@Test
	void mapExample() {
		Flux<String> fluxColors = Flux.just("red", "green", "blue");
		fluxColors.map(color -> color.charAt(0)).subscribe(System.out::println);
	}

	/**
	 * combine 2 or more flux,
	 * zip(flux1, flux2 ...) -> [a1, b1, ...] [a2, b2 ...]
	 */
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

	/**
	 * onErrorReturn( T arg ) -> return this arg then abort
	 */
	@Test
	public void onErrorReturnExample() {
		Flux<String> fluxCalc = Flux.just(2, 1, 0, 3)
				.map(i -> "10 / " + i + " = " + (10 / i))
				.onErrorReturn(ArithmeticException.class, "Division by 0 not allowed");

		fluxCalc.subscribe(value -> System.out.println("Next: " + value),
				error -> System.err.println("Error: " + error));
	}

	/**
	 * onErrorResume(Publisher argFlux) -> Subscribe argFlux
	 */
	@Test
	public void onErrorResumeExample() {
		Flux<String> fluxCalc = Flux.just(-1, 0, 1, 2)
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

	/**
	 * Programmatically create a Flux by generating signals one-by-one via a consumer callback and some state.
	 */
	@Test
	void testGenerateFlux(){
		Flux<String> flux = Flux.generate(
				() -> 0,
				(state, sink) -> {
					sink.next("3 x " + state + " = " + 3*state);
					if (state == 10) {
						sink.complete();
					}
					return state + 1;
				});
		flux.log().blockLast();
	}

	/**
	 * flatMap(Publisher argFlux) -> Subscribe argFlux
	 */

	@Test
	void testFlatMap1(){
		String[] p1 = new String[]{"aaa", "bbb"};
		String[] p2 = new String[]{"hello", "world"};
		Flux<String[]> flux = Flux.just(
				p1, p2);

		flux
				.flatMap(x-> Flux.just(x))
				.doOnNext(System.out::println)
				.blockLast();
	}

	/**
	 * flatMap(Publisher argFlux) -> Subscribe argFlux
	 */
	@Test
	void testFlatMap2(){
		Flux<String> flux = Flux.just(
				"aaa bbb", "hello world");

		flux
				.flatMap(x-> Flux.just(x.split(" ")))
				.doOnNext(System.out::println)
				.blockLast();
	}

	/**
	 * flatMap(Publisher argFlux) -> Subscribe argFlux
	 */
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

//	Mono<Void> getMonoVoid(){
//		return Mono.just("OK").then();
//	}

	/**
	 * doOnNext() never happen on Mono<Void>
	 */
	@Test
	void testMonoVoid(){

		Mono<Void> mono1 = Mono.empty();

		mono1
			.doOnNext(el->System.out.println(el))   // never happen
			.doOnSuccess(el->System.out.println("--- done 1 ---"))
			.subscribe();



		Supplier<Mono<Void>> func = ()->Mono.just("OK").then();
		Mono<Void> mono2 = func.get();

		mono2
			.doOnNext(el->System.out.println(el))   // never happen
			.doOnSuccess(el->System.out.println("--- done 2 ---"))
			.subscribe();
	}

	/**
	 * doOnNext( λ ) do not change the element of flux
	 *
	 * [a1,a2, ...] -> doOnNext( λ ) -> [a1, a2, ...]
	 */
	@Test
	void testDoOnNext(){

		Flux<String> data = Flux.just("hello", "world");
		data
			.doOnNext(el->System.out.println("do1 -> " + el))   // happen 2 times
			.doOnNext(el->System.out.println("do2 -> " + el))   // happen 2 times
			.doFinally(el->System.out.println("--- finally done ---"))
			.subscribe();
	}
}
