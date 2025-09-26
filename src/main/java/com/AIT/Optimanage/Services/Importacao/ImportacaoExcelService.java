package com.AIT.Optimanage.Services.Importacao;

import com.AIT.Optimanage.Controllers.dto.ClienteRequest;
import com.AIT.Optimanage.Controllers.dto.FornecedorRequest;
import com.AIT.Optimanage.Controllers.dto.ImportacaoResultado;
import com.AIT.Optimanage.Controllers.dto.ProdutoRequest;
import com.AIT.Optimanage.Controllers.dto.ServicoRequest;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraProdutoDTO;
import com.AIT.Optimanage.Models.Compra.DTOs.CompraServicoDTO;
import com.AIT.Optimanage.Models.Compra.Related.StatusCompra;
import com.AIT.Optimanage.Models.Enums.TipoPessoa;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaProdutoDTO;
import com.AIT.Optimanage.Models.Venda.DTOs.VendaServicoDTO;
import com.AIT.Optimanage.Models.Venda.Related.StatusVenda;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.Cliente.ClienteService;
import com.AIT.Optimanage.Services.Compra.CompraService;
import com.AIT.Optimanage.Services.Fornecedor.FornecedorService;
import com.AIT.Optimanage.Services.ProdutoService;
import com.AIT.Optimanage.Services.ServicoService;
import com.AIT.Optimanage.Services.Venda.VendaService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportacaoExcelService {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.getDefault());
    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy")
    };
    private static final DateTimeFormatter[] TIME_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_TIME,
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm:ss")
    };

    private final ClienteService clienteService;
    private final FornecedorService fornecedorService;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;
    private final VendaService vendaService;
    private final CompraService compraService;

    public ImportacaoResultado importarClientes(MultipartFile arquivo) {
        return processaPlanilhaSimples(arquivo, "clientes", (row, reader, linha) -> {
            ClienteRequest request = ClienteRequest.builder()
                    .atividadeId(reader.getRequiredInteger(row, "atividadeId"))
                    .tipoPessoa(reader.getRequiredEnum(row, "tipoPessoa", TipoPessoa.class))
                    .origem(reader.getRequiredString(row, "origem"))
                    .ativo(reader.getBoolean(row, "ativo"))
                    .nome(reader.getString(row, "nome"))
                    .nomeFantasia(reader.getString(row, "nomeFantasia"))
                    .razaoSocial(reader.getString(row, "razaoSocial"))
                    .cpf(reader.getString(row, "cpf"))
                    .cnpj(reader.getString(row, "cnpj"))
                    .inscricaoEstadual(reader.getString(row, "inscricaoEstadual"))
                    .inscricaoMunicipal(reader.getString(row, "inscricaoMunicipal"))
                    .site(reader.getString(row, "site"))
                    .informacoesAdicionais(reader.getString(row, "informacoesAdicionais"))
                    .build();
            clienteService.criarCliente(request);
        });
    }

    public ImportacaoResultado importarFornecedores(MultipartFile arquivo) {
        return processaPlanilhaSimples(arquivo, "fornecedores", (row, reader, linha) -> {
            FornecedorRequest request = FornecedorRequest.builder()
                    .atividadeId(reader.getRequiredInteger(row, "atividadeId"))
                    .tipoPessoa(reader.getRequiredEnum(row, "tipoPessoa", TipoPessoa.class))
                    .origem(reader.getRequiredString(row, "origem"))
                    .ativo(reader.getBoolean(row, "ativo"))
                    .nome(reader.getString(row, "nome"))
                    .nomeFantasia(reader.getString(row, "nomeFantasia"))
                    .razaoSocial(reader.getString(row, "razaoSocial"))
                    .cpf(reader.getString(row, "cpf"))
                    .cnpj(reader.getString(row, "cnpj"))
                    .inscricaoEstadual(reader.getString(row, "inscricaoEstadual"))
                    .inscricaoMunicipal(reader.getString(row, "inscricaoMunicipal"))
                    .site(reader.getString(row, "site"))
                    .informacoesAdicionais(reader.getString(row, "informacoesAdicionais"))
                    .build();
            fornecedorService.criarFornecedor(request);
        });
    }

    public ImportacaoResultado importarProdutos(MultipartFile arquivo) {
        return processaPlanilhaSimples(arquivo, "produtos", (row, reader, linha) -> {
            ProdutoRequest request = ProdutoRequest.builder()
                    .fornecedorId(reader.getInteger(row, "fornecedorId"))
                    .sequencialUsuario(reader.getRequiredInteger(row, "sequencialUsuario"))
                    .codigoReferencia(reader.getRequiredString(row, "codigoReferencia"))
                    .nome(reader.getRequiredString(row, "nome"))
                    .descricao(reader.getString(row, "descricao"))
                    .custo(reader.getRequiredBigDecimal(row, "custo"))
                    .disponivelVenda(reader.getBoolean(row, "disponivelVenda"))
                    .valorVenda(reader.getRequiredBigDecimal(row, "valorVenda"))
                    .qtdEstoque(reader.getRequiredInteger(row, "qtdEstoque"))
                    .terceirizado(reader.getBoolean(row, "terceirizado"))
                    .ativo(reader.getBoolean(row, "ativo"))
                    .estoqueMinimo(reader.getInteger(row, "estoqueMinimo"))
                    .prazoReposicaoDias(reader.getInteger(row, "prazoReposicaoDias"))
                    .build();
            produtoService.cadastrarProduto(request);
        });
    }

    public ImportacaoResultado importarServicos(MultipartFile arquivo) {
        return processaPlanilhaSimples(arquivo, "servicos", (row, reader, linha) -> {
            ServicoRequest request = ServicoRequest.builder()
                    .fornecedorId(reader.getInteger(row, "fornecedorId"))
                    .sequencialUsuario(reader.getRequiredInteger(row, "sequencialUsuario"))
                    .nome(reader.getRequiredString(row, "nome"))
                    .descricao(reader.getString(row, "descricao"))
                    .custo(reader.getRequiredBigDecimal(row, "custo"))
                    .disponivelVenda(reader.getBoolean(row, "disponivelVenda"))
                    .valorVenda(reader.getRequiredBigDecimal(row, "valorVenda"))
                    .tempoExecucao(reader.getRequiredInteger(row, "tempoExecucao"))
                    .terceirizado(reader.getBoolean(row, "terceirizado"))
                    .ativo(reader.getBoolean(row, "ativo"))
                    .build();
            servicoService.cadastrarServico(request);
        });
    }

    public ImportacaoResultado importarVendas(MultipartFile arquivo) {
        try (Workbook workbook = carregarWorkbook(arquivo)) {
            Sheet sheetVendas = obterPlanilhaObrigatoria(workbook, "vendas");
            ExcelSheetReader readerVendas = ExcelSheetReader.fromSheet(sheetVendas);
            Map<String, VendaImportacao> vendas = new LinkedHashMap<>();
            List<String> errosGerais = new ArrayList<>();
            int total = 0;

            for (int i = 1; i <= sheetVendas.getLastRowNum(); i++) {
                Row row = sheetVendas.getRow(i);
                if (isLinhaVazia(row)) {
                    continue;
                }
                total++;
                int linha = i + 1;
                try {
                    String codigo = readerVendas.getRequiredString(row, "codigo");
                    if (vendas.containsKey(codigo)) {
                        throw new IllegalArgumentException("Código de venda duplicado: " + codigo);
                    }
                    VendaDTO dto = new VendaDTO();
                    dto.setClienteId(readerVendas.getRequiredInteger(row, "clienteId"));
                    LocalDate dataEfetuacao = readerVendas.getLocalDate(row, "dataEfetuacao");
                    if (dataEfetuacao != null) {
                        dto.setDataEfetuacao(dataEfetuacao);
                    }
                    LocalDate dataAgendada = readerVendas.getLocalDate(row, "dataAgendada");
                    if (dataAgendada != null) {
                        dto.setDataAgendada(dataAgendada);
                    }
                    LocalTime horaAgendada = readerVendas.getLocalTime(row, "horaAgendada");
                    if (horaAgendada != null) {
                        dto.setHoraAgendada(horaAgendada);
                    }
                    Integer duracaoMinutos = readerVendas.getInteger(row, "duracaoEstimadaMinutos");
                    if (duracaoMinutos != null) {
                        dto.setDuracaoEstimada(Duration.ofMinutes(duracaoMinutos));
                    }
                    LocalDate dataCobranca = readerVendas.getLocalDate(row, "dataCobranca");
                    if (dataCobranca != null) {
                        dto.setDataCobranca(dataCobranca);
                    }
                    BigDecimal descontoGeral = readerVendas.getBigDecimal(row, "descontoGeral");
                    if (descontoGeral != null) {
                        dto.setDescontoGeral(descontoGeral);
                    }
                    dto.setCondicaoPagamento(readerVendas.getString(row, "condicaoPagamento"));
                    Integer alteracoes = readerVendas.getInteger(row, "alteracoesPermitidas");
                    if (alteracoes != null) {
                        dto.setAlteracoesPermitidas(alteracoes);
                    }
                    StatusVenda status = readerVendas.getEnum(row, "status", StatusVenda.class);
                    if (status != null) {
                        dto.setStatus(status);
                    }
                    dto.setObservacoes(readerVendas.getString(row, "observacoes"));
                    dto.setProdutos(new ArrayList<>());
                    dto.setServicos(new ArrayList<>());
                    vendas.put(codigo, new VendaImportacao(linha, codigo, dto));
                } catch (Exception ex) {
                    errosGerais.add("vendas linha " + linha + ": " + ex.getMessage());
                }
            }

            processarItensVenda(workbook.getSheet("venda_produtos"), vendas, true, errosGerais);
            processarItensVenda(workbook.getSheet("venda_servicos"), vendas, false, errosGerais);

            int sucesso = 0;
            for (VendaImportacao venda : vendas.values()) {
                if (!venda.erros.isEmpty()) {
                    errosGerais.addAll(venda.erros);
                    continue;
                }
                if (venda.dto.hasNoItems()) {
                    errosGerais.add("vendas linha " + venda.linha + ": venda sem produtos ou serviços associados");
                    continue;
                }
                try {
                    vendaService.registrarVenda(CurrentUser.get(), venda.dto);
                    sucesso++;
                } catch (Exception ex) {
                    errosGerais.add("vendas linha " + venda.linha + ": " + ex.getMessage());
                }
            }

            return new ImportacaoResultado(total, sucesso, errosGerais);
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo XLSX", e);
        }
    }

    public ImportacaoResultado importarCompras(MultipartFile arquivo) {
        try (Workbook workbook = carregarWorkbook(arquivo)) {
            Sheet sheetCompras = obterPlanilhaObrigatoria(workbook, "compras");
            ExcelSheetReader readerCompras = ExcelSheetReader.fromSheet(sheetCompras);
            Map<String, CompraImportacao> compras = new LinkedHashMap<>();
            List<String> errosGerais = new ArrayList<>();
            int total = 0;

            for (int i = 1; i <= sheetCompras.getLastRowNum(); i++) {
                Row row = sheetCompras.getRow(i);
                if (isLinhaVazia(row)) {
                    continue;
                }
                total++;
                int linha = i + 1;
                try {
                    String codigo = readerCompras.getRequiredString(row, "codigo");
                    if (compras.containsKey(codigo)) {
                        throw new IllegalArgumentException("Código de compra duplicado: " + codigo);
                    }
                    CompraDTO dto = new CompraDTO();
                    dto.setFornecedorId(readerCompras.getRequiredInteger(row, "fornecedorId"));
                    LocalDate dataEfetuacao = readerCompras.getLocalDate(row, "dataEfetuacao");
                    if (dataEfetuacao != null) {
                        dto.setDataEfetuacao(dataEfetuacao);
                    }
                    LocalDate dataAgendada = readerCompras.getLocalDate(row, "dataAgendada");
                    if (dataAgendada != null) {
                        dto.setDataAgendada(dataAgendada);
                    }
                    LocalTime horaAgendada = readerCompras.getLocalTime(row, "horaAgendada");
                    if (horaAgendada != null) {
                        dto.setHoraAgendada(horaAgendada);
                    }
                    Integer duracaoMinutos = readerCompras.getInteger(row, "duracaoEstimadaMinutos");
                    if (duracaoMinutos != null) {
                        dto.setDuracaoEstimada(Duration.ofMinutes(duracaoMinutos));
                    }
                    LocalDate dataCobranca = readerCompras.getLocalDate(row, "dataCobranca");
                    if (dataCobranca != null) {
                        dto.setDataCobranca(dataCobranca);
                    }
                    BigDecimal valorFinal = readerCompras.getBigDecimal(row, "valorFinal");
                    if (valorFinal != null) {
                        dto.setValorFinal(valorFinal);
                    }
                    dto.setCondicaoPagamento(readerCompras.getString(row, "condicaoPagamento"));
                    StatusCompra status = readerCompras.getEnum(row, "status", StatusCompra.class);
                    if (status != null) {
                        dto.setStatus(status);
                    }
                    dto.setObservacoes(readerCompras.getString(row, "observacoes"));
                    dto.setProdutos(new ArrayList<>());
                    dto.setServicos(new ArrayList<>());
                    compras.put(codigo, new CompraImportacao(linha, codigo, dto));
                } catch (Exception ex) {
                    errosGerais.add("compras linha " + linha + ": " + ex.getMessage());
                }
            }

            processarItensCompra(workbook.getSheet("compra_produtos"), compras, true, errosGerais);
            processarItensCompra(workbook.getSheet("compra_servicos"), compras, false, errosGerais);

            int sucesso = 0;
            for (CompraImportacao compra : compras.values()) {
                if (!compra.erros.isEmpty()) {
                    errosGerais.addAll(compra.erros);
                    continue;
                }
                if (compra.dto.hasNoItems()) {
                    errosGerais.add("compras linha " + compra.linha + ": compra sem produtos ou serviços associados");
                    continue;
                }
                try {
                    compraService.criarCompra(compra.dto);
                    sucesso++;
                } catch (Exception ex) {
                    errosGerais.add("compras linha " + compra.linha + ": " + ex.getMessage());
                }
            }

            return new ImportacaoResultado(total, sucesso, errosGerais);
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo XLSX", e);
        }
    }

    public byte[] gerarModelo(ImportacaoTipo tipo) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            switch (tipo) {
                case CLIENTES -> criarPlanilha(workbook, "clientes", List.of(
                        "atividadeId", "tipoPessoa", "origem", "ativo", "nome", "nomeFantasia",
                        "razaoSocial", "cpf", "cnpj", "inscricaoEstadual", "inscricaoMunicipal",
                        "site", "informacoesAdicionais"));
                case FORNECEDORES -> criarPlanilha(workbook, "fornecedores", List.of(
                        "atividadeId", "tipoPessoa", "origem", "ativo", "nome", "nomeFantasia",
                        "razaoSocial", "cpf", "cnpj", "inscricaoEstadual", "inscricaoMunicipal",
                        "site", "informacoesAdicionais"));
                case PRODUTOS -> criarPlanilha(workbook, "produtos", List.of(
                        "fornecedorId", "sequencialUsuario", "codigoReferencia", "nome", "descricao",
                        "custo", "disponivelVenda", "valorVenda", "qtdEstoque", "terceirizado",
                        "ativo", "estoqueMinimo", "prazoReposicaoDias"));
                case SERVICOS -> criarPlanilha(workbook, "servicos", List.of(
                        "fornecedorId", "sequencialUsuario", "nome", "descricao", "custo",
                        "disponivelVenda", "valorVenda", "tempoExecucao", "terceirizado", "ativo"));
                case VENDAS -> {
                    criarPlanilha(workbook, "vendas", List.of(
                            "codigo", "clienteId", "dataEfetuacao", "dataAgendada", "horaAgendada",
                            "duracaoEstimadaMinutos", "dataCobranca", "descontoGeral", "condicaoPagamento",
                            "alteracoesPermitidas", "status", "observacoes"));
                    criarPlanilha(workbook, "venda_produtos", List.of(
                            "codigo", "produtoId", "quantidade", "desconto"));
                    criarPlanilha(workbook, "venda_servicos", List.of(
                            "codigo", "servicoId", "quantidade", "desconto"));
                }
                case COMPRAS -> {
                    criarPlanilha(workbook, "compras", List.of(
                            "codigo", "fornecedorId", "dataEfetuacao", "dataAgendada", "horaAgendada",
                            "duracaoEstimadaMinutos", "dataCobranca", "valorFinal", "condicaoPagamento",
                            "status", "observacoes"));
                    criarPlanilha(workbook, "compra_produtos", List.of(
                            "codigo", "produtoId", "quantidade", "valorUnitario"));
                    criarPlanilha(workbook, "compra_servicos", List.of(
                            "codigo", "servicoId", "quantidade"));
                }
                default -> throw new IllegalArgumentException("Tipo de modelo não suportado: " + tipo);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gerar modelo XLSX", e);
        }
    }

    private ImportacaoResultado processaPlanilhaSimples(MultipartFile arquivo, String nomePlanilha, RowProcessor processor) {
        try (Workbook workbook = carregarWorkbook(arquivo)) {
            Sheet sheet = obterPlanilhaObrigatoria(workbook, nomePlanilha);
            ExcelSheetReader reader = ExcelSheetReader.fromSheet(sheet);
            List<String> erros = new ArrayList<>();
            int total = 0;
            int sucesso = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isLinhaVazia(row)) {
                    continue;
                }
                total++;
                int linha = i + 1;
                try {
                    processor.process(row, reader, linha);
                    sucesso++;
                } catch (Exception ex) {
                    erros.add(nomePlanilha + " linha " + linha + ": " + ex.getMessage());
                }
            }
            return new ImportacaoResultado(total, sucesso, erros);
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo XLSX", e);
        }
    }

    private Workbook carregarWorkbook(MultipartFile arquivo) throws IOException {
        try (InputStream inputStream = arquivo.getInputStream()) {
            return WorkbookFactory.create(inputStream);
        }
    }

    private Sheet obterPlanilhaObrigatoria(Workbook workbook, String nome) {
        Sheet sheet = workbook.getSheet(nome);
        if (sheet == null) {
            throw new IllegalArgumentException("Planilha '" + nome + "' não encontrada no arquivo informado");
        }
        return sheet;
    }

    private void criarPlanilha(Workbook workbook, String nome, List<String> colunas) {
        Sheet sheet = workbook.createSheet(nome);
        Row header = sheet.createRow(0);
        for (int i = 0; i < colunas.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(colunas.get(i));
        }
    }

    private void processarItensVenda(Sheet sheet, Map<String, VendaImportacao> vendas, boolean produtos, List<String> errosGerais) {
        if (sheet == null) {
            return;
        }
        ExcelSheetReader reader = ExcelSheetReader.fromSheet(sheet);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isLinhaVazia(row)) {
                continue;
            }
            int linha = i + 1;
            String codigo = null;
            try {
                codigo = reader.getRequiredString(row, "codigo");
                VendaImportacao venda = vendas.get(codigo);
                if (venda == null) {
                    throw new IllegalArgumentException("Código de venda não encontrado para associação: " + codigo);
                }
                try {
                    if (produtos) {
                        VendaProdutoDTO produto = new VendaProdutoDTO(
                                reader.getRequiredInteger(row, "produtoId"),
                                reader.getRequiredInteger(row, "quantidade"),
                                defaultZero(reader.getBigDecimal(row, "desconto"))
                        );
                        venda.dto.getProdutos().add(produto);
                    } else {
                        VendaServicoDTO servico = new VendaServicoDTO(
                                reader.getRequiredInteger(row, "servicoId"),
                                reader.getRequiredInteger(row, "quantidade"),
                                defaultZero(reader.getBigDecimal(row, "desconto"))
                        );
                        venda.dto.getServicos().add(servico);
                    }
                } catch (Exception e) {
                    venda.erros.add((produtos ? "venda_produtos" : "venda_servicos") + " linha " + linha + ": " + e.getMessage());
                }
            } catch (Exception ex) {
                String origem = produtos ? "venda_produtos" : "venda_servicos";
                String codigoInfo = codigo == null ? "" : " (código " + codigo + ")";
                errosGerais.add(origem + " linha " + linha + codigoInfo + ": " + ex.getMessage());
            }
        }
    }

    private void processarItensCompra(Sheet sheet, Map<String, CompraImportacao> compras, boolean produtos, List<String> errosGerais) {
        if (sheet == null) {
            return;
        }
        ExcelSheetReader reader = ExcelSheetReader.fromSheet(sheet);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isLinhaVazia(row)) {
                continue;
            }
            int linha = i + 1;
            String codigo = null;
            try {
                codigo = reader.getRequiredString(row, "codigo");
                CompraImportacao compra = compras.get(codigo);
                if (compra == null) {
                    throw new IllegalArgumentException("Código de compra não encontrado para associação: " + codigo);
                }
                try {
                    if (produtos) {
                        CompraProdutoDTO produto = new CompraProdutoDTO(
                                reader.getRequiredInteger(row, "produtoId"),
                                reader.getRequiredInteger(row, "quantidade"),
                                reader.getRequiredBigDecimal(row, "valorUnitario")
                        );
                        compra.dto.getProdutos().add(produto);
                    } else {
                        CompraServicoDTO servico = new CompraServicoDTO(
                                reader.getRequiredInteger(row, "servicoId"),
                                reader.getRequiredInteger(row, "quantidade"));
                        compra.dto.getServicos().add(servico);
                    }
                } catch (Exception e) {
                    compra.erros.add((produtos ? "compra_produtos" : "compra_servicos") + " linha " + linha + ": " + e.getMessage());
                }
            } catch (Exception ex) {
                String origem = produtos ? "compra_produtos" : "compra_servicos";
                String codigoInfo = codigo == null ? "" : " (código " + codigo + ")";
                errosGerais.add(origem + " linha " + linha + codigoInfo + ": " + ex.getMessage());
            }
        }
    }

    private static BigDecimal defaultZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private boolean isLinhaVazia(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !DATA_FORMATTER.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private interface RowProcessor {
        void process(Row row, ExcelSheetReader reader, int linha) throws Exception;
    }

    private static class ExcelSheetReader {
        private final Map<String, Integer> colunas;

        private ExcelSheetReader(Map<String, Integer> colunas) {
            this.colunas = colunas;
        }

        static ExcelSheetReader fromSheet(Sheet sheet) {
            Row header = sheet.getRow(0);
            if (header == null) {
                throw new IllegalArgumentException("Planilha " + sheet.getSheetName() + " está sem cabeçalho");
            }
            Map<String, Integer> colunas = new HashMap<>();
            for (int i = header.getFirstCellNum(); i < header.getLastCellNum(); i++) {
                Cell cell = header.getCell(i);
                if (cell == null) {
                    continue;
                }
                String valor = DATA_FORMATTER.formatCellValue(cell).trim();
                if (!valor.isEmpty()) {
                    colunas.put(normalizar(valor), cell.getColumnIndex());
                }
            }
            return new ExcelSheetReader(colunas);
        }

        Integer getRequiredInteger(Row row, String coluna) {
            Integer valor = getInteger(row, coluna);
            if (valor == null) {
                throw new IllegalArgumentException("Coluna '" + coluna + "' é obrigatória");
            }
            return valor;
        }

        Integer getInteger(Row row, String coluna) {
            String valor = getString(row, coluna);
            if (valor.isBlank()) {
                return null;
            }
            try {
                BigDecimal numero = new BigDecimal(valor.replace(" ", "").replace(',', '.'))
                        .setScale(0, RoundingMode.UNNECESSARY);
                return numero.intValueExact();
            } catch (ArithmeticException | NumberFormatException e) {
                throw new IllegalArgumentException("Valor inteiro inválido na coluna '" + coluna + "': " + valor);
            }
        }

        BigDecimal getRequiredBigDecimal(Row row, String coluna) {
            BigDecimal valor = getBigDecimal(row, coluna);
            if (valor == null) {
                throw new IllegalArgumentException("Coluna '" + coluna + "' é obrigatória");
            }
            return valor;
        }

        BigDecimal getBigDecimal(Row row, String coluna) {
            String valor = getString(row, coluna);
            if (valor.isBlank()) {
                return null;
            }
            try {
                return new BigDecimal(valor.replace(" ", "").replace(',', '.'));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor numérico inválido na coluna '" + coluna + "': " + valor);
            }
        }

        String getRequiredString(Row row, String coluna) {
            String valor = getString(row, coluna);
            if (valor.isBlank()) {
                throw new IllegalArgumentException("Coluna '" + coluna + "' é obrigatória");
            }
            return valor;
        }

        String getString(Row row, String coluna) {
            Integer index = colunas.get(normalizar(coluna));
            if (index == null) {
                throw new IllegalArgumentException("Coluna '" + coluna + "' não encontrada");
            }
            Cell cell = row.getCell(index);
            if (cell == null) {
                return "";
            }
            return DATA_FORMATTER.formatCellValue(cell).trim();
        }

        Boolean getBoolean(Row row, String coluna) {
            String valor = getString(row, coluna);
            if (valor.isBlank()) {
                return null;
            }
            String normalizado = valor.trim().toLowerCase(Locale.ROOT);
            return switch (normalizado) {
                case "true", "1", "sim", "s", "yes", "y" -> true;
                case "false", "0", "nao", "não", "n", "no" -> false;
                default -> throw new IllegalArgumentException("Valor booleano inválido na coluna '" + coluna + "': " + valor);
            };
        }

        LocalDate getLocalDate(Row row, String coluna) {
            String valor = getString(row, coluna);
            if (valor.isBlank()) {
                return null;
            }
            for (DateTimeFormatter formatter : DATE_FORMATS) {
                try {
                    return LocalDate.parse(valor, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
            throw new IllegalArgumentException("Data inválida na coluna '" + coluna + "': " + valor);
        }

        LocalTime getLocalTime(Row row, String coluna) {
            String valor = getString(row, coluna);
            if (valor.isBlank()) {
                return null;
            }
            for (DateTimeFormatter formatter : TIME_FORMATS) {
                try {
                    return LocalTime.parse(valor, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
            throw new IllegalArgumentException("Hora inválida na coluna '" + coluna + "': " + valor);
        }

        <E extends Enum<E>> E getRequiredEnum(Row row, String coluna, Class<E> tipoEnum) {
            E valor = getEnum(row, coluna, tipoEnum);
            if (valor == null) {
                throw new IllegalArgumentException("Coluna '" + coluna + "' é obrigatória");
            }
            return valor;
        }

        <E extends Enum<E>> E getEnum(Row row, String coluna, Class<E> tipoEnum) {
            String valor = getString(row, coluna);
            if (valor.isBlank()) {
                return null;
            }
            try {
                return Enum.valueOf(tipoEnum, valor.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Valor inválido na coluna '" + coluna + "': " + valor);
            }
        }

        private static String normalizar(String valor) {
            return valor.trim().toLowerCase(Locale.ROOT);
        }
    }

    private static class VendaImportacao {
        private final int linha;
        private final String codigo;
        private final VendaDTO dto;
        private final List<String> erros = new ArrayList<>();

        private VendaImportacao(int linha, String codigo, VendaDTO dto) {
            this.linha = linha;
            this.codigo = codigo;
            this.dto = dto;
        }
    }

    private static class CompraImportacao {
        private final int linha;
        private final String codigo;
        private final CompraDTO dto;
        private final List<String> erros = new ArrayList<>();

        private CompraImportacao(int linha, String codigo, CompraDTO dto) {
            this.linha = linha;
            this.codigo = codigo;
            this.dto = dto;
        }
    }
}
