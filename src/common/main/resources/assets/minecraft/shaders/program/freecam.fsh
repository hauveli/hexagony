#version 150

uniform sampler2D DiffuseSampler;
uniform float GreyscaleAmount; // 0.0 = normal, 1.0 = greyscale

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    // fiddle with this to make it look good I guess
    float gray = dot(color.rgb, vec3(0.3, 0.6, 0.1));

    float amount = smoothstep(0.0, 1.0, GreyscaleAmount);
    vec3 result = mix(color.rgb, vec3(gray), amount);
    fragColor = vec4(result, 1.0);
}