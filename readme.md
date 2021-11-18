
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