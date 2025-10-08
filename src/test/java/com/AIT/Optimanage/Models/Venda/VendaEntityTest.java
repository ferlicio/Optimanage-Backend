package com.AIT.Optimanage.Models.Venda;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VendaEntityTest {

    @Test
    void dataCobrancaPermiteValoresNulos() throws NoSuchFieldException {
        Field campoDataCobranca = Venda.class.getDeclaredField("dataCobranca");
        Column column = campoDataCobranca.getAnnotation(Column.class);
        assertTrue(column == null || column.nullable(),
                "O campo dataCobranca deve aceitar valores nulos para permitir vendas pendentes");
    }
}
