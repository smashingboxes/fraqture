#ifndef LIQUID_H_
#define LIQUID_H_

#include "../strobe/color.h"
#include "../strobe/led_array.h"
#define HALF_ROW_COUNT (int)(ROW_COUNT / 2)

#ifdef __cplusplus
extern "C"{
#endif

typedef struct ripple_state ripple_state_t;

/**
 *  Create a new ripple simulation.
 */
ripple_state_t* ripple_init();

/**
 *  Destroy an existing ripple simulation.
 */
void ripple_release(ripple_state_t* ripple);

/**
 *  Step the simulation.
 */
void ripple_update(ripple_state_t* ripple);

/**
 * @param x, y The coordinate to touch (normalized to 0..1)
 * @param strength The strength of the disturbance from 0..1
 */
void ripple_touch_point(ripple_state_t* ripple, float x, float y, float strength);

/**
 *  @param colors The colors to fill
 */
void ripple_sim_to_colors(ripple_state_t* ripple, color_t* colors);

#ifdef __cplusplus
}
#endif

#endif
