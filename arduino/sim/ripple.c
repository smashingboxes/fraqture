#include "ripple.h"
#include "../strobe/color.h"
#include "../strobe/led_strip.h"
#include "../strobe/led_array.h"
#include <string.h>

#define HALF_ROW_COUNT (int)(ROW_COUNT / 2)

uint8_t frame_one[HALF_ROW_COUNT][COL_COUNT];
uint8_t frame_two[HALF_ROW_COUNT][COL_COUNT];

typedef uint8_t(* frame_ptr)[COL_COUNT];

frame_ptr active_frame = frame_one;
frame_ptr inactive_frame = frame_two;

void zero_heightmap(frame_ptr heightmap)
{
    memset(heightmap, 0, sizeof(heightmap[0][0]) * COL_COUNT * HALF_ROW_COUNT);
}

void ripple_init()
{
  zero_heightmap(active_frame);
  zero_heightmap(inactive_frame);
}

void swap_buffers()
{
  frame_ptr temp = active_frame;
  active_frame = inactive_frame;
  inactive_frame = temp;
}

void ripple_touch_point(float x, float y, float strength)
{
  int x_index = (int)(x * COL_COUNT);
  int y_index = (int)(y * HALF_ROW_COUNT);
  active_frame[x_index][y_index] = 255 * strength;
}

void ripple_update()
{
  // http://freespace.virgin.net/hugo.elias/graphics/x_water.htm
  float decay = 0.88;
  for (int x = 1; x < COL_COUNT-1; x++) {
    for (int y = 1; y < HALF_ROW_COUNT-1; y++) {
      uint8_t avg = (active_frame[x-1][y] +
                     active_frame[x+1][y] +
                     active_frame[x][y-1] +
                     active_frame[x][y+1]) / 4;
      inactive_frame[x][y] = (avg * 2 - inactive_frame[x][y]) * decay;
    }
  }
  swap_buffers();
}

void ripple_sim_to_colors(color_t* colors)
{
  color_t high = { 200, 200, 255 };
  color_t low = { 0, 0, 0 };

  for (int x = 0; x < COL_COUNT; x++) {
    for (int y = 0; y < HALF_ROW_COUNT; y++) {

      float amount  = active_frame[x][y] / 255.0f;
      color_t color;
      lerp_color(&low, &high, amount, &color);

      uint16_t flatIndex = led_position_to_index(x, y);
      colors[flatIndex] = color;
    }
  }
}
