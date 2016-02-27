#ifndef LIQUID_H_
#define LIQUID_H_

#ifdef __cplusplus
extern "C"{
#endif

/**
 *  This runs two separate simulations a top half and a bottom half
 */
typedef enum {
  RippleSimTop,
  RippleSimBotom
} RippleSim;

/**
 *  Initialize the heightmaps
 */
void ripple_init();

/**
 *  Step the simulation
 */
void ripple_update();

/**
 * @param sim The simulation to touch
 * @param x, y The coordinate to touch (normalized to 0..1)
 * @param strength The strength of the disturbance from 0..1
 */
void ripple_touch_point(RippleSim sim, float x, float y, float strength);

/**
 *  @param sim The simulation to convert
 *  @param colors The colors to fill
 */
void ripple_sim_to_colors(RippleSim sim, color_t* colors)

#ifdef __cplusplus
}
#endif

#endif
