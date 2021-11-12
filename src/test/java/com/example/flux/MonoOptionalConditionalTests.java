package com.example.flux;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * demo use Mono<Option<T>> to replace Conditional branch.
 *
 * suppose :
 *
 *       find product(id, branchId, categoryId)       ** branchId, categoryId may be null
 *
 *                 |--> find branch (branchId)          if branchId    is not null
 *                 |--> find category (categoryId)      if categoryId  is not null
 *
 *                         |--> finalCompose( product, branch, category)
 */

@Log4j2
public class MonoOptionalConditionalTests {
    @Data
    @AllArgsConstructor
    static class Branch{
        String desc;
        String branchId;
    }
    @Data
    @AllArgsConstructor
    static class Category{
        String categoryId;
        String desc;
    }
    @Data
    @AllArgsConstructor
    static class Product{
        String id;
        String branchId;
        String categoryId;
    }

    static Mono<Product> findProd(String id){
        if(id.equalsIgnoreCase("001")){
            return Mono.just(new Product(id, "b001", null));
        }else if(id.equalsIgnoreCase("002")){
            return Mono.just(new Product(id, null, "c002"));
        }else if(id.equalsIgnoreCase("003")){
            return Mono.just(new Product(id, "b003", "c003"));
        }
        return Mono.just(new Product(id, null, null));
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
     * the worst cases
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

    @Test
    void testBetterSolution_without_conditional_branch(){
        findProd("001")
                .flatMap(product->
                    Mono.zip(
                                findBranch2(product.branchId),    // either one Mono<void> can cause ZIP to be cancelled.
                                findCategory2(product.categoryId)
                            )
                            .flatMap(tx->finalCompose2(product, tx.getT1(), tx.getT2()))
                )
                .log()
                .subscribe();
    }

    @Test
    void testFindBranch(){
        findBranch2("123")
                .log()
                .subscribe();

        findBranch2(null)
                .log()
                .subscribe();
    }
}
