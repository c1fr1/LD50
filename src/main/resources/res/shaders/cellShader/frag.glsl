in vec2 pos;

out vec4 color;

uniform vec3 ocolor;

uniform bool isProtected;

void main() {
    color = vec4(ocolor, 1);
    if (pos.x < -0.4 || pos.x > 0.4 || pos.y < -0.4 || pos.y > 0.4) {
        color = vec4(0, 0, 0, 1);
    }
}
