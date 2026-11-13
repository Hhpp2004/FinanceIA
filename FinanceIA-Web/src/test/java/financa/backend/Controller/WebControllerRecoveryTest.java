package financa.backend.Controller;

import financa.backend.Repository.ExtratoRepository;
import financa.backend.Repository.OperacaoRepository;
import financa.backend.Repository.UserRepository;
import financa.backend.Service.AcoesService;
import financa.backend.Service.GenerateFilePDF;
import financa.backend.Service.OperacaoService;
import financa.backend.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WebControllerRecoveryTest {
    private final UserService userService = mock(UserService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new WebController(
                userService,
                mock(PasswordEncoder.class),
                mock(UserRepository.class),
                mock(ExtratoRepository.class),
                mock(OperacaoRepository.class),
                mock(OperacaoService.class),
                mock(GenerateFilePDF.class),
                mock(AcoesService.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers((viewName, locale) -> viewName.startsWith("redirect:")
                        ? new RedirectView(viewName.substring("redirect:".length()))
                        : mock(View.class))
                .build();
    }

    @Test
    void recoveryPostRedirectsToCodePageAndThatRouteRenders() throws Exception {
        String email = "recovery@example.com";

        mockMvc.perform(post("/web-esqueci-senha").param("email", email))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/verificar-codigo?email=recovery%40example.com"));

        verify(userService).requestPasswordReset(email);

        mockMvc.perform(get("/verificar-codigo").param("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name("verificar-codigo"))
                .andExpect(model().attribute("email", email));
    }

    @Test
    void recoveryPostSendsHxRedirectForBoostedForms() throws Exception {
        mockMvc.perform(post("/web-esqueci-senha")
                        .param("email", "recovery@example.com")
                        .header("HX-Request", "true"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("HX-Redirect",
                        "/verificar-codigo?email=recovery%40example.com"));
    }
}
