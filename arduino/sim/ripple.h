#ifndef LIQUID_H_
#define LIQUID_H_

#include "../strobe/color.h"

#ifdef __cplusplus
extern "C"{
#endif

void ripple_init();
void ripple_update();

/**
 * @param x, y The coordinate to touch (normalized to 0..1)
 * @param strength The strength of the disturbance from 0..1
 */
void ripple_touch_point(float x, float y, float strength);

/**
 *  @param colors The colors to fill
 */
void ripple_sim_to_colors(color_t* colors);

#ifdef __cplusplus
}
#endif

#endif
