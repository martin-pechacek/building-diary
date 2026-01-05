package cz.mp.building_diary.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListMapperTest {

    private ListMapper listMapper;

    @BeforeEach
    void setUp() {
        listMapper = new ListMapper();
    }

    @Nested
    class Convert {

        @Test
        void shouldConvertListSuccessfully() {
            List<String> source = List.of("1", "2", "3");

            List<Integer> result = listMapper.convert(source, Integer::parseInt);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        void shouldReturnEmptyListWhenSourceIsNull() {
            List<String> nullList = null;

            List<Integer> result = listMapper.convert(nullList, Integer::parseInt);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenSourceIsEmpty() {
            List<String> emptyList = Collections.emptyList();

            List<Integer> result = listMapper.convert(emptyList, Integer::parseInt);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ConvertAndAdd {

        @Test
        void shouldConvertAndAddSuccessfully() {
            List<String> source = List.of("a", "b", "c");
            List<String> target = new ArrayList<>();

            listMapper.convertAndAdd(source, String::toUpperCase, target::add);

            assertThat(target).containsExactly("A", "B", "C");
        }

        @Test
        void shouldDoNothingWhenSourceIsNull() {
            List<String> nullList = null;
            List<String> target = new ArrayList<>();

            listMapper.convertAndAdd(nullList, String::toUpperCase, target::add);

            assertThat(target).isEmpty();
        }

        @Test
        void shouldDoNothingWhenSourceIsEmpty() {
            List<String> emptyList = Collections.emptyList();
            List<String> target = new ArrayList<>();

            listMapper.convertAndAdd(emptyList, String::toUpperCase, target::add);

            assertThat(target).isEmpty();
        }

        @Test
        void shouldApplyMapperToEachElement() {
            List<Integer> source = List.of(1, 2, 3);
            List<Integer> target = new ArrayList<>();

            listMapper.convertAndAdd(source, n -> n * 2, target::add);

            assertThat(target).containsExactly(2, 4, 6);
        }
    }
}