package financa.backend.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import financa.backend.Model.Acoes;

@Service
public class AcoesService {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Gson gson = new Gson();

    @Value("${url.acao}")
    private String url;
    @Value("${token}")
    private String token;

    public List<Acoes> getMaioresAltas() throws Exception {
        return getAcoesPorVariacao("desc");
    }

    public List<Acoes> getMaioresBaixas() throws Exception {
        return getAcoesPorVariacao("asc");
    }

    public List<Acoes> pesquisarAcoesEFundos(String termo) throws Exception {
        String busca = termo == null ? "" : termo.trim();
        if (busca.length() < 2 || busca.length() > 50) {
            return List.of();
        }

        Map<String, Acoes> resultados = new LinkedHashMap<>();
        for (String tipo : List.of("stock", "fund")) {
            URI endpoint = URI.create(url + "/list?search="
                    + URLEncoder.encode(busca, StandardCharsets.UTF_8)
                    + "&type=" + tipo + "&limit=6");
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(endpoint)
                    .timeout(Duration.ofSeconds(15))
                    .GET();

            String bearerToken = bearerToken();
            if (!bearerToken.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + bearerToken);
            }

            HttpResponse<String> response = client.send(requestBuilder.build(), BodyHandlers.ofString());
            validarResposta(response);
            ListaAcoesResponse payload = gson.fromJson(response.body(), ListaAcoesResponse.class);
            if (payload != null && payload.stocks() != null) {
                payload.stocks().forEach(acao -> {
                    if (acao.getStock() != null) {
                        resultados.putIfAbsent(acao.getStock(), acao);
                    }
                });
            }
        }
        return resultados.values().stream().limit(10).toList();
    }

    public Acoes getAcaoPorCodigo(String codigo) throws Exception {
        String codigoNormalizado = codigo == null ? "" : codigo.trim().toUpperCase();

        if (!codigoNormalizado.matches("[A-Z0-9]{4,12}")) {
            throw new IllegalArgumentException("Código de ação inválido");
        }

        URI endpoint = URI.create(url + "/" + URLEncoder.encode(codigoNormalizado, StandardCharsets.UTF_8));
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(15)).GET();

        String bearerToken = bearerToken();
        if (!bearerToken.isBlank()) {
            req.header("Authorization", "Bearer " + bearerToken);
        }
        HttpResponse<String> res = client.send(req.build(), BodyHandlers.ofString());
        validarResposta(res);
        CotacaoResponse payload = gson.fromJson(res.body(), CotacaoResponse.class);
        if (payload == null || payload.results() == null || payload.results().isEmpty()) {
            throw new IllegalArgumentException("Ação não encontrada");
        }
        return payload.results().get(0);
    }

    private List<Acoes> getAcoesPorVariacao(String ordem) throws Exception {
        URI endpoint = URI.create(url + "/list?type=stock&sortBy=change&sortOrder=" + ordem + "&limit=5");

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(15))
                .GET();

        String bearerToken = bearerToken();
        if (!bearerToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + bearerToken);
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Erro da API de ações: HTTP " + response.statusCode());
        }

        ListaAcoesResponse payload = gson.fromJson(response.body(), ListaAcoesResponse.class);
        return payload == null || payload.stocks() == null ? List.of() : payload.stocks();
    }

    private String bearerToken() {
        if (token == null) {
            return "";
        }
        return token.trim().replaceFirst("(?i)^Bearer\\s+", "").trim();
    }

    private void validarResposta(HttpResponse<String> response) {
        switch (response.statusCode()) {
            case 401 -> throw new IllegalStateException("BRAPI_AUTH");
            case 403 -> throw new IllegalStateException("BRAPI_FORBIDDEN");
            case 404 -> {
                throw new IllegalArgumentException("Ação não encontrada");
            }
            case 429 -> throw new IllegalStateException("BRAPI_LIMIT");
            default -> {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IllegalStateException("BRAPI_HTTP_" + response.statusCode());
                }
            }
        }
    }

    private record ListaAcoesResponse(List<Acoes> stocks) {}
    private record CotacaoResponse(List<Acoes> results) {}
}
