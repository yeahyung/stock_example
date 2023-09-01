package com.example.stock_example.facade;

import com.example.stock_example.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockStockFacade {

    private final RedissonClient redissonCLient;
    private final StockService stockService;

    public RedissonLockStockFacade(RedissonClient redissonCLient, StockService stockService) {
        this.redissonCLient = redissonCLient;
        this.stockService = stockService;
    }

    /*
    Redisson
        - channel 을 하나 생성 & lock 을 획득중인 thread 가 channel 에 lock 해제를 알림
        - channel 은 lock 획득 요청을 하는 다른 thread 에게 안내
        - pub/sub 기반의 redis lock 획득이기 때문에 Lettuce 에 비해 redis 에 부하가 덜 감
        - lock 획득 재시도를 기본으로 제공 -> 재시도가 필요하다면 실부에선 Redisson 활용 / 필요하지 않다면 Lettuce 사용하자.
        - 별도 라이브러리가 필요하다는 단점 & 라이브러리에 대한 사용법 숙지 필요
     */
    public void decrease(Long id, Long quantity) {
        RLock lock = redissonCLient.getLock(id.toString());

        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("Lock 획득 실패");
                return;
            }

            stockService.decrease(id, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
