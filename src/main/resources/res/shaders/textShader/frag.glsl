in vec2 texCoords;

out vec4 color;

uniform sampler2D texSampler;

void main() {
    color.xyz = vec3(1, 1, 1);
    color.a = texture(texSampler, texCoords).r;
}