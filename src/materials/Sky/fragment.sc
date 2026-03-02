#ifndef INSTANCING
  $input v_fogColor, v_worldPos, v_underwaterRainTimeDay
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>
  uniform vec4 FogAndDistanceControl;
  uniform vec4 u_time;
  SAMPLER2D_AUTOREG(s_MatTexture);

  // ============================================
  // CHANGE ONLY THIS NUMBER TO MATCH YOUR VIDEO
  #define TOTAL_FRAMES 80.0
  // ============================================
  
  // DO NOT TOUCH BELOW
  #define FPS 24.0
#endif

void main() {
  #ifndef INSTANCING
    vec3 viewDir = normalize(v_worldPos);
    bool isMenu = abs(viewDir.y) < 0.01;

    if (isMenu) {
      float frame = floor(mod(u_time.x * FPS, TOTAL_FRAMES));
      float frameU = v_worldPos.x * 0.5 + 0.5;
      float frameV = (v_worldPos.z * 0.5 + 0.5 + frame) / TOTAL_FRAMES;
      vec4 diffuse = texture2D(s_MatTexture, vec2(frameU, frameV));
      gl_FragColor = vec4(colorCorrection(diffuse.rgb), 1.0);
    } else {
      nl_environment env;
      env.end = false;
      env.nether = false;
      env.underwater = v_underwaterRainTimeDay.x > 0.5;
      env.rainFactor = v_underwaterRainTimeDay.y;
      env.dayFactor = v_underwaterRainTimeDay.w;

      nl_skycolor skycol;
      if (env.underwater) {
        skycol = nlUnderwaterSkyColors(env.rainFactor, v_fogColor.rgb);
      } else {
        skycol = nlOverworldSkyColors(env.rainFactor, v_fogColor.rgb);
      }

      vec3 skyColor = nlRenderSky(skycol, env, -viewDir, v_fogColor, v_underwaterRainTimeDay.z);
      #ifdef NL_SHOOTING_STAR
        skyColor += NL_SHOOTING_STAR*nlRenderShootingStar(viewDir, v_fogColor, v_underwaterRainTimeDay.z);
      #endif
      #ifdef NL_GALAXY_STARS
        skyColor += NL_GALAXY_STARS*nlRenderGalaxy(viewDir, v_fogColor, env, v_underwaterRainTimeDay.z);
      #endif

      skyColor = colorCorrection(skyColor);
      gl_FragColor = vec4(skyColor, 1.0);
    }
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
