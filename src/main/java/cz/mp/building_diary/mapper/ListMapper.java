package cz.mp.building_diary.mapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class ListMapper {

    public <S, T> List<T> convert(List<S> source, Function<S, T> mapper) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return source.stream()
                .map(mapper)
                .toList();
    }

    public <S, T> void convertAndAdd(List<S> source, Function<S, T> mapper, Consumer<T> adder) {
        if (source != null) {
            source.stream()
                    .map(mapper)
                    .forEach(adder);
        }
    }
}