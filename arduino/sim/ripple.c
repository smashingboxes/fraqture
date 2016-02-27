#include "ripple.h"
#include "../strobe/color.h"
#include "../strobe/led_strip.h"

#define HALF_ROW_COUNT floor(ROW_COUNT / 2)

// Heightmaps to keep the wave data.
// Run two separate simulations: top & bottom.
uint8_t top_frame[HALF_ROW_COUNT][COL_COUNT];
uint8_t bottom_frame[HALF_ROW_COUNT][COL_COUNT];

void ripple_init()
{
  zero_heightmap(&top_frame);
  zero_heightmap(&bottom_frame);
}

void zero_heightmap(uint8_t* heightmap)
{
  memset(heightmap, 0, sizeof(heightmap[0][0]) * COL_COUNT * HALF_ROW_COUNT);
}

uint8_t* heightmap_for_sim(RippleSim sim) {
  switch (sim) {
    case RippleSimTop:
      return &top_frame;
    case RippleSimBottom:
      return &bottom_frame;
    default:
      fprintf(stderr, "Unmatched simulation type: %d", sim);
      return &top_frame;
  }
}

void ripple_touch_point(RippleSim sim, float x, float y, float strength)
{
  uint8_t* heightmap = heightmap_for_sim(sim);
  heightmap[x][y] = 255 * strength;
}

void ripple_update()
{
  update_heightmap(&top_frame);
  update_heightmap(&bottom_frame);
}

void update_heightmap(uint8_t* heigtmap)
{
  uint8_t* heightmap = heightmap_for_sim(sim);

  for (int i = 0; i < COL_COUNT; i++) {
    for (int j = 0; j < HALF_ROW_COUNT; j++) {
      // todo: advance the ripples
    }
  }
}

void ripple_sim_to_colors(RippleSim sim, color_t* colors)
{
  uint8_t *heightmap = heightmap_for_sim(sim);
  heightmap_to_colors(&heightmap, &colors);
}

void heightmap_to_colors(uint8_t* heightmap, color_t* colors)
{
  // Convet the heightmap to color values
  high = (color_t){200, 200, 255};
  low = (color_t){0, 0, 0};

  for (int i = 0; i < COL_COUNT; i++) {
    for (int j = 0; j < HALF_ROW_COUNT; j++) {

      float amount  = heightmap[i][j] / 255.0f;
      color_t color;
      lerp_color(&high, &low, amount, &color);

      // TODO: convert the index correctly
      int flatIndex = i + (j * COL_COUNT);
      colors[flatIndex] = color;
    }
  }
}
