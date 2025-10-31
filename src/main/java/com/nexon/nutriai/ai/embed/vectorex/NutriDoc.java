package com.nexon.nutriai.ai.embed.vectorex;

import io.github.javpower.vectorex.keynote.model.MetricType;
import io.github.javpower.vectorexcore.annotation.VectoRexCollection;
import io.github.javpower.vectorexcore.annotation.VectoRexField;
import io.github.javpower.vectorexcore.entity.DataType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@VectoRexCollection(name = "nutri")
public class NutriDoc {
    @VectoRexField(name = "id", isPrimaryKey = true)
    private String id;
    @VectoRexField
    private String content;
    @VectoRexField(
            dataType = DataType.FloatVector,
            dimension = 1024,
            metricType = MetricType.FLOAT_COSINE_DISTANCE
    )
    private List<Float> vector;

    @VectoRexField
    private Map<String, Object> metadata;

    public NutriDoc(String id, String content, float[] vector) {
        new NutriDoc(id, content, vector, Map.of());
    }

    public NutriDoc(String id, String content, float[] vector, Map<String, Object> metadata) {
        this.id = id;
        this.content = content;
        if (vector != null) {
            this.vector = new ArrayList<>();
            for (float f : vector) {
                this.vector.add(f);
            }
        }
        this.metadata = metadata;
    }
}
