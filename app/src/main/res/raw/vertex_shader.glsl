attribute vec2 pos;

void main()
{
    gl_Position = vec4(pos.xy, 0.0, 1.0);
}