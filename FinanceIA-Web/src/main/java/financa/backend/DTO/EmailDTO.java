package financa.backend.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailDTO {
    private int userId;
    private String emailTo;
    private String subject;
    private String text;
    private String nome;
    private String code;
    private byte[] filePDF;
    private boolean filePDFExists;
}
