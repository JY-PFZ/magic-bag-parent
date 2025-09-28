package nus.iss.se.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import nus.iss.se.common.annotation.RsaDecrypt;

@Getter
@Schema(description = "Login request parameters")
public class LoginReq {
    @NotBlank(message = "username is null")
    @Email(message = "Please enter the correct email")
    @Schema(description = "username", example = "123@gmail.com")
    private String username;

    @RsaDecrypt
    @NotBlank(message = "password is null")
    @Schema(description = "Password (The front end needs to be encrypted with RSA public key before transmission)", example = "AQIDBAUG...")
    private String password;
}