#ifndef COLOR_H_
#define COLOR_H_

#include <stdint.h>

typedef struct {
  uint8_t r;
  uint8_t g;
  uint8_t b;
} color_t;

#ifdef __cplusplus
extern "C"{
#endif

void lerp_color(color_t *c1, color_t *c2, float amount, color_t *out);

#ifdef __cplusplus
}
#endif
#endif
