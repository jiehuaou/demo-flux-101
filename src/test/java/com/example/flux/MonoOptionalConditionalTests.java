package com.example.flux;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

/**
 * demo use Mono<Option<T>> to replace Conditional branch.
 *
 * suppose :
 *
 *       find-product (id),    -->Mono<Product(id, branchId, categoryId)>        ** branchId, categoryId may be null
 *
 *                 |--> find-branch (branchId)          --> Mono<branch>
 *                 |--> find-category (categoryId)      --> Mono<categoryId>
 *
 *                         |--> finalCompose( product, branch, category)
 */

@Log4j2
public class MonoOptionalConditionalTests {
    @Data
    @AllArgsConstructor
    static class Branch{
        String branchId;
        String desc;
    }
    @Data
    @AllArgsConstructor
    static class Category{
        String categoryId;
        String desc;
    }
    @Data
    @AllArgsConstructor
    @Builder
    static class Product{
        String id;
        String branchId;
        String categoryId;
    }

    static Mono<Product> findProd(String id){
        Product.ProductBuilder builder = Product.builder().id(id);
        if(id.equalsIgnoreCase("001")){
            builder.branchId("b001").categoryId(null);
        }else if(id.equalsIgnoreCase("002")){
            builder.branchId(null).categoryId("c002");
        }else if(id.equalsIgnoreCase("003")){
            builder.branchId("b003").categoryId("c003");
        }

        Product product = builder.build();
        log.info("build product --> {}", product);
        // return
        return Mono.just(product);
    }
    static Mono<Branch> findBranch(String id){
        return Mono.just(new Branch(id, "sample Branch"));
    }
    static Mono<Category> findCategory(String id){
        return Mono.just(new Category(id, "sample Category"));
    }

    static Mono<String> finalCompose(Product product, Branch branch, Category category){
        // do some final process.
        log.info("final Product-->{}", product);
        log.info("final Branch-->{}", branch);
        log.info("final Category-->{}", category);
        return Mono.just("done");
    }

    /**
     * this is example for bad coding with a lot of conditional branch.
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

    //////-------------------- improve ------------

    /**
     * return Mono<Optional<T>> can avoid Mono.zip()  to be cancelled.
     */
    static Mono<Optional<Branch>> findBranch2(String branchId){
        log.info("start : query branch by {}", branchId);
        return Mono
                .justOrEmpty(branchId)
                .flatMap(e->{
                    // suppose to do some query if branchId is not null
                    log.info("done : query branch by {}", branchId);
                    return Mono.just(new Branch(branchId, "sample Branch"));
                })
                .map(e->Optional.of(e))
                //.switchIfEmpty(Mono.just(Optional.empty()))
                .defaultIfEmpty(Optional.empty())
                ;
    }
    /**
     * return Mono<Optional<T>> can avoid Mono.zip()  to be cancelled.
     */
    static Mono<Optional<Category>> findCategory2(String categoryId){
        log.info("start : query Category by {}", categoryId);
        return Mono
                .justOrEmpty(categoryId)
                .flatMap(e->{
                    // suppose to do some query
                    log.info("done : query Category by {}", categoryId);
                    return Mono.just(new Category(categoryId, "sample Category"));
                })
                .map(e->Optional.of(e))
                //.switchIfEmpty(Mono.just(Optional.empty()))
                .defaultIfEmpty(Optional.empty())
                ;
    }

    static Mono<String> finalCompose2(Product product, Optional<Branch> branch, Optional<Category> category){
        // do final process
        log.info("final Product-->{}", product);
        log.info("final Branch-->{}", branch);
        log.info("final Category-->{}", category);
        return Mono.just("done");
    }


    @ParameterizedTest
    @ValueSource(strings = { "001", "002", "003", "004" })
    void testBetterSolution_without_conditional_branch(String id){
        log.info("=============== start of {} ==============", id);
        findProd(id)
                .flatMap(product->
                    Mono.zip(
                            findBranch2(product.branchId),    // either one return Mono<void>, can cause ZIP to be cancelled.
                            findCategory2(product.categoryId) // either one return Mono<void>, can cause ZIP to be cancelled.
                        )
                        .flatMap(tx->finalCompose2(product, tx.getT1(), tx.getT2()))
                )
                //.log()
                .subscribe(s->log.info(s));

        log.info("=============== end of {} ==============", id);
    }

    @Test
    void testFindBranch(){

        StepVerifier.create(findBranch2("123"))
                .expectNextMatches(s->s.get().branchId.equalsIgnoreCase("123"))
                .expectComplete()
                .verify();

        StepVerifier.create(findBranch2(null))
                .expectNext(Optional.empty())
                .expectComplete()
                .verify();
    }
}
