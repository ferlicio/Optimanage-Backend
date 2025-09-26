package com.AIT.Optimanage.Controllers.Importacao;

import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.dto.ImportacaoResultado;
import com.AIT.Optimanage.Services.Importacao.ImportacaoExcelService;
import com.AIT.Optimanage.Services.Importacao.ImportacaoTipo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@RequestMapping("/importacoes")
@RequiredArgsConstructor
@Tag(name = "Importações", description = "Importação em lote por arquivos XLSX")
public class ImportacaoController extends V1BaseController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ImportacaoExcelService importacaoExcelService;

    @PostMapping(value = "/clientes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar clientes", description = "Importa clientes a partir de um arquivo XLSX")
    public ResponseEntity<ImportacaoResultado> importarClientes(@RequestParam("arquivo") MultipartFile arquivo) {
        return ok(importacaoExcelService.importarClientes(arquivo));
    }

    @PostMapping(value = "/fornecedores", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar fornecedores", description = "Importa fornecedores a partir de um arquivo XLSX")
    public ResponseEntity<ImportacaoResultado> importarFornecedores(@RequestParam("arquivo") MultipartFile arquivo) {
        return ok(importacaoExcelService.importarFornecedores(arquivo));
    }

    @PostMapping(value = "/produtos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar produtos", description = "Importa produtos a partir de um arquivo XLSX")
    public ResponseEntity<ImportacaoResultado> importarProdutos(@RequestParam("arquivo") MultipartFile arquivo) {
        return ok(importacaoExcelService.importarProdutos(arquivo));
    }

    @PostMapping(value = "/servicos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar serviços", description = "Importa serviços a partir de um arquivo XLSX")
    public ResponseEntity<ImportacaoResultado> importarServicos(@RequestParam("arquivo") MultipartFile arquivo) {
        return ok(importacaoExcelService.importarServicos(arquivo));
    }

    @PostMapping(value = "/vendas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar vendas", description = "Importa vendas e seus itens a partir de um arquivo XLSX")
    public ResponseEntity<ImportacaoResultado> importarVendas(@RequestParam("arquivo") MultipartFile arquivo) {
        return ok(importacaoExcelService.importarVendas(arquivo));
    }

    @PostMapping(value = "/compras", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar compras", description = "Importa compras e seus itens a partir de um arquivo XLSX")
    public ResponseEntity<ImportacaoResultado> importarCompras(@RequestParam("arquivo") MultipartFile arquivo) {
        return ok(importacaoExcelService.importarCompras(arquivo));
    }

    @GetMapping("/modelo/{tipo}")
    @Operation(summary = "Baixar modelo", description = "Retorna um arquivo XLSX modelo para o tipo de importação informado")
    public ResponseEntity<ByteArrayResource> obterModelo(@PathVariable("tipo") String tipo) {
        ImportacaoTipo tipoImportacao = Arrays.stream(ImportacaoTipo.values())
                .filter(valor -> valor.name().equalsIgnoreCase(tipo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de modelo inválido: " + tipo));
        byte[] conteudo = importacaoExcelService.gerarModelo(tipoImportacao);
        String nomeArquivo = "modelo-" + tipoImportacao.name().toLowerCase(Locale.ROOT) + ".xlsx";
        ByteArrayResource recurso = new ByteArrayResource(conteudo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodeFilename(nomeArquivo))
                .contentType(XLSX_MEDIA_TYPE)
                .contentLength(conteudo.length)
                .body(recurso);
    }

    private String encodeFilename(String filename) {
        return "\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8) + "\"";
    }
}
