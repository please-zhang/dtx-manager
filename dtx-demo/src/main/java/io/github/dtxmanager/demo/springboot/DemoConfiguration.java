package io.github.dtxmanager.demo.springboot;

import io.github.dtxmanager.demo.common.OrderRepository;
import io.github.dtxmanager.demo.common.OrderService;
import io.github.dtxmanager.demo.common.PaymentRepository;
import io.github.dtxmanager.demo.common.PaymentService;
import io.github.dtxmanager.demo.common.StockRepository;
import io.github.dtxmanager.demo.common.StockService;
import io.github.dtxmanager.DistributedTransactionEngine;
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

