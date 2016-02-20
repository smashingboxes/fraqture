#include <SPI.h>
#include "led_strip.h"

led_strip_t strip;
uint8_t current = 0;

void led_transfer(uint8_t x) {
  SPI.transfer(x);
  Serial.begin(9600);
}

void clear_led_f(uint16_t index, color_t *color)
{
  memset(color, 0, sizeof(color_t));
}

void clear_leds(void) { led_map(&strip, clear_led_f); }

void setup() {
  SPI.begin();
  led_init(&strip, led_transfer);
  clear_leds();
}

void loop() {
  if(Serial.available()) {
    // Add commands here
    led_refresh(&strip);
  }
}
