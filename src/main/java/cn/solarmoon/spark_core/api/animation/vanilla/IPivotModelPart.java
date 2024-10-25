package cn.solarmoon.spark_core.api.animation.vanilla;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

public interface IPivotModelPart {

    Vector3f getPivot();

    void setPivot(Vector3f pivot);

    ModelPart getRoot();

    void setRoot(ModelPart root);

}

