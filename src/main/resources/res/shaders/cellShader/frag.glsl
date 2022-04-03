in vec2 pos;

out vec4 color;

uniform vec3 ocolor;

uniform bool isProtected;

uniform vec3 ncolor;

uniform float lerpThresh;

void main() {
    color = vec4(ocolor, 1);
    if (dot(pos, pos) < lerpThresh) {
        color = vec4(ncolor, 1);
    }
    if (abs(pos.x) > 0.42 || abs(pos.y) > 0.42) {
        color = vec4(0, 0, 0, 1);
    }
    if (((abs(pos.x) > 0.3 && abs(pos.y) < 0.2) || (abs(pos.y) > 0.3 && abs(pos.x) < 0.2)) && isProtected) {
        color *= 0.75;
    }
}
