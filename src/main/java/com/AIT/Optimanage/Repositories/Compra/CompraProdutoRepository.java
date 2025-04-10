package com.AIT.Optimanage.Repositories.Compra;

import com.AIT.Optimanage.Models.Compra.Compra;
import com.AIT.Optimanage.Models.Compra.CompraProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraProdutoRepository extends JpaRepository<CompraProduto, Integer> {
}
