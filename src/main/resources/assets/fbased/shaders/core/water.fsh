
#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 textureColor = texture(Sampler0, texCoord);
    fragColor = textureColor * vertexColor;
}
