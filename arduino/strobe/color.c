#include "color.h"

void lerp_color(color_t *c1, color_t *c2, float amount, color_t *out) {
  out->r = (uint8_t)(c2->r + (c1->r - c2->r) * amount);
  out->g = (uint8_t)(c2->g + (c1->g - c2->g) * amount);
  out->b = (uint8_t)(c2->b + (c1->b - c2->b) * amount);
}
