#include "ripple.h"
#include "../strobe/color.h"
#include "../strobe/led_strip.h"
#include <stdlib.h>
#include <string.h>

void zero_heightmap(ripple_frame_ptr heightmap)
{
  memset(heightmap, 0, sizeof(heightmap[0][0]) * COL_COUNT * HALF_ROW_COUNT);
}

void ripple_init(ripple_state_t* ripple)
{
  ripple->active_frame = ripple->frame_one;
  ripple->inactive_frame = ripple->frame_two;
  zero_heightmap(ripple->active_frame);
  zero_heightmap(ripple->inactive_frame);
}

void swap_buffers(ripple_state_t* ripple)
{
  ripple_frame_ptr temp = ripple->active_frame;
  ripple->active_frame = ripple->inactive_frame;
  ripple->inactive_frame = temp;
}

void ripple_touch_point(ripple_state_t* ripple, float x, float y, float strength)
{
  int x_index = (int)(x * COL_COUNT);
  int y_index = (int)(y * HALF_ROW_COUNT);
  ripple->active_frame[x_index][y_index] = 255 * strength;
}

void ripple_update(ripple_state_t* ripple)
{
  // http://freespace.virgin.net/hugo.elias/graphics/x_water.htm
  float decay = 0.88;
  for (int x = 1; x < COL_COUNT-1; x++) {
    for (int y = 1; y < HALF_ROW_COUNT-1; y++) {
      uint8_t avg = (ripple->active_frame[x-1][y] +
                     ripple->active_frame[x+1][y] +
                     ripple->active_frame[x][y-1] +
                     ripple->active_frame[x][y+1]) / 4;
      ripple->inactive_frame[x][y] = (avg * 2 - ripple->inactive_frame[x][y]) * decay;
    }
  }
  swap_buffers(ripple);
}

void ripple_sim_to_colors(ripple_state_t* ripple, color_t* colors)
{
  color_t high = { 200, 200, 255 };
  color_t low = { 0, 0, 0 };

  for (int x = 0; x < COL_COUNT; x++) {
    for (int y = 0; y < HALF_ROW_COUNT; y++) {

      float amount = ripple->active_frame[x][y] / 255.0f;
      color_t color;
      lerp_color(&low, &high, amount, &color);

      uint16_t flatIndex = led_position_to_index(x, y);
      colors[flatIndex] = color;
    }
  }
}
