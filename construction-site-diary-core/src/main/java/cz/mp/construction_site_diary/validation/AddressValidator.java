package cz.mp.construction_site_diary.validation;

import cz.mp.construction_site_diary.dto.AddressDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AddressValidator implements ConstraintValidator<ValidAddress, AddressDto> {

    @Override
    public boolean isValid(AddressDto address, ConstraintValidatorContext context) {
        if (address == null) {
            return true;
        }

        boolean hasStreetAddress = notBlank(address.street()) && notBlank(address.streetNumber());

        return notBlank(address.parcelNumber()) || hasStreetAddress;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}