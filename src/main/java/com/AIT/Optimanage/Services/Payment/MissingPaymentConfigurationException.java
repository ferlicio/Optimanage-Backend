package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentProvider;

/**
 * Exceção lançada quando não existe configuração de pagamento cadastrada e
 * nenhuma alternativa padrão pode ser utilizada para o provedor solicitado.
 */
public class MissingPaymentConfigurationException extends RuntimeException {

    public MissingPaymentConfigurationException(PaymentProvider provider) {
        this(provider, null);
    }

    public MissingPaymentConfigurationException(PaymentProvider provider, Integer organizationId) {
        super(buildMessage(provider, organizationId));
    }

    private static String buildMessage(PaymentProvider provider, Integer organizationId) {
        String base = "Nenhuma configuração de pagamento disponível para o provedor " + provider;
        if (organizationId != null) {
            base += " na organização " + organizationId;
        }
        return base + ". Cadastre as credenciais antes de iniciar cobranças.";
    }
}
