package me.mat.jprocessor.mappings.generation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;
import me.mat.jprocessor.mappings.generation.generator.impl.AlphabetMappingGenerator;
import me.mat.jprocessor.mappings.generation.generator.impl.RandomMappingGenerator;

@Getter
@RequiredArgsConstructor
public enum GenerationType {

    ALPHABET(new AlphabetMappingGenerator()),
    RANDOM(new RandomMappingGenerator());

    @NonNull
    final MappingGenerator mappingGenerator;

}
