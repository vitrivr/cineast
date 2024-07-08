#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;
const float SPECULAR_COLOR = 10;

in vec3 outPosition;
in vec3 outNormal;
in vec3 outTangent;
in vec3 outBitangent;
in vec2 outTextCoord;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
    int hasNormalMap;
};

struct AmbientLight
{
    float factor;
    vec3 color;
};

struct PointLight
{
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation attenuation;
};

struct SpotLight
{
    PointLight pointLight;
    vec3 coneDirection;
    float cutOff;
};

struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};

uniform sampler2D txtSampler;
uniform sampler2D normalMapSampler;
uniform Material material;
uniform AmbientLight ambientLight;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;

vec3 calcNormal(vec3 normal, vec3 tangent, vec3 bitangent, vec2 textCoord) {
    mat3 TBN = mat3(tangent, bitangent, normal);
    vec3 newNormal = texture(normalMapSampler, textCoord).rgb;
    newNormal = normalize(newNormal * 2.0 - 1.0);
    newNormal = normalize(TBN * newNormal);
    return newNormal;
}

vec4 calcAmbient(AmbientLight ambientLight, vec4 ambient) {
    return vec4(ambientLight.factor * ambientLight.color, 1) * ambient;
}

vec4 calcLightColor(vec4 diffuse, vec4 specular, vec3 lightColor, float lightIntensity, vec3 position, vec3 toLightDirection, vec3 normal) {
    vec4 diffuseColor = vec4(0, 0, 0, 1);
    vec4 specColor = vec4(0, 0, 0, 1);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, toLightDirection), 0.0);
    diffuseColor = diffuse * vec4(lightColor, 1) * lightIntensity * diffuseFactor;

    // Specular Light
    vec3 cameraDirection = normalize(-position);
    vec3 fromLightDirection = -toLightDirection;
    vec3 reflectedLight = normalize(reflect(fromLightDirection, normal));
    float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_COLOR);
    specColor = specular * lightIntensity * specularFactor * material.reflectance * vec4(lightColor, 1);

    return (diffuseColor + specColor);
}

vec4 calcPointLight(vec4 diffuse, vec4 specular, PointLight light, vec3 position, vec3 normal) {
    vec3 lightDirection = light.position - position;
    vec3 toLightDirection = normalize(lightDirection);
    vec4 lightColor = calcLightColor(diffuse, specular, light.color, light.intensity, position, toLightDirection, normal);

    // Attenuation
    float distance = length(lightDirection);
    float attenuationInv = light.attenuation.constant + light.attenuation.linear * distance + light.attenuation.exponent * distance * distance;
    return lightColor / attenuationInv;
}

vec4 calcSpotLight(vec4 diffuse, vec4 specular, SpotLight light, vec3 position, vec3 normal) {
    vec3 lightDirection = light.pointLight.position - position;
    vec3 toLightDirection = normalize(lightDirection);
    vec3 fromLightDirection = -toLightDirection;
    float spotAlpha = dot(fromLightDirection, normalize(light.coneDirection));

    vec4 color = vec4(0, 0, 0, 0);

    if (spotAlpha > light.cutOff) {
        color = calcPointLight(diffuse, specular, light.pointLight, position, normal);
        color *= (1.0 - (1.0 - spotAlpha) / (1.0 - light.cutOff));
    }
    return color;
}

vec4 calcDirectionalLight(vec4 diffuse, vec4 specular, DirectionalLight light, vec3 position, vec3 normal) {
    return calcLightColor(diffuse, specular, light.color, light.intensity, position, normalize(light.direction), normal);
}

void main() {
    vec4 textColor = texture(txtSampler, outTextCoord);
    vec4 ambient = calcAmbient(ambientLight, textColor + material.ambient);
    vec4 diffuse = textColor + material.diffuse;
    vec4 specular = textColor + material.specular;

    vec3 normal = outNormal;
    if (material.hasNormalMap > 0) {
        normal = calcNormal(outNormal, outTangent, outBitangent, outTextCoord);
    }

    vec4 diffuseSpecularComp = calcDirectionalLight(diffuse, specular, directionalLight, outPosition, normal);
    for (int ic = 0; ic < MAX_POINT_LIGHTS; ic++) {
        if (pointLights[ic].intensity > 0) {
            diffuseSpecularComp += calcPointLight(diffuse, specular, pointLights[ic], outPosition, normal);
        }
    }
    for (int ic = 0; ic < MAX_SPOT_LIGHTS; ic++) {
        if (spotLights[ic].pointLight.intensity > 0) {
            diffuseSpecularComp += calcSpotLight(diffuse, specular, spotLights[ic], outPosition, normal);
        }
    }
    fragColor = ambient + diffuseSpecularComp;
}