package com.example.stock_example.service;

import com.example.stock_example.entity.Stock;
import com.example.stock_example.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /*
     @Transactional => @Transactional 어노테이션을 덧붙이면 아래의 코드가 포함된 별도의 클래스가 생성됨
     startTransaction
     stockService.decrease
     endTransaction

     decrease 완료되었고 endTransaction 하기 전에 다른 Thread가 decrease를 호출할 수 있음
     따라서, @Transactional 어노테이션 사용할 경우 synchronized 가 예상대로 동작하지 않을 수 있다.
     */

    /*
    synchronized: 하나의 thread 만 메서드에 접근할 수 있도록 메서드 선언부에 설정할 수 있음
    하지만 synchronized 는 하나의 프로세스에서만 정상 동작함을 보장함
    즉, 서버가 1대가 아니라면 재소감소가 정상적으로 이루어졌다고 보장할 수 없음, 따라서 DB 에서의 Lock 을 활용해야함

    1. Pessimistic Lock
        - 실제로 데이터에(table or row level) lock 을 걸어서 정합성을 맞추는 방법
        - exclusive lock 을 걸게되면 다른 트랜잭션에서는 lock 이 해제되기 전까지 데이터를 가져갈 수 없게 됨 -> 성능 감소
        - 데드락 발생 가능성
    2. Optimistic Lock
        - 실제 Lock 을 이용하지 않고 버전을 이용하여 정합성을 맞춤 -> Lock 을 사용하지 않기 때문에 Pessimistic Lock 에 비해 성능이 좋음
        - 데이터를 읽은 후 update 를 수행할 때 내가 읽은 버전이 맞는지 확인하여 업데이트
            - 내가 읽은 버전과 맞지 않는다면 application 에서 다시 읽은 후에 작업 수행 -> 버전이 다르다면(실패했다면) 다시 시도하는 로직을 개발자가 직접 구현해야하는 단점이 있음
    3. Named Lock
        - metadata lock
        - lock 획득 후 해제하기 전까지 다른 세션에서는 해당 lock 을 획득할 수 없음
        - transaction 이 종료될 때 lock 이 자동으로 해제되지 않음, 별도 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW) // for named-lock
    public synchronized void decrease(Long id, Long quantity) {
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값 저장
        stockRepository.saveAndFlush(stock);
    }
}
