package com.AIT.Optimanage.Services.Payment;

import com.AIT.Optimanage.Models.Payment.PaymentProvider;

/**
 * Exceção lançada quando não existe configuração de pagamento cadastrada e
 * nenhuma alternativa padrão pode ser utilizada para o provedor solicitado.
 */
public class MissingPaymentConfigurationException extends RuntimeException {

    public MissingPaymentConfigurationException(PaymentProvider provider) {
        super("Nenhuma configuração de pagamento disponível para o provedor " + provider
                + ". Cadastre as credenciais antes de iniciar cobranças.");
    }
}
