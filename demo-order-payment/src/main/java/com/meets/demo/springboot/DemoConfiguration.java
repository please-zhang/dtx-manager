package com.meets.demo.springboot;

import com.meets.demo.common.OrderRepository;
import com.meets.demo.common.OrderService;
import com.meets.demo.common.PaymentRepository;
import com.meets.demo.common.PaymentService;
import com.meets.demo.common.StockRepository;
import com.meets.demo.common.StockService;
import com.meets.dtx.DistributedTransactionEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Bean 配置。
 */
@Configuration
public class DemoConfiguration {
    @Bean
    public OrderRepository orderRepository() {
        return new OrderRepository();
    }

    @Bean
    public PaymentRepository paymentRepository() {
        return new PaymentRepository();
    }

    @Bean
    public StockRepository stockRepository() {
        return new StockRepository();
    }

    @Bean
    public OrderService orderService(OrderRepository repository) {
        return new OrderService(repository);
    }

    @Bean
    public PaymentService paymentService(PaymentRepository repository) {
        return new PaymentService(repository);
    }

    @Bean
    public StockService stockService(StockRepository repository) {
        return new StockService(repository);
    }

    @Bean
    public DistributedTransactionEngine distributedTransactionEngine() {
        return new DistributedTransactionEngine();
    }
}
