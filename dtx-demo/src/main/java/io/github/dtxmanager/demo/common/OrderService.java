package io.github.dtxmanager.demo.common;

/**
 * 订单服务，包含 TCC / SAGA / 2PC 都会用到的业务操作。
 */
public class OrderService {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(OrderService.class);
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public OrderInfo createOrder(String orderId, String userId, String sku, int quantity) {
        OrderInfo existing = repository.find(orderId);
        if (existing != null) {
            LOGGER.info("订单已存在，跳过创建 orderId=%s", orderId);
            return existing;
        }
        OrderInfo order = new OrderInfo(orderId, userId, sku, quantity);
        repository.save(order);
        LOGGER.info("创建订单 orderId=%s userId=%s sku=%s quantity=%s", orderId, userId, sku, quantity);
        return order;
    }

    public void markTrySuccess(String orderId) {
        OrderInfo order = repository.find(orderId);
        if (order != null && order.getStatus() == OrderStatus.INIT) {
            order.setStatus(OrderStatus.TRY_SUCCESS);
            LOGGER.info("订单进入 TRY_SUCCESS orderId=%s", orderId);
        }
    }

    public void confirm(String orderId) {
        OrderInfo order = repository.find(orderId);
        if (order != null && order.getStatus() != OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.CONFIRMED);
            LOGGER.info("订单确认 orderId=%s", orderId);
        }
    }

    public void cancel(String orderId) {
        OrderInfo order = repository.find(orderId);
        if (order != null && order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            LOGGER.info("订单取消 orderId=%s", orderId);
        }
    }

    public OrderInfo find(String orderId) {
        return repository.find(orderId);
    }
}

