package com.AIT.Optimanage.Analytics;

import com.AIT.Optimanage.Analytics.DTOs.PrevisaoDTO;
import com.AIT.Optimanage.Analytics.DTOs.ResumoDTO;
import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Venda;
import com.AIT.Optimanage.Repositories.Compra.CompraRepository;
import com.AIT.Optimanage.Repositories.Venda.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final CompraRepository compraRepository;

    public ResumoDTO obterResumo(User user) {
        BigDecimal totalVendas = vendaRepository.findAll().stream()
                .filter(v -> v.getOwnerUser().getId().equals(user.getId()))
                .map(Venda::getValorFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCompras = compraRepository.findAll().stream()
                .filter(c -> c.getOwnerUser().getId().equals(user.getId()))
                .map(Compra::getValorFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lucro = totalVendas.subtract(totalCompras);
        return new ResumoDTO(totalVendas, totalCompras, lucro);
    }

    public PrevisaoDTO preverDemanda(User user) {
        List<Venda> vendas = vendaRepository.findAll().stream()
                .filter(v -> v.getOwnerUser().getId().equals(user.getId()))
                .sorted(Comparator.comparing(Venda::getDataEfetuacao))
                .toList();

        if (vendas.size() < 2) {
            return new PrevisaoDTO(BigDecimal.ZERO);
        }

        // Forecast using a simple linear regression from Apache Commons Math.
        // This placeholder approach can be replaced by an AI-based model in the future.
        SimpleRegression regression = new SimpleRegression();
        int i = 0;
        for (Venda venda : vendas) {
            regression.addData(i++, venda.getValorFinal().doubleValue());
        }

        double forecast = regression.predict(i);
        return new PrevisaoDTO(BigDecimal.valueOf(forecast));
    }
}

