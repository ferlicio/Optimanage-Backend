package com.AIT.Optimanage.Exceptions;

/**
 * Exceção disparada quando uma operação de escrita é executada
 * enquanto a organização está no plano de somente visualização.
 */
public class PlanoSomenteVisualizacaoException extends CustomRuntimeException {

    private static final String DEFAULT_MESSAGE = "Operação não permitida: sua assinatura está no plano de somente visualização. Atualize o plano para voltar a editar seus dados.";

    public PlanoSomenteVisualizacaoException() {
        super(DEFAULT_MESSAGE);
    }
}
