package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.joml.Vector3f;

public class SceneLights {

  private final AmbientLight ambientLight;
  private final DirectionalLight directionalLight;
  private final List<PointLight> pointLights;
  private final List<SpotLight> spotLights;

  public SceneLights() {
    // Default settings as no lights are set
    this.ambientLight = new AmbientLight(LightColor.WHITE, 1.0F);
    this.directionalLight = new DirectionalLight(LightColor.WHITE, new Vector3f(0.0F, 0.0F, 0.0F), 0.0f);
    this.pointLights = new ArrayList<>();
    this.spotLights = new ArrayList<>();
  }

  public SceneLights(Supplier<Vector3f> position) {
    // Default settings as no lights are set
    this.ambientLight = new AmbientLight(LightColor.WHITE, 0.0F);
    this.directionalLight = new DirectionalLight(LightColor.WHITE, position, 0.5f);
    this.pointLights = new ArrayList<>();
    this.spotLights = new ArrayList<>();
  }

  public AmbientLight getAmbientLight() {
    return this.ambientLight;
  }

  public DirectionalLight getDirectionalLight() {
    return this.directionalLight;
  }

  public List<PointLight> getPointLights() {
    return Collections.unmodifiableList(this.pointLights);
  }

  public List<SpotLight> getSpotLights() {
    return Collections.unmodifiableList(this.spotLights);
  }

  public void setupAmbientLight(LightColor lightColor) {
    this.ambientLight.setColor(lightColor.getUnitRGB());
  }

  public void setupAmbientLight(float intensity) {
    this.ambientLight.setIntensity(intensity);
  }

  public void setupAmbientLight(LightColor lightColor, float intensity) {
    this.setupAmbientLight(lightColor);
    this.setupAmbientLight(intensity);
  }


  public SceneLights addLight(LightColor lightColor, Vector3f position, float intensity) {
    this.pointLights.add(new PointLight(lightColor.getUnitRGB(), position, intensity));
    return this;
  }

  public SceneLights addLight(LightColor lightColor, Vector3f position, float intensity, Vector3f coneDirection,
      float cutOff) {
    this.spotLights.add(new SpotLight(lightColor.getUnitRGB(), position, intensity, coneDirection, cutOff));
    return this;
  }
}
