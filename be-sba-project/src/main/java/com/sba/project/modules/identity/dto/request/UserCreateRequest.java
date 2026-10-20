package com.sba.project.modules.identity.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequest {
    @Email
    @Size(max = 320)
    private String email;

    @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$")
    private String phone;

    @NotBlank
    @Size(max = 255)
    private String fullName;

    private LocalDate dateOfBirth;

    @Size(max = 30)
    private String gender;

    @AssertTrue(message = "INVALID_REQUEST")
    public boolean isContactProvided() {
        return (email != null && !email.isBlank()) || (phone != null && !phone.isBlank());
    }
}
