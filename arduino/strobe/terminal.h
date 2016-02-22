#ifndef TERMINAL_H_
#define TERMINAL_H_

#include <stdint.h>
#include <stdbool.h>

// Sending RESET_LENGTH of RESET_CHARACTER will reset the protocol
#define RESET_LENGTH 6
#define RESET_CHARACTER '*'

typedef void (*terminal_handler)(void *);

typedef struct terminal_cmd {
  char trigger;
  uint8_t length;
  terminal_handler handler;
  struct terminal_cmd *next;
} terminal_cmd_t;

typedef struct {
  terminal_cmd_t *current_handler;
  terminal_cmd_t *handler_chain;
  uint8_t character_index;
  uint8_t reset_index;
  char buffer[CMD_LENGTH];
} terminal_t;

#ifdef __cplusplus
extern "C"{
#endif

void terminal_init(terminal_t *term);
bool terminal_attach(terminal_t *term, terminal_cmd_t *cmd);
void terminal_feed(terminal_t *term, char incoming);

#ifdef __cplusplus
}
#endif

#endif
