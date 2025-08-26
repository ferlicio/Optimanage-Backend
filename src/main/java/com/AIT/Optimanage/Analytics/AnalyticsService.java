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

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VendaRepository vendaRepository;
    private final CompraRepository compraRepository;

    public ResumoDTO obterResumo(User user) {
        double totalVendas = vendaRepository.findAll().stream()
                .filter(v -> v.getOwnerUser().getId().equals(user.getId()))
                .mapToDouble(Venda::getValorFinal)
                .sum();

        double totalCompras = compraRepository.findAll().stream()
                .filter(c -> c.getOwnerUser().getId().equals(user.getId()))
                .mapToDouble(Compra::getValorFinal)
                .sum();

        return new ResumoDTO(totalVendas, totalCompras);
    }

    public PrevisaoDTO preverDemanda(User user) {
        List<Venda> vendas = vendaRepository.findAll().stream()
                .filter(v -> v.getOwnerUser().getId().equals(user.getId()))
                .sorted(Comparator.comparing(Venda::getDataEfetuacao))
                .toList();

        if (vendas.size() < 2) {
            return new PrevisaoDTO(0.0);
        }

        // Forecast using a simple linear regression from Apache Commons Math.
        // This placeholder approach can be replaced by an AI-based model in the future.
        SimpleRegression regression = new SimpleRegression();
        int i = 0;
        for (Venda venda : vendas) {
            regression.addData(i++, venda.getValorFinal());
        }

        double forecast = regression.predict(i);
        return new PrevisaoDTO(forecast);
    }
}

