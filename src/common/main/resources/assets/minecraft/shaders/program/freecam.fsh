#version 150

uniform sampler2D DiffuseSampler;
uniform float FirstArgumentAmount; // 0.0 = normal, 1.0 = greyscale
uniform float SecondArgumentAmount; // 0.0 = normal, 1.0 = red
uniform float ThirdArgumentAmount; // 0.0 = normal, 1.0 = max tint blending

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    // fiddle with this to make it look good I guess
    float gray = dot(color.rgb, vec3(0.3, 0.6, 0.1));
    vec3 endingcolor = vec3(0.8, 0.7, 0.7);
    vec3 blue = vec3(0.2, 0.3, 0.7);

    // this is probably terribly bad in a few waysa but whatever, it works
    float amountgray = smoothstep(0.2, 1.0, FirstArgumentAmount);
    vec3 resultgray = mix(color.rgb, vec3(gray), amountgray);
    vec3 resultblue = mix(resultgray.rgb, blue, 0.05 + (0.55 - FirstArgumentAmount) / 4); // 0.55 is max value of FirstArgumentAmount. 0.1 and 4 are arbitrary and feel-based so whatever

    vec3 resultendingcolor = mix(resultblue.rgb, endingcolor, SecondArgumentAmount);
    vec3 resultfinal = mix(resultendingcolor.rgb, vec3(0), ThirdArgumentAmount); // make it do nothing if not enough time to get started?
    fragColor = vec4(resultfinal.rgb, color.a);
}