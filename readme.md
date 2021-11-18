
## demo Backpressure
* consumer with request(N)
* use onBackpressureBuffer(max) to park up to maxSize elements when not enough demand is requested.
* limit rate ( prefetchRate ) 
    -> first request ( prefetchRate such as 100 ) 
    -> second request ( prefetchRate * 75% = 75 )
    -> and so on
* drop event when overflow occur.

```java
public class BackpressureTests {}
```  
  
## demo hot vs cold publisher
hot publisher:
 1) do not create new data producer for each new subscription,
 2) Instead there will be only one data producer and all the observers listen to the data produced by the single data producer.
 3) So all the observers get the same data.

cold publisher:
 1) by default do not produce any value unless at least 1 observer subscribes to it.
 2) Publishers create new data producers for each new subscription.
```java
public class ColdHotPublisherTests {}
```

## demo complex service combination

```java
// service1() 1 -----> * service2() 1 --> * service3()
//                |--> 1 service4()

public class CombiningTests {
    static Flux<Response1> service1(){
        return Flux
                .just(new Response1("a1", "a2"))
                .delayElements(Duration.ofMillis(4));
    }
    static Flux<Response2> service2(String a1){
        return Flux
                .just(new Response2("b1-" + a1), new Response2("b2-" + a1))
                .delayElements(Duration.ofMillis(3));
    }
    static Flux<Response3> service3(String b1){
        return Flux
                .just(new Response3("c1-" + b1), new Response3("c2-" + b1))
                .delayElements(Duration.ofMillis(5));
    }
    static Mono<Response4> service4(String a2){
        return Mono
                .just(new Response4("d1-" + a2))
                .delayElement(Duration.ofMillis(8));
    }
    // how to compose service1 with other service ?
    
}
```
https://stackoverflow.com/questions/54543039/webflux-chaining-to-call-multiple-services-and-response-aggregation

## demo use Mono<Option< T >> to replace Conditional branch.
```java
/**
 * 
 * suppose :
 *
 *       find-product (id),    -->Mono<Product(id,branchId, categoryId)>        ** branchId, categoryId may be null
 *
 *                 |--> find-branch (branchId)          --> Mono<branch>
 *                 |--> find-category (categoryId)      --> Mono<categoryId>
 *
 *                         |--> finalCompose( product, branch, category)
 */

@Log4j2
public class MonoOptionalConditionalTests {
    /**
     * this is example for bad coding style with a lot of conditional branch.
     */
    @Test
    void testBadConditional(){
        findProd("001")
                .flatMap(product->{
                    if(product.branchId!=null && product.categoryId!=null){
                        return Mono.zip(
                                findBranch(product.branchId),
                                findCategory(product.categoryId)
                        ).flatMap(t2->finalCompose(product, t2.getT1(), t2.getT2()));
                    }else if(product.branchId!=null  && product.categoryId==null){
                        return findBranch(product.branchId)
                                .flatMap(branch->finalCompose(product, branch, null));
                    }else if(product.branchId==null  && product.categoryId!=null){
                        return findCategory(product.categoryId)
                                .flatMap(category->finalCompose(product, null, category));
                    }
                    return finalCompose(product, null, null);
                })
                .block();
    }

    //////-------------------- how to improve ? ------------
}

```
