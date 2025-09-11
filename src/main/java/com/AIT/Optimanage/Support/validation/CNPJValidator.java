package com.AIT.Optimanage.Support.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CNPJValidator implements ConstraintValidator<CNPJValido, String> {

    @Override
    public void initialize(CNPJValido constraintAnnotation) {
        // no initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String cnpj = value.replaceAll("\\D", "");
        if (cnpj.length() != 14) {
            return false;
        }
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }
        int[] weights1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int[] weights2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * weights1[i];
        }
        int firstCheck = sum % 11;
        firstCheck = firstCheck < 2 ? 0 : 11 - firstCheck;
        if ((cnpj.charAt(12) - '0') != firstCheck) {
            return false;
        }
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * weights2[i];
        }
        int secondCheck = sum % 11;
        secondCheck = secondCheck < 2 ? 0 : 11 - secondCheck;
        return (cnpj.charAt(13) - '0') == secondCheck;
    }
}
