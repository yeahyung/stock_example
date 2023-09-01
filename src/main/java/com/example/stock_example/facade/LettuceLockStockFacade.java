package com.example.stock_example.facade;

import com.example.stock_example.repository.RedisLockRepository;
import com.example.stock_example.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    /*
    Lettuce
        - setnx 명령어를 활용하여 분산락 구현
            - key와 value를 set 할 때, 기존의 값이 없을 때만 set 하는 명령어
        - spin lock 방식
            - lock 을 획득하려는 thread 가 lock 을 획득할 수 있는지 지속적으로 확인하며 lock 을 획득하는 방식 -> retry 로직 필요
        - 구현이 간단 / spring data redis 는 lettuce 를 기본으로 사용하기 때문에 별도 라이브러리 필요하지 않음
        - spin lock 방식이기 때문에 동시에 많은 thread 가 lock 획득 대기 상태라면 redis 에 부 하가 갈 수 있음
     */
    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(id)) {
            Thread.sleep(100); // sleep 이 짧으면 redis 에 부하가 갈 수 있음
        }

        try {
            stockService.decrease(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }
}
