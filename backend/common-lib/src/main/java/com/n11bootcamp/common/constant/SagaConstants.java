package com.n11bootcamp.common.constant;

public class SagaConstants {
    public static final String SAGA_EXCHANGE = "saga.events.exchange";
    public static final String SAGA_DLX      = "saga.events.dlx";

    public static final String Q_INVENTORY_RESERVE = "q.inventory.reserve";
    public static final String Q_INVENTORY_RELEASE = "q.inventory.release";
    public static final String Q_INVENTORY_CONFIRM = "q.inventory.confirm";
    public static final String Q_CART_CLEAR = "q.cart.clear";
    public static final String Q_ORDER_CANCEL   = "q.order.cancel";
    public static final String Q_ORDER_COMPLETE = "q.order.complete";
    public static final String Q_ORDER_AWAIT_PAYMENT = "q.order.await.payment";
    public static final String Q_ORDER_CANCEL_INVENTORY = "q.order.cancel.inventory";
    public static final String Q_ORDER_CANCEL_PAYMENT   = "q.order.cancel.payment";


    public static final String RK_ORDER_CREATED     = "order.created";
    public static final String RK_PAYMENT_FAILED    = "payment.failed";
    public static final String RK_PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String RK_INVENTORY_RESERVED = "inventory.reserved";
    public static final String RK_INVENTORY_FAILED   = "inventory.failed";

    public static final String RK_USER_PRODUCT_VIEWED = "user.product.viewed";
    public static final String RK_USER_ADD_TO_CART = "user.cart.add";
    public static final String RK_USER_REMOVE_FROM_CART = "user.cart.remove";
    public static final String RK_USER_CHECKOUT_STARTED = "user.checkout.started";

    public static final String RK_USER_BEHAVIOR_PATTERN = "user.#";
}
