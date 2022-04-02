layout (location = 0) in vec3 vertices;

uniform mat4 matrix;

out vec2 pos;

void main() {
	gl_Position = matrix * vec4(vertices, 1);
	pos = vertices.xy;
}