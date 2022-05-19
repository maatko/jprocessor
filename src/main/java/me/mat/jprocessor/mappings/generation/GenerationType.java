package me.mat.jprocessor.mappings.generation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;
import me.mat.jprocessor.mappings.generation.generator.impl.AlphabetMappingGenerator;

@Getter
@RequiredArgsConstructor
public enum GenerationType {

    ALPHABET(new AlphabetMappingGenerator());

    @NonNull
    final MappingGenerator mappingGenerator;

}
