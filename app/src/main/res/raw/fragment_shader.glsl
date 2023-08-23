precision highp float;

uniform vec2 iResolution;
uniform sampler2D iChannel0;
uniform sampler2D puddle;

uniform sampler2D leaf0;
uniform vec2 leaf0Position;
uniform float leaf0Rotation;
uniform float leaf0Size;

uniform sampler2D leaf1;
uniform vec2 leaf1Position;
uniform float leaf1Rotation;
uniform float leaf1Size;

uniform sampler2D leaf2;
uniform vec2 leaf2Position;
uniform float leaf2Rotation;
uniform float leaf2Size;

uniform sampler2D leaf3;
uniform vec2 leaf3Position;
uniform float leaf3Rotation;
uniform float leaf3Size;

uniform sampler2D leaf4;
uniform vec2 leaf4Position;
uniform float leaf4Rotation;
uniform float leaf4Size;

uniform sampler2D leaf5;
uniform vec2 leaf5Position;
uniform float leaf5Rotation;
uniform float leaf5Size;

uniform sampler2D leaf6;
uniform vec2 leaf6Position;
uniform float leaf6Rotation;
uniform float leaf6Size;

uniform sampler2D leaf7;
uniform vec2 leaf7Position;
uniform float leaf7Rotation;
uniform float leaf7Size;

vec4 decode(vec4 vec)
{
    return vec * vec4(2) - vec4(1);
}

vec2 Circle(float Start, float Points, float Point)
{
    float Rad = (3.141592 * 2.0 * (1.0 / Points)) * (Point + Start);
    return vec2(sin(Rad), cos(Rad));
}

vec4 blur(sampler2D tex, vec2 fragCoord)
{
    vec2 uv = fragCoord.xy / iResolution.xy;
    vec2 PixelOffset = 1.0 / iResolution.xy;

    float Start = 2.0 / 14.0;
    vec2 Scale = 0.66 * 4.0 * 2.0 * PixelOffset.xy;

    vec4 N0 = decode(texture2D(tex, uv + Circle(Start, 9.0, 0.0) * Scale));
    vec4 N1 = decode(texture2D(tex, uv + Circle(Start, 9.0, 1.0) * Scale));
    vec4 N2 = decode(texture2D(tex, uv + Circle(Start, 9.0, 2.0) * Scale));
    vec4 N3 = decode(texture2D(tex, uv + Circle(Start, 9.0, 3.0) * Scale));
    vec4 N4 = decode(texture2D(tex, uv + Circle(Start, 9.0, 4.0) * Scale));
    vec4 N5 = decode(texture2D(tex, uv + Circle(Start, 9.0, 5.0) * Scale));
    vec4 N6 = decode(texture2D(tex, uv + Circle(Start, 9.0, 6.0) * Scale));
    vec4 N7 = decode(texture2D(tex, uv));

    return (N0+N1+N2+N3+N4+N5+N6+N7) / 14.0;
}

float cross2d( vec2 a, vec2 b ) { return a.x*b.y - a.y*b.x; }

vec2 quadTexture(vec2 a, vec2 b, vec2 c, vec2 d)
{
    vec2 e = b-a;
    vec2 f = d-a;
    vec2 g = a-b+c-d;
    vec2 h = gl_FragCoord.xy - a;
    float k1 = cross2d(e, f) + cross2d(h, g);
    float k0 = cross2d(h, e);

    return vec2((h.x*k1+f.x*k0)/(e.x*k1-g.x*k0), -k0/k1);
}

const float PI = 3.14159;

vec2 rotate(vec2 point, vec2 center, float angle)
{
    float angleRadians = angle * PI / 180.0;
    float s = sin(angleRadians); float c = cos(angleRadians);
    return mat2(c, s, -s, c) * (point - center) + center;
}

vec4 leaf(vec4 color, vec2 center, vec2 size, sampler2D tex, float angle)
{
    vec2 a = vec2(center.x - size.x / 2., center.y - size.y / 2.);
    vec2 b = vec2(center.x + size.x / 2., center.y - size.y / 2.);
    vec2 c = vec2(center.x + size.x / 2., center.y + size.y / 2.);
    vec2 d = vec2(center.x - size.x / 2., center.y + size.y / 2.);

    a = rotate(a, center, angle);
    b = rotate(b, center, angle);
    c = rotate(c, center, angle);
    d = rotate(d, center, angle);

    vec2 uv = quadTexture(a, b, c, d);

    if(max(abs(uv.x-0.5), abs(uv.y-0.5)) < 0.5)
    {
        vec4 leafColor = texture2D( tex, uv );

        if (leafColor.x > 0.07 || leafColor.y > 0.07 || leafColor.z > 0.07)
        {
            return leafColor;
        }
    }

    return color;
}

void main()
{
    vec2 uv = gl_FragCoord.xy / iResolution.xy;
    vec4 data = blur(iChannel0, gl_FragCoord.xy);
    gl_FragColor = texture2D(puddle, uv + 0.2 * data.zw);
    vec3 normal = normalize(vec3(-data.z, 0.2, -data.w));
    gl_FragColor += vec4(1) * pow(max(0.0, dot(normal, normalize(vec3(-3, 10, 3)))), 60.0);

    gl_FragColor = leaf(gl_FragColor, leaf0Position, vec2(leaf0Size), leaf0, leaf0Rotation);
    gl_FragColor = leaf(gl_FragColor, leaf1Position, vec2(leaf1Size), leaf1, leaf1Rotation);
    gl_FragColor = leaf(gl_FragColor, leaf2Position, vec2(leaf2Size), leaf2, leaf2Rotation);
    gl_FragColor = leaf(gl_FragColor, leaf3Position, vec2(leaf3Size), leaf3, leaf3Rotation);
    gl_FragColor = leaf(gl_FragColor, leaf4Position, vec2(leaf4Size), leaf4, leaf4Rotation);
    gl_FragColor = leaf(gl_FragColor, leaf5Position, vec2(leaf5Size), leaf5, leaf5Rotation);
    gl_FragColor = leaf(gl_FragColor, leaf6Position, vec2(leaf6Size), leaf6, leaf6Rotation);
    gl_FragColor = leaf(gl_FragColor, leaf7Position, vec2(leaf7Size), leaf7, leaf7Rotation);
}