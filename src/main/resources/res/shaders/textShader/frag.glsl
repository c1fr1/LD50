in vec2 texCoords;

out vec4 color;

uniform vec3 texColor;

uniform sampler2D texSampler;

void main() {
    color.xyz = texColor;
    color.a = texture(texSampler, texCoords).r;
}