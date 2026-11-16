package com.n11bootcamp.common.enums;

import com.n11bootcamp.common.constant.SagaConstants;


public enum UserBehaviorType {
    PRODUCT_VIEWED,
    ADD_TO_CART,
    REMOVE_FROM_CART,
    CHECKOUT_STARTED;

    public String routingKey() {
        return switch (this) {
            case PRODUCT_VIEWED -> SagaConstants.RK_USER_PRODUCT_VIEWED;
            case ADD_TO_CART -> SagaConstants.RK_USER_ADD_TO_CART;
            case REMOVE_FROM_CART -> SagaConstants.RK_USER_REMOVE_FROM_CART;
            case CHECKOUT_STARTED -> SagaConstants.RK_USER_CHECKOUT_STARTED;
        };
    }
}
