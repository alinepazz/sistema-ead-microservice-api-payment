package com.ead.payment.services.impl;

import com.ead.payment.models.CreditCardModel;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.services.PaymentStripeService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {

    private static final Logger logger = LogManager.getLogger(PaymentServiceImpl.class);

    @Value("${ead.stripe.secretKey}")
    private String secretKeyStripe;
    @Override
    public PaymentModel processStripePayment(PaymentModel paymentModel, CreditCardModel creditCardModel) {
        logger.info("Realizando conexão com o Stripe");
        Stripe.apiKey = secretKeyStripe;
        String paymentIntentId = null;

        try {
            logger.info("Create a PaymentIntent");
            List<Object> paymentMethodTypes = new ArrayList<>();
            paymentMethodTypes.add("card");
            Map<String, Object> paramsPaymentIntent = new HashMap<>();
            paramsPaymentIntent.put("amount", paymentModel.getValuePaid().multiply(new BigDecimal("100")).longValue());
            paramsPaymentIntent.put("currency", "brl");
            paramsPaymentIntent.put("payment_method_types", paymentMethodTypes);
            PaymentIntent paymentIntent = PaymentIntent.create(paramsPaymentIntent);
            paymentIntentId = paymentIntent.getId();


            logger.info("Create a PaymentMethod");
            Map<String, Object> card = new HashMap<>();
            card.put("number", creditCardModel.getCreditCardNumber().replaceAll(" ", ""));
            card.put("exp_month", creditCardModel.getExpirationDate().split("/")[0]);
            card.put("exp_year", creditCardModel.getExpirationDate().split("/")[1]);
            card.put("cvc", creditCardModel.getCvvCode());
            Map<String, Object> paramsPaymentMethod = new HashMap<>();
            paramsPaymentMethod.put("type", "card");
            paramsPaymentMethod.put("card", card);
            PaymentMethod paymentMethod = PaymentMethod.create(paramsPaymentMethod);


            logger.info("Confirm a PaymentConfirm");
            Map<String, Object> paramsPaymentConfirm = new HashMap<>();
            paramsPaymentConfirm.put("payment_method", paymentMethod.getId());
            PaymentIntent confirmPaymentIntent = paymentIntent.confirm(paramsPaymentConfirm);

        }catch (Exception exception){

        }
        return null;
    }
}
