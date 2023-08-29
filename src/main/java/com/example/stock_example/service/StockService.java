package com.example.stock_example.service;

import com.example.stock_example.entity.Stock;
import com.example.stock_example.repository.StockRepository;
import org.springframework.stereotype.Service;

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
     */

    /*
    synchronized: 하나의 thread 만 메서드에 접근할 수 있도록 메서드 선언부에 설정할 수 있음
    하지만 synchronized 는 하나의 프로세스에서만 정상 동작함을 보장함
    즉, 서버가 1대가 아니라면 재소감소가 정상적으로 이루어졌다고 보장할 수 없음, 따라서 DB 에서의 Lock 을 활용해야함
     */
    public synchronized void decrease(Long id, Long quantity) {
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값 저장
        stockRepository.saveAndFlush(stock);
    }
}
