precision highp float;

uniform vec3 iMouse0;
uniform vec3 iMouse1;
uniform vec3 iMouse2;
uniform vec3 iMouse3;
uniform vec3 iMouse4;
uniform vec3 iMouse5;
uniform vec3 iMouse6;
uniform vec3 iMouse7;
uniform vec3 iMouse8;
uniform vec3 iMouse9;
uniform float iMouseSize;
uniform vec2 iResolution;
uniform int iFrame;
uniform sampler2D iChannel0;

uniform float delta;

vec4 encode(vec4 vec)
{
    return vec * vec4(0.5) + vec4(0.5);
}

vec4 decode(vec4 vec)
{
    return vec * vec4(2) - vec4(1);
}

void main()
{
    if (iFrame == 0 || iFrame == 1) { gl_FragColor = encode(vec4(0)); return; }

    vec2 uv = gl_FragCoord.xy / iResolution.xy;
    vec2 dx = vec2(1.0, 0.0) / iResolution.xy;
    vec2 dy = vec2(0.0, 1.0) / iResolution.xy;

    // x - pressure. y - velocity
    vec2 i = decode(texture2D(iChannel0, uv)).xy;
    // x - right. y - left. z - up. w - down
    vec4 p = decode(vec4(texture2D(iChannel0, uv + dx).x, texture2D(iChannel0, uv - dx).x, texture2D(iChannel0, uv + dy).x, texture2D(iChannel0, uv - dy).x));

    if (gl_FragColor.x == 0.5) p.y = p.x;
    if (gl_FragColor.x == iResolution.x - 0.5) p.x = p.y;
    if (gl_FragColor.y == 0.5) p.w = p.z;
    if (gl_FragColor.y == iResolution.y - 0.5) p.z = p.w;

    // apply wave function
    i.y += delta * (-4.0 * i.x + p.x + p.y + p.z + p.w) / 4.0;

    // apply pressure velocity
    i.x += delta * i.y;

    // "Spring" motion
    i.y -= 0.001 * delta * i.x;

    // velocity damping
    i.y *= 1.0 - 0.002 * delta;

    // pressure damping
    i.x *= 0.999;

    float x = i.x;
    float y = i.y;
    float z = (p.x - p.y) / 2.0;
    float w = (p.z - p.w) / 2.0;

    if (iMouse0.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse0.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse1.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse1.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse2.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse2.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse3.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse3.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse4.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse4.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse5.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse5.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse6.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse6.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse7.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse7.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse8.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse8.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }
    if (iMouse9.z > 1.0)
    {
        float dist = distance(gl_FragCoord.xy, iMouse9.xy);
        if (dist <= iMouseSize)
        {
            x += 1.0 - dist / iMouseSize;
        }
    }

    gl_FragColor = encode(vec4(x, y, z, w));
}