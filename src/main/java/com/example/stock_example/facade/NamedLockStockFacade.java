package com.example.stock_example.facade;

import com.example.stock_example.repository.LockRepository;
import com.example.stock_example.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class NamedLockStockFacade {

    @Autowired
    LockRepository lockRepository;

    @Autowired
    StockService stockService;

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decrease(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
