package com.AIT.Optimanage.Support.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPFValido, String> {

    @Override
    public void initialize(CPFValido constraintAnnotation) {
        // no initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String cpf = value.replaceAll("\\D", "");
        if (cpf.length() != 11) {
            return false;
        }
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstCheck = sum % 11;
        firstCheck = firstCheck < 2 ? 0 : 11 - firstCheck;
        if ((cpf.charAt(9) - '0') != firstCheck) {
            return false;
        }
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondCheck = sum % 11;
        secondCheck = secondCheck < 2 ? 0 : 11 - secondCheck;
        return (cpf.charAt(10) - '0') == secondCheck;
    }
}
