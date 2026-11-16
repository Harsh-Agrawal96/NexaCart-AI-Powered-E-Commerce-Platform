package com.n11bootcamp.payment_service.client.provider;

import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import com.n11bootcamp.common.event.OrderItemPayload;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class IyzicoRequestBuilder {

    public CreatePaymentRequest build(PaymentRequest request) {
        CreatePaymentRequest iyzicoRequest = new CreatePaymentRequest();
        iyzicoRequest.setLocale(Locale.TR.getValue());
        iyzicoRequest.setConversationId(request.getOrderId().toString());
        iyzicoRequest.setPrice(request.getGrandTotal());
        iyzicoRequest.setPaidPrice(request.getGrandTotal());
        iyzicoRequest.setCurrency(Currency.TRY.name());
        iyzicoRequest.setInstallment(1);
        iyzicoRequest.setBasketId(request.getOrderId().toString());
        iyzicoRequest.setPaymentChannel(PaymentChannel.WEB.name());
        iyzicoRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        iyzicoRequest.setPaymentCard(buildPaymentCard(request));
        iyzicoRequest.setBuyer(buildBuyer(request));
        iyzicoRequest.setShippingAddress(buildAddress(request));
        iyzicoRequest.setBillingAddress(buildAddress(request));
        iyzicoRequest.setBasketItems(buildBasketItems(request));

        return iyzicoRequest;
    }

    private PaymentCard buildPaymentCard(PaymentRequest request) {
        PaymentCard card = new PaymentCard();
        card.setCardHolderName(request.getCardHolderName() != null 
                ? request.getCardHolderName() : "John Doe");
        card.setCardNumber(request.getCardNumber() != null 
                ? request.getCardNumber() : "5528790000000008");
        card.setExpireMonth(request.getExpireMonth() != null 
                ? request.getExpireMonth() : "12");
        card.setExpireYear(request.getExpireYear() != null 
                ? request.getExpireYear() : "2030");
        card.setCvc(request.getCvc() != null 
                ? request.getCvc() : "123");
        card.setRegisterCard(0);
        return card;
    }

    private Buyer buildBuyer(PaymentRequest request) {
        Buyer buyer = new Buyer();
        buyer.setId(request.getUserId().toString());
        buyer.setName(request.getBuyerName() != null 
                ? request.getBuyerName() : "John");
        buyer.setSurname(request.getBuyerSurname() != null 
                ? request.getBuyerSurname() : "Doe");
        buyer.setEmail(request.getBuyerEmail() != null 
                ? request.getBuyerEmail() : "john.doe@n11bootcamp.com");
        buyer.setGsmNumber(request.getBuyerPhone() != null 
                ? request.getBuyerPhone() : "+905350000000");
        buyer.setCity(request.getBuyerCity() != null 
                ? request.getBuyerCity() : "Istanbul");
        buyer.
        setRegistrationAddress(request.getBuyerAddress() != null 
                ? request.getBuyerAddress() : "Nispetiye Mah. Aytar Cad. No:18");
        buyer.setIdentityNumber("74300864791");
        buyer.setIp("85.34.78.112");
        buyer.setCountry("Turkey");
        buyer.setRegistrationAddress(request.getBuyerAddress() != null 
                ? request.getBuyerAddress() : "Nispetiye Mah. Aytar Cad. No:18");
        return buyer;
    }

    private Address buildAddress(PaymentRequest request) {
        Address address = new Address();
        address.setContactName(
            (request.getBuyerName() != null && request.getBuyerSurname() != null)
                ? request.getBuyerName() + " " + request.getBuyerSurname()
                : "John Doe");
        address.setCity(request.getBuyerCity() != null
                ? request.getBuyerCity() : "Istanbul");
        address.setCountry("Turkey");
        address.setAddress(request.getBuyerAddress() != null
                ? request.getBuyerAddress() : "Nispetiye Mah. Aytar Cad. No:18");
        return address;
    }

    private List<BasketItem> buildBasketItems(PaymentRequest request) {
        List<BasketItem> basketItems = new ArrayList<>();

        for (OrderItemPayload item : request.getItems()) {
            BasketItem basketItem = new BasketItem();
            basketItem.setId(item.getProductId().toString());
            basketItem.setName("Product-" + item.getProductId());
            basketItem.setCategory1("General");
            basketItem.setItemType(BasketItemType.PHYSICAL.name());
            basketItem.setPrice(item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getAmount())));
            basketItems.add(basketItem);
        }

        return basketItems;
    }
}