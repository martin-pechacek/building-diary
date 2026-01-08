package cz.mp.building_diary.validation;

import cz.mp.building_diary.dto.AddressDto;
import cz.mp.building_diary.enums.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressValidatorTest {

    private AddressValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AddressValidator();
    }

    @Nested
    class ValidAddress {

        @Test
        void shouldReturnTrueWhenAddressIsNull() {
            assertThat(validator.isValid(null, null)).isTrue();
        }

        @Test
        void shouldReturnTrueWhenHasParcelNumber() {
            AddressDto address = new AddressDto(
                    "1234/5",
                    null,
                    null,
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isTrue();
        }

        @Test
        void shouldReturnTrueWhenHasStreetAndStreetNumber() {
            AddressDto address = new AddressDto(
                    null,
                    "Hlavní",
                    "123",
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isTrue();
        }

        @Test
        void shouldReturnTrueWhenHasBothParcelAndStreet() {
            AddressDto address = new AddressDto(
                    "1234/5",
                    "Hlavní",
                    "123",
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isTrue();
        }
    }

    @Nested
    class InvalidAddress {

        @Test
        void shouldReturnFalseWhenNoParcelAndNoStreet() {
            AddressDto address = new AddressDto(
                    null,
                    null,
                    null,
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenHasStreetButNoStreetNumber() {
            AddressDto address = new AddressDto(
                    null,
                    "Hlavní",
                    null,
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenHasStreetNumberButNoStreet() {
            AddressDto address = new AddressDto(
                    null,
                    null,
                    "123",
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenParcelNumberIsBlank() {
            AddressDto address = new AddressDto(
                    "   ",
                    null,
                    null,
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenStreetIsBlank() {
            AddressDto address = new AddressDto(
                    null,
                    "   ",
                    "123",
                    "Praha",
                    "11000",
                    Country.CZ
            );

            assertThat(validator.isValid(address, null)).isFalse();
        }
    }
}