package financa.backend.Controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.crypto.password.PasswordEncoder;

import financa.backend.Model.Saldo;
import financa.backend.Model.Categoria;
import financa.backend.Model.Tipo;
import financa.backend.Model.Users;
import financa.backend.DTO.OperacaoDTO;
import financa.backend.Exception.TimeData;
import financa.backend.Repository.ExtratoRepository;
import financa.backend.Repository.OperacaoRepository;
import financa.backend.Repository.UserRepository;
import financa.backend.Service.OperacaoService;
import financa.backend.Service.AcoesService;
import financa.backend.Service.GenerateFilePDF;
import financa.backend.Service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {
    private static final String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ExtratoRepository extratoRepository;
    private final OperacaoRepository operacaoRepository;
    private final OperacaoService operacaoService;
    private final GenerateFilePDF generateFilePDF;
    private final AcoesService acoesService;

    public WebController(UserService userService, PasswordEncoder passwordEncoder,
            UserRepository userRepository, ExtratoRepository extratoRepository,
            OperacaoRepository operacaoRepository, OperacaoService operacaoService,
            GenerateFilePDF generateFilePDF, AcoesService acoesService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.extratoRepository = extratoRepository;
        this.operacaoRepository = operacaoRepository;
        this.operacaoService = operacaoService;
        this.generateFilePDF = generateFilePDF;
        this.acoesService = acoesService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/erro/429")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public String muitasSolicitacoes(Model model) {
        model.addAttribute("status", HttpStatus.TOO_MANY_REQUESTS.value());
        return "error";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @GetMapping("/esqueci-senha")
    public String esqueciSenha() {
        return "esqueci-senha";
    }

    @PostMapping("/web-esqueci-senha")
    public String solicitarRecuperacao(@RequestParam String email,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        userService.requestPasswordReset(email);
        redirectAttributes.addAttribute("email", email);
        return "redirect:/verificar-codigo";
    }

    @GetMapping("/verificar-codigo")
    public String verificarCodigo(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verificar-codigo";
    }

    @PostMapping("/web-verificar-codigo")
    public String validarCodigoRecuperacao(@RequestParam String email,
            @RequestParam String codigo,
            Model model,
            HttpSession session) {
        model.addAttribute("email", email);
        if (!userService.isValidPasswordResetCode(email, codigo)) {
            model.addAttribute("error", "Código inválido ou expirado. Solicite um novo código.");
            return "verificar-codigo";
        }
        session.setAttribute("passwordResetEmail", email);
        session.setAttribute("passwordResetCode", codigo);
        return "redirect:/redefinir-senha";
    }

    @GetMapping("/redefinir-senha")
    public String redefinirSenha(HttpSession session) {
        if (session.getAttribute("passwordResetEmail") == null
                || session.getAttribute("passwordResetCode") == null) {
            return "redirect:/esqueci-senha";
        }
        return "redefinir-senha";
    }

    @PostMapping("/web-redefinir-senha")
    public String salvarNovaSenha(@RequestParam String senha,
            @RequestParam String confirmarSenha,
            HttpSession session,
            Model model) {
        String email = (String) session.getAttribute("passwordResetEmail");
        String code = (String) session.getAttribute("passwordResetCode");
        if (email == null || code == null) {
            return "redirect:/esqueci-senha";
        }
        if (!senha.matches(STRONG_PASSWORD)) {
            model.addAttribute("error", "A senha não atende a todos os requisitos de segurança.");
            return "redefinir-senha";
        }
        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("error", "As senhas não coincidem.");
            return "redefinir-senha";
        }
        if (!userService.resetPassword(email, code, senha)) {
            session.removeAttribute("passwordResetEmail");
            session.removeAttribute("passwordResetCode");
            return "redirect:/esqueci-senha?expired";
        }
        session.invalidate();
        return "redirect:/login?passwordReset";
    }

    @PostMapping("/web-cadastro")
    public String cadastrar(@RequestParam String nome,
            @RequestParam String email,
            @RequestParam String telefone,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (!senha.matches(STRONG_PASSWORD)) {
            model.addAttribute("error", "A senha não atende a todos os requisitos de segurança.");
            return "cadastro";
        }

        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("error", "As senhas não coincidem.");
            return "cadastro";
        }

        try {
            Users user = new Users(nome, telefone, passwordEncoder.encode(senha), email,
                    new Saldo(), false, null);
            userService.createUser(user);
            redirectAttributes.addAttribute("email", email);
            return "redirect:/confirmar-email";
        } catch (TimeData exception) {
            model.addAttribute("error", "A data da operação não pode estar no futuro.");
            return "nova-operacao";
        } catch (RuntimeException exception) {
            model.addAttribute("error", "Não foi possível criar a conta. Verifique os dados e tente novamente.");
            return "cadastro";
        }
    }

    @GetMapping("/confirmar-email")
    public String confirmarEmail(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "confirmar-email";
    }

    @PostMapping("/web-confirmar-email")
    public String validarConfirmacaoEmail(@RequestParam String email,
            @RequestParam String codigo,
            Model model) {
        if (!userService.validarCodigo(email, codigo)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Código inválido ou expirado.");
            return "confirmar-email";
        }
        return "redirect:/login?confirmed";
    }

    @GetMapping("/app")
    public String app(Principal principal, Model model) {
        Users user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        Saldo saldoUsuario = user.getSaldo() == null ? new Saldo() : user.getSaldo();
        Map<String, Double> gastosPorCategoria = new LinkedHashMap<>();
        for (Categoria categoria : Categoria.values()) {
            gastosPorCategoria.put(categoria.name(), 0.0);
        }

        LocalDate hoje = LocalDate.now();
        YearMonth periodoAtual = YearMonth.from(hoje);
        var operacoesDoMes = operacaoRepository.findByExtratoUserAndDataBetween(
                user, periodoAtual.atDay(1), periodoAtual.atEndOfMonth());

        double entradasDoMes = operacoesDoMes.stream()
                .filter(operacao -> operacao.getTipo() == Tipo.ENTRADA)
                .mapToDouble(operacao -> Math.abs(operacao.getValor()))
                .sum();
        double saidasDoMes = operacoesDoMes.stream()
                .filter(operacao -> operacao.getTipo() == Tipo.SAIDA)
                .mapToDouble(operacao -> Math.abs(operacao.getValor()))
                .sum();

        operacoesDoMes.stream()
                .filter(operacao -> operacao.getTipo() == Tipo.SAIDA && operacao.getCategoria() != null)
                .forEach(operacao -> gastosPorCategoria.merge(
                        operacao.getCategoria().name(), Math.abs(operacao.getValor()), Double::sum));

        // O saldo atual é acumulado no registro do usuário; apenas os totais
        // de entradas e saídas abaixo são limitados ao mês vigente.
        Saldo saldo = new Saldo(saldoUsuario.getSaldoAtual(), entradasDoMes, saidasDoMes);
        String mesAtual = operacaoService.formatter(hoje);

        model.addAttribute("email", user.getEmail());
        model.addAttribute("nome", user.getNome());
        model.addAttribute("saldo", saldo);
        model.addAttribute("mesAtual", mesAtual.replace(" / ", " de "));
        model.addAttribute("extratos", extratoRepository.findByUserOrderByDateDesc(user));
        model.addAttribute("gastosPorCategoria", gastosPorCategoria);
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("semGastos", gastosPorCategoria.values().stream().allMatch(valor -> valor == 0));
        try {
            model.addAttribute("maioresAltas", acoesService.getMaioresAltas());
            model.addAttribute("maioresBaixas", acoesService.getMaioresBaixas());
            model.addAttribute("mercadoDisponivel", true);
        } catch (Exception exception) {
            model.addAttribute("maioresAltas", List.of());
            model.addAttribute("maioresBaixas", List.of());
            model.addAttribute("mercadoDisponivel", false);
        }
        return "app";
    }

    @GetMapping("/acoes/{codigo}")
    public String visualizarAcao(@PathVariable String codigo, Model model) {
        try {
            model.addAttribute("acao", acoesService.getAcaoPorCodigo(codigo));
            return "acao-detalhe";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("codigo", codigo);
            model.addAttribute("erro", "Ação não encontrada. Confira o código e tente novamente.");
            return "acao-detalhe";
        } catch (IllegalStateException exception) {
            model.addAttribute("codigo", codigo);
            String mensagem = switch (exception.getMessage()) {
                case "BRAPI_AUTH" -> "A BRAPI recusou a chave. Confira o token e reinicie a aplicação.";
                case "BRAPI_FORBIDDEN" -> "Seu plano da BRAPI não permite consultar os detalhes deste ativo.";
                case "BRAPI_LIMIT" -> "O limite de consultas da BRAPI foi atingido. Tente novamente mais tarde.";
                default -> "O serviço de cotações não conseguiu consultar esta ação agora.";
            };
            model.addAttribute("erro", mensagem);
            return "acao-detalhe";
        } catch (Exception exception) {
            model.addAttribute("codigo", codigo);
            model.addAttribute("erro", "Não foi possível consultar esta ação agora.");
            return "acao-detalhe";
        }
    }

    @GetMapping("/api/acoes/pesquisa")
    @ResponseBody
    public List<AcaoPesquisaView> pesquisarAcoes(@RequestParam String termo) throws Exception {
        return acoesService.pesquisarAcoesEFundos(termo).stream()
                .map(acao -> new AcaoPesquisaView(
                        acao.getStock(), acao.getName(), acao.getClose(),
                        acao.getChange(), acao.getLogo(), acao.getType()))
                .toList();
    }

    public record AcaoPesquisaView(
            String stock, String name, Double close, Double change, String logo, String type) {}

    @GetMapping("/nova-operacao")
    public String novaOperacao() {
        return "nova-operacao";
    }

    @PostMapping("/web-operacao")
    public String cadastrarOperacao(@RequestParam Double valor,
            @RequestParam String nome,
            @RequestParam String descricao,
            @RequestParam String data,
            @RequestParam Tipo tipo,
            @RequestParam(required = false) Categoria categoria,
            Principal principal,
            Model model) {
        Users user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        try {
            OperacaoDTO operacao = new OperacaoDTO(nome, descricao, valor, data,
                    tipo == Tipo.ENTRADA ? null : categoria, tipo);
            operacaoService.createOperacao(user, operacao);
            return "redirect:/app?operacaoCadastrada";
        } catch (RuntimeException exception) {
            model.addAttribute("error", "Não foi possível cadastrar a operação. Verifique os dados.");
            return "nova-operacao";
        }
    }

    @GetMapping("/extratos/{id}")
    public String visualizarExtrato(@org.springframework.web.bind.annotation.PathVariable UUID id,
            Principal principal, Model model) {
        Users user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        var extrato = extratoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Extrato não encontrado"));
        DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<OperacaoView> operacoes = operacaoRepository.findByExtrato(extrato).stream()
                .sorted(Comparator.comparing(financa.backend.Model.Operacao::getData)
                .thenComparing(financa.backend.Model.Operacao::getTime).reversed())
                .map(operacao -> new OperacaoView(
                        operacao.getId(), operacao.getNome(),
                        operacao.getCategoria() == null ? "Entrada" : operacao.getCategoria().getNome(),
                        operacao.getTipo(), operacao.getValor(), operacao.getData().format(dataFormat)))
                .toList();
        model.addAttribute("extrato", extrato);
        model.addAttribute("operacoes", operacoes);
        model.addAttribute("tituloExtrato", extrato.getNome().replace(" / ", " de "));
        return "extrato-detalhe";
    }

    @PostMapping("/extratos/{id}/relatorio-email")
    public String enviarRelatorioExtrato(@org.springframework.web.bind.annotation.PathVariable UUID id,
            Principal principal) {
        Users user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        var extrato = extratoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Extrato não encontrado"));
        generateFilePDF.generateAndSend(user, extrato);
        return "redirect:/extratos/" + id + "?relatorioEnviado";
    }

    @GetMapping("/extratos/{extratoId}/operacoes/{operacaoId}")
    public String visualizarOperacao(
            @org.springframework.web.bind.annotation.PathVariable UUID extratoId,
            @org.springframework.web.bind.annotation.PathVariable UUID operacaoId,
            Principal principal, Model model) {
        Users user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        var extrato = extratoRepository.findByIdAndUser(extratoId, user)
                .orElseThrow(() -> new IllegalArgumentException("Extrato não encontrado"));
        var operacao = operacaoRepository.findByIdAndExtrato(operacaoId, extrato)
                .orElseThrow(() -> new IllegalArgumentException("Operação não encontrada"));
        DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm");
        model.addAttribute("extrato", extrato);
        model.addAttribute("operacao", operacao);
        model.addAttribute("categoriaOperacao",
                operacao.getCategoria() == null ? "Entrada" : operacao.getCategoria().getNome());
        model.addAttribute("dataOperacao", operacao.getData().format(dataFormat));
        model.addAttribute("horaOperacao", operacao.getTime() == null ? "—" : operacao.getTime().format(horaFormat));
        return "operacao-detalhe";
    }

    public record OperacaoView(UUID id, String nome, String categoria, Tipo tipo, Double valor, String data) {}

    @GetMapping("/configuracoes")
    public String configuracoes(Principal principal, Model model) {
        Users user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        model.addAttribute("usuario", user);
        return "configuracoes";
    }

    @PostMapping("/web-configuracoes")
    public String salvarConfiguracoes(@RequestParam String nome,
            @RequestParam String email,
            @RequestParam String telefone,
            @RequestParam(required = false) String senhaAtual,
            @RequestParam(required = false) String novaSenha,
            @RequestParam(required = false) String confirmarNovaSenha,
            Principal principal, Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Users user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        boolean desejaTrocarSenha = novaSenha != null && !novaSenha.isBlank();

        if (nome.isBlank() || email.isBlank() || telefone.isBlank()) {
            return configuracoesComErro(model, user, "Preencha todos os dados da conta.");
        }
        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            return configuracoesComErro(model, user, "Este e-mail já está sendo utilizado.");
        }
        if (desejaTrocarSenha) {
            if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, user.getSenha())) {
                return configuracoesComErro(model, user, "A senha atual está incorreta.");
            }
            if (!novaSenha.matches(STRONG_PASSWORD)) {
                return configuracoesComErro(model, user,
                        "A nova senha deve ter 8 caracteres, incluindo maiúscula, minúscula, número e símbolo.");
            }
            if (!novaSenha.equals(confirmarNovaSenha)) {
                return configuracoesComErro(model, user, "A confirmação da nova senha não coincide.");
            }
            user.setSenha(passwordEncoder.encode(novaSenha));
        }

        user.setNome(nome.trim());
        user.setTelefone(telefone.trim());
        // O e-mail identifica a sessão; sua edição será habilitada em um fluxo com reconfirmação.
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Configurações atualizadas com sucesso.");
        return "redirect:/configuracoes";
    }

    private String configuracoesComErro(Model model, Users user, String mensagem) {
        model.addAttribute("usuario", user);
        model.addAttribute("error", mensagem);
        return "configuracoes";
    }
}
