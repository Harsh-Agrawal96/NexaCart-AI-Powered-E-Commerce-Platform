package com.n11bootcamp.payment_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.n11bootcamp.common.enums.PaymentProviderType;
import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.payment_service.exception.PaymentErrorCode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentProviderFactory {

    private final Map<PaymentProviderType, PaymentProvider> providers;

    public PaymentProviderFactory(List<PaymentProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(PaymentProvider::getType, Function.identity()));
        log.info("Registered payment providers: {}", providers.keySet());
    }

    public PaymentProvider getProvider(PaymentProviderType type) {
        PaymentProvider provider = providers.get(type);
        if (provider == null) {
            throw new BusinessException(PaymentErrorCode.UNKNOWN_PAYMENT_PROVIDER, type);
        }
        return provider;
    }
}
